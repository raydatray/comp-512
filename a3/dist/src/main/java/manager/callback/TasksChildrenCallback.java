package manager.callback;

import java.util.List;
import manager.DistManager;
import org.apache.zookeeper.AsyncCallback;

public class TasksChildrenCallback implements AsyncCallback.ChildrenCallback {

    private DistManager manager;

    public TasksChildrenCallback(DistManager manager) {
        this.manager = manager;
    }

    public void processResult(
        int rc,
        String path,
        Object ctx,
        List<String> children
    ) {
        manager.handleTasksChildrenChange(children);
    }
}
