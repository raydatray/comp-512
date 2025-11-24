package manager.watcher;

import manager.DistManager;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

public class WorkerDataWatcher implements Watcher {

    private DistManager manager;

    public WorkerDataWatcher(DistManager manager) {
        this.manager = manager;
    }

    public void process(WatchedEvent event) {
        if (event.getType() == Watcher.Event.EventType.NodeDataChanged) {
            // parse path to get worker
            String path = event.getPath();
            String worker = path.substring(path.lastIndexOf("/") + 1);

            manager.setWorkerDataWatch(worker);
        }
    }
}
