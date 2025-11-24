import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.UnknownHostException;
import java.util.List;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// You may have to add other interfaces such as for threading, etc., as needed.
// This class will contain the logic for both your manager process as well as the worker processes.
//  Make sure that the callbacks and watch do not conflict between your manager's logic and worker's logic.
//		This is important as both the manager and worker may need same kind of callbacks and could result
//			with the same callback functions.
//	For simplicity, so far all the code in a single class (including the callbacks).
//		You are free to break it apart into multiple classes, if that is your programming style or helps
//		you manage the code more modularly.
//	REMEMBER !! Managers and Workers are also clients of ZK and the ZK client library is single thread - Watches & CallBacks should not be used for time consuming tasks.
//		In particular, if the process is a worker, Watches & CallBacks should only be used to assign the "work" to a separate thread inside your program.
public class DistProcess implements Watcher, AsyncCallback.ChildrenCallback {

    private static Integer TIMEOUT = 10000;
    private static Logger logger = LoggerFactory.getLogger(DistProcess.class);

    private ZooKeeper zk;
    private String zkServer;
    private String pinfo;
    private Integer id;

    private Boolean isManager = false;
    private Boolean initialized = false;

    DistProcess(String zkhost, Integer groupNum) {
        zkServer = zkhost;
        pinfo = ManagementFactory.getRuntimeMXBean().getName(); // get process id
        id = groupNum;

        logger.info("DISTAPP : ZK Connection information : " + zkServer);
        logger.info("DISTAPP : Process information : " + pinfo);
    }

    void startProcess()
        throws IOException, UnknownHostException, KeeperException, InterruptedException {
        zk = new ZooKeeper(zkServer, TIMEOUT, this);
    }

    void initialize() {
        try {
            runForManager(); // See if you can become the manager (i.e, no other manager exists)
            isManager = true;
            getTasks(); // Install monitoring on any new tasks that will be created.
            // TODO monitor for worker tasks?
        } catch (NodeExistsException nee) {
            isManager = false;
        } catch (UnknownHostException uhe) {
            // TODO: What else will you need if this was a worker process?
            logger.error(uhe.toString());
        } catch (KeeperException ke) {
            logger.error(ke.toString());
        } catch (InterruptedException ie) {
            logger.error(ie.toString());
        }

        logger.info(
            "DISTAPP : Role : I will be functioning as {}",
            isManager ? "manager" : "worker"
        );
    }

    void getTasks() {
        zk.getChildren(String.format("/dist%d/tasks", id), this, this, null);
    }

    void runForManager()
        throws UnknownHostException, KeeperException, InterruptedException {
        // Try to create an ephemeral node to be the manager, put the hostname and pid
        // of this process as the data.
        // This is an example of Synchronous API invocation as the function waits for
        // the execution and no callback is involved..
        zk.create(
            String.format("/dist%d/manager", id),
            pinfo.getBytes(),
            Ids.OPEN_ACL_UNSAFE,
            CreateMode.EPHEMERAL
        );
    }

    public void process(WatchedEvent e) {
        // Get watcher notifications.

        // !! IMPORTANT !!
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

        // Manager should be notified if any new znodes are added to tasks.
        if (
            e.getType() == Watcher.Event.EventType.NodeChildrenChanged &&
            e.getPath().equals(String.format("/dist%d/tasks", id))
        ) {
            // There has been changes to the children of the node.
            // We are going to re-install the Watch as well as request for the list of the
            // children.
            getTasks();
        }
    }

    // Asynchronous callback that is invoked by the zk.getChildren request.
    public void processResult(
        int rc,
        String path,
        Object ctx,
        List<String> children
    ) {
        // !! IMPORTANT !!
        // Do not perform any time consuming/waiting steps here
        // including in other functions called from here.
        // Your will be essentially holding up ZK client library
        // thread and you will not get other notifications.
        // Instead include another thread in your program logic that
        // does the time consuming "work" and notify that thread from here.

        // This logic is for manager !!
        // Every time a new task znode is created by the client, this will be invoked.

        // TODO: Filter out and go over only the newly created task znodes.
        // Also have a mechanism to assign these tasks to a "Worker" process.
        // The worker must invoke the "compute" function of the Task send by the client.
        // What to do if you do not have a free worker process?
        logger.info("DISTAPP : processResult : {} : {} : {}", rc, path, ctx);
        for (String child : children) {
            logger.debug(child);
        }
    }

    public static void main(String args[]) throws Exception {
        String zkHost = System.getenv("ZKSERVER");
        Integer groupNum = Integer.parseInt(System.getenv("GROUP_NUM"));

        DistProcess dt = new DistProcess(zkHost, groupNum);
        dt.startProcess();

        // TODO: Replace this with an approach that will make sure that the process is
        // up and running forever.
        Thread.sleep(20000);
    }
}
