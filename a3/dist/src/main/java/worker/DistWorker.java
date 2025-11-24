package worker;

import interfaces.DistTask;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import role.DistRole;
import serde.DistSerde;
import worker.callback.TaskDataCallback;
import worker.callback.WorkerDataCallback;
import worker.watcher.WorkerWatcher;

public class DistWorker implements DistRole, Runnable {

    private static Logger logger = LoggerFactory.getLogger(DistWorker.class);

    ZooKeeper zk;
    Integer groupNum;
    String workerName;

    WorkerWatcher workerWatcher;
    WorkerDataCallback workerDataCallback;

    TaskDataCallback taskDataCallback;

    private ExecutorService executor;

    public DistWorker(ZooKeeper zk, Integer groupNum, String workerName) {
        this.zk = zk;
        this.groupNum = groupNum;
        this.workerName = workerName;

        workerWatcher = new WorkerWatcher(this);
        workerDataCallback = new WorkerDataCallback(this);

        taskDataCallback = new TaskDataCallback(this);

        executor = Executors.newSingleThreadExecutor();
    }

    public void run() {}

    public void setWatch() {
        // set watch on own node
        String path = String.format("/dist%d/workers/%s", groupNum, workerName);
        zk.getData(path, workerWatcher, workerDataCallback, null);

        logger.info("Worker set watch to {}", path);
    }

    // when worker gets assigned a new task
    public void handleWorkerDataChange(byte[] workerData) {
        executor.submit(() -> {
            // if data is empty byte array, worker is idle
            if (workerData.length > 0) {
                String taskNode = new String(
                    workerData,
                    StandardCharsets.UTF_8
                );

                logger.info(
                    "Worker `{}` received task `{}`",
                    taskNode,
                    workerName
                );

                // fetch znode data from task node
                String path = String.format(
                    "/dist%d/tasks/%s",
                    groupNum,
                    taskNode
                );

                logger.info("Fetching data from task node at `{}`", path);

                zk.getData(path, null, taskDataCallback, taskNode);
            }
        });
    }

    // received task data -> compute and create result node
    public void handleTaskData(byte[] taskData, String taskNode) {
        executor.submit(() -> {
            try {
                // Re-construct our task object.
                DistTask task = DistSerde.deserialize(taskData);

                logger.info("Worker computing task...");
                task.compute();
                logger.info("Worker done with task...");

                // Serialize our Task object back to a byte array
                byte[] resultData = DistSerde.serialize(task);
                // Store it inside the result node.
                String resultPath = String.format(
                    "/dist%d/tasks/%s/result",
                    groupNum,
                    taskNode
                );

                logger.info("Creating result node at path `{}`", resultPath);

                zk.create(
                    resultPath,
                    resultData,
                    Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT
                );

                // set own worker data back to empty byte array
                String workerPath = String.format(
                    "/dist%d/workers/%s",
                    groupNum,
                    workerName
                );
                zk.setData(workerPath, new byte[0], -1);
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
