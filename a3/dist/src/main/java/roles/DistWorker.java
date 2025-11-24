package roles;

import interfaces.DistTask;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import roles.interfaces.DistRole;
import serde.DistSerde;

public class DistWorker implements DistRole {

    private static Logger logger = LoggerFactory.getLogger(DistWorker.class);
    private ExecutorService executor;

    private ZooKeeper zk;
    private Integer groupNum;
    private String workerNodePath;

    // Watchers
    private Watcher workerWatcher = event -> {
        if (event.getType() == Watcher.Event.EventType.NodeDataChanged) {
            watchWorker();
        }
    };

    // Callbacks
    private AsyncCallback.DataCallback onWorkerDataChangeCb = (
        int rc,
        String path,
        Object ctx,
        byte[] data,
        Stat stat
    ) -> {
        handleWorkerDataChange(data);
    };

    private AsyncCallback.DataCallback onReceiveTaskDataCb = (
        int rc,
        String path,
        Object ctx,
        byte[] data,
        Stat stat
    ) -> {
        String taskNodeName = (String) ctx;
        handleNewTaskData(data, taskNodeName);
    };

    public DistWorker(ZooKeeper zk, Integer groupNum, String workerNodeName) {
        executor = Executors.newSingleThreadExecutor();

        this.zk = zk;
        this.groupNum = groupNum;
        this.workerNodePath = String.format(
            "/dist%d/workers/%s",
            groupNum,
            workerNodeName
        );
    }

    public void setWatch() {
        watchWorker();
    }

    private void watchWorker() {
        // set watch on own node
        zk.getData(workerNodePath, workerWatcher, onWorkerDataChangeCb, null);
        logger.info("Worker set watch to {}", workerNodePath);
    }

    private void handleWorkerDataChange(byte[] workerData) {
        executor.submit(() -> {
            // if data empty, then worker transitioned to idle state
            // if not, it received a new task
            if (workerData.length > 0) {
                String taskNodeName = new String(
                    workerData,
                    StandardCharsets.UTF_8
                );

                logger.info(
                    "Worker at `{}` received task `{}`",
                    workerNodePath,
                    taskNodeName
                );

                // fetch data from task znode
                String taskNodePath = String.format(
                    "/dist%d/tasks/%s",
                    groupNum,
                    taskNodeName
                );
                zk.getData(
                    taskNodePath,
                    null,
                    onReceiveTaskDataCb,
                    taskNodeName
                ); // pass task name as ctx

                logger.info(
                    "Fetching data from task node at `{}`",
                    taskNodePath
                );
            }
        });
    }

    private void handleNewTaskData(byte[] taskData, String taskNodeName) {
        executor.submit(() -> {
            try {
                DistTask task = DistSerde.deserialize(taskData);
                logger.info("Worker computing task...");
                task.compute();
                logger.info("Worker done with task...");
                byte[] resultData = DistSerde.serialize(task);

                String resultNodePath = String.format(
                    "/dist%d/tasks/%s/result",
                    groupNum,
                    taskNodeName
                );
                zk.create(
                    resultNodePath,
                    resultData,
                    Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT
                );

                logger.info(
                    "Creating result node at path `{}`",
                    resultNodePath
                );

                // set own worker data back to empty byte array
                zk.setData(workerNodePath, new byte[0], -1);

                logger.info(
                    "Setting data at {} to empty byte array, returning to idle status",
                    workerNodePath
                );
            } catch (NodeExistsException nee) {
                logger.error(nee.toString());
            } catch (KeeperException ke) {
                logger.error(ke.toString());
            } catch (InterruptedException ie) {
                logger.error(ie.toString());
            } catch (IOException io) {
                logger.error(io.toString());
            } catch (ClassNotFoundException cne) {
                logger.error(cne.toString());
            }
        });
    }

    public void onShutdown() {
        executor.shutdown();
    }
}
