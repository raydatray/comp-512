package worker.watcher;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import worker.DistWorker;

public class WorkerWatcher implements Watcher {

    private DistWorker worker;

    public WorkerWatcher(DistWorker worker) {
        this.worker = worker;
    }

    public void process(WatchedEvent event) {
        if (event.getType() == Watcher.Event.EventType.NodeDataChanged) {
            worker.setWatch();
        }
    }
}
