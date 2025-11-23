import interfaces.DistTask;
import java.io.IOException;
import mcpi.MCPi;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serde.DistSerde;

public class DistClient {

    private static Integer TIMEOUT = 10_000;
    private static Logger logger = LoggerFactory.getLogger(DistClient.class);

    private ZooKeeper zk;
    private String zkServer;
    private String taskNodePath;
    private Integer groupNum;
    private DistTask dTask;

    // Watchers
    private Watcher connectionWatcher = event -> {
        watchConnection(event);
    };

    private Watcher resultNodeWatcher = event -> {
        watchResultNode(event);
    };

    // Callbacks
    private AsyncCallback.StatCallback handleResultNodeCreatedCb = (
        int rc,
        String path,
        Object ctx,
        Stat stat
    ) -> {
        handleResultNodeCreated(rc, path, ctx, stat);
    };

    private AsyncCallback.DataCallback handleResultNodeDataCb = (
        int rc,
        String path,
        Object ctx,
        byte[] data,
        Stat stat
    ) -> {
        handleResultNodeData(rc, path, ctx, data, stat);
    };

    public DistClient(String zkHost, Integer groupNum, DistTask dt) {
        zkServer = zkHost;
        this.groupNum = groupNum;
        dTask = dt;

        logger.info("Zookeeper connection information: {}", zkServer);
    }

    private void startClient()
        throws IOException, KeeperException, InterruptedException {
        zk = new ZooKeeper(zkServer, TIMEOUT, connectionWatcher);
    }

    private void watchConnection(WatchedEvent event) {
        logger.info("Event received : {}", event);
        if (
            event.getType() == Watcher.Event.EventType.None // This seems to be the event type associated with connections.
        ) {
            // Once we are connected, send our task if we have not done so.
            if (
                event.getPath() == null &&
                event.getState() == Watcher.Event.KeeperState.SyncConnected &&
                taskNodePath == null
            ) {
                try {
                    byte[] dTaskSerial = DistSerde.serialize(dTask);

                    // Create a sequential znode with the Task object as its data.
                    taskNodePath = zk.create(
                        String.format("/dist%d/tasks/task-", groupNum),
                        dTaskSerial,
                        Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT_SEQUENTIAL
                    );
                    logger.info("Task node {} created", taskNodePath);

                    // Place watch for the result znode which will be created under our task znode.
                    String resultNodePath = taskNodePath + "/result";
                    Watcher resultNodeWatcher = e -> {
                        watchResultNode(e);
                    };

                    zk.exists(
                        resultNodePath,
                        resultNodeWatcher,
                        handleResultNodeCreatedCb,
                        null
                    );
                } catch (IOException ioe) {
                    logger.error(ioe.toString());
                } catch (KeeperException ke) {
                    logger.error(ke.toString());
                } catch (InterruptedException ie) {
                    logger.error(ie.toString());
                }
            }
        }
    }

    private void watchResultNode(WatchedEvent event) {
        logger.info("Event received : {}", event);
        // The result znode was created.
        if (
            event.getType() == Watcher.Event.EventType.NodeCreated &&
            event.getPath().equals(taskNodePath + "/result")
        ) {
            logger.info("Result node created at {}", event.getPath());

            zk.getData(
                taskNodePath + "/result",
                null,
                handleResultNodeDataCb,
                null
            );
        }
    }

    private void handleResultNodeCreated(
        int rc,
        String path,
        Object ctx,
        Stat stat
    ) {
        // The client is notified that the result is ready; if it is we ask for the Data
        logger.info(
            "Result node callback triggered : {} : {} : {} : {}",
            rc,
            path,
            ctx,
            stat
        );

        switch (Code.get(rc)) {
            case OK:
                logger.info("Data callback status: OK");
                // Ask for data in the result znode (asynchronously). We do not have to watch
                // this znode anymore.
                zk.getData(
                    taskNodePath + "/result",
                    null,
                    handleResultNodeDataCb,
                    null
                );
                break;
            case NONODE:
                // The result znode was not ready, we will just make sure to reinstall the
                // watcher.
                // Ideally we should come here only once!, if at all. That will be the time we
                // called
                // exists on the result znode immediately after creating the task znode.
                logger.info(
                    "Data callback status: {}... result node probably not ready yet...",
                    Code.get(rc)
                );
                zk.exists(
                    taskNodePath + "/result",
                    resultNodeWatcher,
                    null,
                    null
                );
                break;
            default:
                logger.info(
                    "Data callback status: {}... something went wrong...",
                    Code.get(rc)
                );
                break;
        }
    }

    private void handleResultNodeData(
        int rc,
        String path,
        Object ctx,
        byte[] data,
        Stat stat
    ) {
        logger.info(
            "Result data callback triggered : {} : {} : {} : {}",
            rc,
            path,
            ctx,
            stat
        );

        try {
            dTask = DistSerde.deserialize(data);
        } catch (Exception e) {
            // Some error happened, we should set the task object reference to null to avoid confusion.
            logger.error(e.toString());
            dTask = null;
        }

        cleanup();

        // Free the main thread to go ahead and terminate.
        synchronized (this) {
            this.notify();
        }
    }

    private void cleanup() {
        // Cleanup, we do not need our task and result nodes anymore
        zk.delete(taskNodePath + "/result", -1, null, null);
        zk.delete(taskNodePath, -1, null, null);
    }

    public DistTask getDistTask() {
        return dTask;
    }

    public static void main(String args[]) throws Exception {
        String zkHost = System.getenv("ZKSERVER");
        Integer groupNum = Integer.parseInt(System.getenv("GROUP_NUM"));
        Long samples = Long.parseLong(args[0]);
        MCPi mcpi = new MCPi(samples);

        DistClient dt = new DistClient(zkHost, groupNum, mcpi);
        dt.startClient();

        synchronized (dt) {
            try {
                dt.wait();
            } catch (InterruptedException ie) {}
        }

        mcpi = (MCPi) dt.getDistTask();
        logger.info("{}", mcpi.getPi());
    }
}
