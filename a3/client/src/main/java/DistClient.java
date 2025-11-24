import interfaces.DistTask;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

// You may have to add other interfaces such as for threading, etc., as needed.
public class DistClient
    implements Watcher, AsyncCallback.StatCallback, AsyncCallback.DataCallback {

    private static Integer TIMEOUT = 10000;
    private static Logger logger = LoggerFactory.getLogger(DistClient.class);

    ZooKeeper zk;
    String zkServer;
    String taskNodeName;
    Integer id;
    DistTask dTask;

    DistClient(String zkHost, Integer groupNum, DistTask dt) {
        zkServer = zkHost;
        id = groupNum;
        dTask = dt;

        logger.info("DISTAPP : ZK Connection information: {}", zkServer);
    }

    void startClient()
        throws IOException, KeeperException, InterruptedException {
        // , UnknownHostException
        zk = new ZooKeeper(zkServer, TIMEOUT, this); // connect to ZK.
    }

    // Implementing the Watcher interface
    public void process(WatchedEvent e) {
        // Get event notifications.

        // !! IMPORTANT !!
        // Our Application Client is a simple process that simply sends one request
        // right now.
        // It receives the results by waiting for the appropriate event indicating a
        // result
        // Do not perform any time consuming/waiting steps here
        // including in other functions called from here.
        // Your will be essentially holding up ZK client library
        // thread and you will not get other notifications.
        // Instead include another thread in your program logic that
        // does the time consuming "work" and notify that thread from here.

        logger.info("DISTAPP : Event received : {}", e);
        if (
            e.getType() == Watcher.Event.EventType.None // This seems to be the event type associated with connections.
        ) {
            // Once we are connected, send our task if we have not done so.
            if (
                e.getPath() == null &&
                e.getState() == Watcher.Event.KeeperState.SyncConnected &&
                taskNodeName == null
            ) {
                try {
                    // Serialize our Task object to a byte array!
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(bos);
                    oos.writeObject(dTask);
                    oos.flush();
                    byte[] dTaskSerial = bos.toByteArray();

                    // Create a sequential znode with the Task object as its data.
                    taskNodeName = zk.create(
                        String.format("/dist%d/tasks/task-", id),
                        dTaskSerial,
                        Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT_SEQUENTIAL
                    );
                    logger.info("DISTAPP : TaskNode : {}", taskNodeName);

                    // Place watch for the result znode which will be created under our task znode.
                    zk.exists(taskNodeName + "/result", this, this, null);
                } catch (IOException ioe) {
                    logger.error(ioe.toString());
                } catch (KeeperException ke) {
                    logger.error(ke.toString());
                } catch (InterruptedException ie) {
                    logger.error(ie.toString());
                }
            }
        }
        // The result znode was created.
        else if (
            e.getType() == Watcher.Event.EventType.NodeCreated &&
            e.getPath().equals(taskNodeName + "/result")
        ) {
            logger.info("DISTAPP : Node created : {}", e.getPath());
            // Ask for data in the result znode (asynchronously). We do not have to watch
            // this znode anymore.
            zk.getData(taskNodeName + "/result", null, this, null);
        }
    }

    // Implementing the AsyncCallback.StatCallback interface. This will be invoked
    // by the zk.exists
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        // The client is notified that the result is ready; if it is we ask for the Data

        logger.info(
            "DISTAPP : processResult : StatCallback : {} : {} : {} : {}",
            rc,
            path,
            ctx,
            stat
        );
        switch (Code.get(rc)) {
            case OK:
                logger.info("DISTAPP : processResult : StatCallback : OK");
                // Ask for data in the result znode (asynchronously). We do not have to watch
                // this znode anymore.
                zk.getData(taskNodeName + "/result", null, this, null);
                break;
            case NONODE:
                // The result znode was not ready, we will just make sure to reinstall the
                // watcher.
                // Ideally we should come here only once!, if at all. That will be the time we
                // called
                // exists on the result znode immediately after creating the task znode.
                logger.info(
                    "DISTAPP : processResult : StatCallback : {}",
                    Code.get(rc)
                );
                zk.exists(taskNodeName + "/result", this, null, null);
                break;
            default:
                logger.info(
                    "DISTAPP : processResult : StatCallback : {}",
                    Code.get(rc)
                );
                break;
        }
    }

    // Implementing the AsyncCallback.DataCallback. This will be invoked as a result
    // of zk.getData on the result node.
    public void processResult(
        int rc,
        String path,
        Object ctx,
        byte[] data,
        Stat stat
    ) {
        logger.info(
            "DISTAPP : processResult : DataCallback : {} : {} : {} : {}",
            rc,
            path,
            ctx,
            stat
        );
        try {
            // Deserialize the "data" back into a task object (which will now also contain
            // the results) and update our task object reference.
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            ObjectInput in = new ObjectInputStream(bis);
            dTask = (DistTask) in.readObject();
        } catch (Exception e) {
            // Some error happened, we should set the task object reference to null to avoid
            // confusion.
            logger.error(e.toString());
            dTask = null;
        }

        // Cleanup, we do not need our task and result nodes anymore.
        zk.delete(taskNodeName + "/result", -1, null, null);
        zk.delete(taskNodeName, -1, null, null);

        // Free the main thread to go ahead and terminate.
        synchronized (this) {
            this.notify();
        }
    }

    // Called after the computation is done at worker and result is send back here
    // Get back the Task Object now, which should have our results.
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

        // DEBUG ONLY - the compute function should be called by the worker.
        // mcpi.compute();
        // logger.debug("{}", mcpi.getPi());

        // We will wait till we get the results and are notified about it.
        synchronized (dt) {
            try {
                dt.wait();
            } catch (InterruptedException ie) {}
        }

        mcpi = (MCPi) dt.getDistTask();
        logger.info("{}", mcpi.getPi());
    }
}
