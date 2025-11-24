package manager.callback;

import java.util.List;
import manager.DistManager;
import org.apache.zookeeper.AsyncCallback;

public class WorkersChildrenCallback implements AsyncCallback.ChildrenCallback {

    private DistManager manager;

    public WorkersChildrenCallback(DistManager manager) {
        this.manager = manager;
    }

    public void processResult(
        int rc,
        String path,
        Object ctx,
        List<String> children
    ) {
        manager.handleWorkersChildrenChange(children);
    }
}
