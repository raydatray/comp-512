package manager.watcher;

import manager.DistManager;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

public class TasksWatcher implements Watcher {

    private DistManager manager;
    private String watchPath;

    public TasksWatcher(DistManager manager, String watchPath) {
        this.manager = manager;
        this.watchPath = watchPath;
    }

    public void process(WatchedEvent event) {
        // manager should be notified if any new child znodes are added
        if (
            event.getType() == Watcher.Event.EventType.NodeChildrenChanged &&
            event.getPath().equals(watchPath)
        ) {
            // There has been changes to the children of the node.
            // We are going to re-install the Watch as well as request for the list of the
            // children.
            manager.setTasksWatch();
        }
    }
}
