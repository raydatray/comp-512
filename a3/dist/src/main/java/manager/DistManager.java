package manager;

import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import manager.callback.TasksChildrenCallback;
import manager.callback.WorkerDataCallback;
import manager.callback.WorkersChildrenCallback;
import manager.watcher.TasksWatcher;
import manager.watcher.WorkerDataWatcher;
import manager.watcher.WorkersWatcher;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import role.DistRole;

public class DistManager implements DistRole {

    private static Logger logger = LoggerFactory.getLogger(DistManager.class);

    private ZooKeeper zk;
    private Integer groupNum;

    private TasksWatcher tasksWatcher;
    private TasksChildrenCallback tasksChildrenCallback;

    private WorkersWatcher workersWatcher;
    private WorkersChildrenCallback workersChildrenCallback;

    private WorkerDataWatcher workerDataWatcher;
    private WorkerDataCallback workerDataCallback;

    private ExecutorService executor;

    private Set<String> seenWorkers;
    private Queue<String> idleWorkers;
    private Set<String> seenTasks;
    private Queue<String> pendingTasks;

    public DistManager(ZooKeeper zk, Integer groupNum) {
        this.zk = zk;
        this.groupNum = groupNum;

        String tasksPath = String.format("/dist%d/tasks", groupNum);
        tasksWatcher = new TasksWatcher(this, tasksPath);
        tasksChildrenCallback = new TasksChildrenCallback(this);

        String workersPath = String.format("/dist%d/workers", groupNum);
        workersWatcher = new WorkersWatcher(this, workersPath);
        workersChildrenCallback = new WorkersChildrenCallback(this);

        workerDataWatcher = new WorkerDataWatcher(this);
        workerDataCallback = new WorkerDataCallback(this);

        executor = Executors.newSingleThreadExecutor();

        seenWorkers = new HashSet<>();
        idleWorkers = new LinkedBlockingQueue<>();
        seenTasks = new HashSet<>();
        pendingTasks = new LinkedBlockingQueue<>();
    }

    public void setWatch() {
        setWorkersWatch();
        setTasksWatch();
    }

    public void setWorkersWatch() {
        String path = String.format("/dist%d/workers", groupNum);
        zk.getChildren(path, workersWatcher, workersChildrenCallback, null);

        logger.info("Manager set watch to {}", path);
    }

    public void setWorkerDataWatch(String workerNode) {
        String path = String.format("/dist%d/workers/%s", groupNum, workerNode);
        zk.getData(path, workerDataWatcher, workerDataCallback, workerNode);

        logger.info("Manager set watch to worker at {}", path);
    }

    public void setTasksWatch() {
        String path = String.format("/dist%d/tasks", groupNum);
        zk.getChildren(path, tasksWatcher, tasksChildrenCallback, null);

        logger.info("Manager set watch to {}", path);
    }

    public void handleTasksChildrenChange(List<String> tasks) {
        for (String task : tasks) {
            if (!seenTasks.contains(task)) {
                seenTasks.add(task);
                pendingTasks.add(task);

                logger.info("New task `{}` found!", task);
            }
        }

        executor.submit(this::assignTasks);
    }

    public void handleWorkersChildrenChange(List<String> workers) {
        for (String worker : workers) {
            if (!seenWorkers.contains(worker)) {
                seenWorkers.add(worker);
                idleWorkers.add(worker);

                logger.info("New worker `{}` found!", worker);

                // set watch on new worker's data
                setWorkerDataWatch(worker);
            }
        }

        executor.submit(this::assignTasks);
    }

    public void handleWorkerDataChange(byte[] data, String worker) {
        // worker data indicates idle status
        if (data.length == 0) {
            idleWorkers.add(worker);
        }
    }

    private void assignTasks() {
        while (!pendingTasks.isEmpty() && !idleWorkers.isEmpty()) {
            String task = pendingTasks.peek();
            String worker = idleWorkers.peek();

            if (task == null) {
                logger.warn("No available task in queue...");
                break;
            }

            if (worker == null) {
                logger.warn("No idle workers available to execute task...");
                break;
            }

            logger.info("Assigning task " + task + " to worker " + worker);

            String workerPath = String.format(
                "/dist%d/workers/%s",
                groupNum,
                worker
            );
            byte[] data = task.getBytes();

            try {
                zk.setData(workerPath, data, -1);

                // If successful, remove from queues
                pendingTasks.poll();
                idleWorkers.poll();
            } catch (KeeperException.NoNodeException nne) {
                logger.warn(
                    "Worker {} crashed during task assignment, removing from queue...",
                    worker
                );
                idleWorkers.poll();
            } catch (KeeperException ke) {
                logger.error(ke.toString());
                break;
            } catch (InterruptedException ie) {
                logger.error(ie.toString());
                break;
            }
        }
    }

    public void onShutdown() {
        executor.shutdown();
    }
}
