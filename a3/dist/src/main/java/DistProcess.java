import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.UnknownHostException;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import roles.DistManager;
import roles.DistWorker;
import roles.interfaces.DistRole;

public class DistProcess {

    private static Integer TIMEOUT = 10_000;
    private static Logger logger = LoggerFactory.getLogger(DistProcess.class);

    private ZooKeeper zk;
    private String zkServer;
    private String pinfo;
    private Integer groupNum;

    private DistRole currRole;
    private Boolean initialized = false;

    // Watchers
    private Watcher connectionWatcher = event -> {
        watchConnection(event);
    };

    public DistProcess(String zkhost, Integer groupNum) {
        zkServer = zkhost;
        pinfo = ManagementFactory.getRuntimeMXBean().getName(); // get process groupNum
        this.groupNum = groupNum;

        logger.info("Zookeeper connection information : " + zkServer);
        logger.info("Process information : " + pinfo);
    }

    private void startProcess()
        throws IOException, UnknownHostException, KeeperException, InterruptedException {
        zk = new ZooKeeper(zkServer, TIMEOUT, connectionWatcher);
    }

    private void initialize() {
        try {
            try {
                runForManager();
                currRole = new DistManager(zk, groupNum);
            } catch (KeeperException.NodeExistsException nee) {
                String workerNodePath = createNewWorkerNode();
                currRole = new DistWorker(zk, groupNum, workerNodePath);
            }

            currRole.start();
        } catch (UnknownHostException uhe) {
            logger.error(
                "DistProcess with role {} failed to initialize, shutting down",
                currRole instanceof DistManager ? "manager" : "worker"
            );
            // What else will you need if this was a worker process?
            // - delete worker znode by closing zk connection
            // - shutdown worker role
            shutdown();
        } catch (KeeperException ke) {
            logger.error(ke.toString());
        } catch (InterruptedException ie) {
            logger.error(ie.toString());
        }

        logger.info(
            "DISTAPP : Role : I will be functioning as {}",
            currRole instanceof DistManager ? "manager" : "worker"
        );
    }

    private void runForManager()
        throws UnknownHostException, KeeperException, InterruptedException {
        // Try to create an ephemeral node to be the manager...
        zk.create(
            String.format("/dist%d/manager", groupNum),
            pinfo.getBytes(),
            Ids.OPEN_ACL_UNSAFE,
            CreateMode.EPHEMERAL
        );
    }

    private String createNewWorkerNode()
        throws InterruptedException, KeeperException {
        String workerNodePath = zk.create(
            String.format("/dist%d/workers/worker-", groupNum),
            new byte[0],
            Ids.OPEN_ACL_UNSAFE,
            CreateMode.EPHEMERAL_SEQUENTIAL
        );

        return workerNodePath;
    }

    private void watchConnection(WatchedEvent e) {
        logger.info("DISTAPP : Event received : {}", e);

        if (
            e.getType() == Watcher.Event.EventType.None // This seems to be the event type associated with connections.
        ) {
            // Once we are connected, do our intialization stuff.
            if (
                e.getPath() == null &&
                e.getState() == Watcher.Event.KeeperState.SyncConnected &&
                initialized == false
            ) {
                initialize();
                initialized = true;
            }
        }
    }

    private void shutdown() {
        logger.info("Shutting down DistProcess...");

        try {
            if (currRole != null) {
                currRole.shutdown();
            }

            if (zk != null) {
                zk.close();
            }
        } catch (Exception e) {
            logger.error("Error while shutting down: {}", e.toString());
        }
    }

    public static void main(String args[]) throws Exception {
        String zkHost = System.getenv("ZKSERVER");
        Integer groupNum = Integer.parseInt(System.getenv("GROUP_NUM"));

        DistProcess dt = new DistProcess(zkHost, groupNum);
        dt.startProcess();

        // Replace this with an approach that will make sure that the process is
        // up and running forever.
        // Thread.sleep(22_000_000);

        // this must work...
        //
        // register shutdown hook
        Runtime.getRuntime().addShutdownHook(
            new Thread(() -> {
                dt.shutdown();
            })
        );

        // sleep forever
        while (true) {
            Thread.sleep(Long.MAX_VALUE);
        }
    }
}
