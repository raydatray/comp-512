package roles;

import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import roles.interfaces.DistRole;

public class DistManager implements DistRole {

    private static Logger logger = LoggerFactory.getLogger(DistManager.class);
    private ExecutorService executor;

    private ZooKeeper zk;
    private Integer groupNum;
    private String tasksNodePath;
    private String workersNodePath;

    private Set<String> seenWorkers;
    private Queue<String> idleWorkers;
    private Set<String> busyWorkers;
    private Set<String> seenTasks;
    private Queue<String> pendingTasks;

    // Watchers
    private Watcher tasksWatcher = event -> {
        if (
            event.getType() == Watcher.Event.EventType.NodeChildrenChanged &&
            event.getPath().equals(tasksNodePath)
        ) {
            watchTasks();
        }
    };

    private Watcher workersWatcher = event -> {
        if (
            event.getType() == Watcher.Event.EventType.NodeChildrenChanged &&
            event.getPath().equals(workersNodePath)
        ) {
            watchWorkers();
        }
    };

    private Watcher workerDataWatcher = event -> {
        if (event.getType() == Watcher.Event.EventType.NodeDataChanged) {
            String workerNodePath = event.getPath();
            watchWorkerData(workerNodePath);
        }
    };

    // Callbacks
    private AsyncCallback.ChildrenCallback onTasksChildrenChangeCb = (
        int rc,
        String path,
        Object ctx,
        List<String> children
    ) -> {
        handleTasksChildrenChange(children);
    };

    private AsyncCallback.ChildrenCallback onWorkersChildrenChangeCb = (
        int rc,
        String path,
        Object ctx,
        List<String> children
    ) -> {
        handleWorkersChildrenChange(children);
    };

    private AsyncCallback.DataCallback onWorkerDataChangeCb = (
        int rc,
        String path,
        Object ctx,
        byte[] data,
        Stat stat
    ) -> {
        handleWorkerDataChange(data, path);
    };

    public DistManager(ZooKeeper zk, Integer groupNum) {
        executor = Executors.newSingleThreadExecutor();

        this.zk = zk;
        this.groupNum = groupNum;
        tasksNodePath = String.format("/dist%d/tasks", groupNum);
        workersNodePath = String.format("/dist%d/workers", groupNum);

        seenWorkers = ConcurrentHashMap.newKeySet();
        idleWorkers = new LinkedBlockingQueue<>();
        busyWorkers = ConcurrentHashMap.newKeySet();
        seenTasks = ConcurrentHashMap.newKeySet();
        pendingTasks = new LinkedBlockingQueue<>();
    }

    public void start() {
        watchWorkers();
        watchTasks();
    }

    private void watchWorkers() {
        zk.getChildren(
            workersNodePath,
            workersWatcher,
            onWorkersChildrenChangeCb,
            null
        );
        logger.info("Manager set watch to {}", workersNodePath);
    }

    private void watchTasks() {
        zk.getChildren(
            tasksNodePath,
            tasksWatcher,
            onTasksChildrenChangeCb,
            null
        );
        logger.info("Manager set watch to {}", tasksNodePath);
    }

    private void watchWorkerData(String workerNodePath) {
        zk.getData(
            workerNodePath,
            workerDataWatcher,
            onWorkerDataChangeCb,
            null
        );
        logger.info("Manager set watch to worker at {}", workerNodePath);
    }

    private void handleTasksChildrenChange(List<String> tasks) {
        for (String task : tasks) {
            if (!seenTasks.contains(task)) {
                seenTasks.add(task);
                pendingTasks.add(task);

                logger.info("New task `{}` found!", task);
            }
        }

        executor.submit(this::assignTasks);
    }

    private void handleWorkersChildrenChange(List<String> workers) {
        for (String worker : workers) {
            if (!seenWorkers.contains(worker)) {
                seenWorkers.add(worker);
                idleWorkers.add(worker);

                logger.info("New worker `{}` found!", worker);

                // set watch on new worker's data
                String workerNodePath = String.format(
                    "%s/%s",
                    workersNodePath,
                    worker
                );
                watchWorkerData(workerNodePath);
            }
        }

        executor.submit(this::assignTasks);
    }

    private void handleWorkerDataChange(byte[] data, String workerNodePath) {
        String workerNodeName = workerNodePath.substring(
            workerNodePath.lastIndexOf("/") + 1
        );

        // only add to idle workes if worker's current data is empty (indicating idle status)
        // AND
        // worker's previous state was busy
        if (data.length == 0 && busyWorkers.contains(workerNodeName)) {
            idleWorkers.add(workerNodeName);
            busyWorkers.remove(workerNodeName);

            logger.info(
                "Manager added {} to idle workers queue",
                workerNodeName
            );
        }
    }

    private void assignTasks() {
        while (!pendingTasks.isEmpty() && !idleWorkers.isEmpty()) {
            String taskNodeName = pendingTasks.peek();
            String workerNodeName = idleWorkers.peek();

            if (taskNodeName == null) {
                logger.warn("No available task in queue...");
                break;
            }

            if (workerNodeName == null) {
                logger.warn("No idle workers available to execute task...");
                break;
            }

            logger.info(
                "Assigning task " +
                    taskNodeName +
                    " to worker " +
                    workerNodeName
            );

            String workerNodePath = String.format(
                "/dist%d/workers/%s",
                groupNum,
                workerNodeName
            );
            byte[] data = taskNodeName.getBytes();

            try {
                zk.setData(workerNodePath, data, -1);

                // if successful, remove from queues
                pendingTasks.poll();
                idleWorkers.poll();
                // add worker to busy set of workers
                busyWorkers.add(workerNodeName);
            } catch (KeeperException ke) {
                logger.error(ke.toString());
                break;
            } catch (InterruptedException ie) {
                logger.error(ie.toString());
                break;
            }
        }
    }

    public void shutdown() {
        executor.shutdown();
    }
}
