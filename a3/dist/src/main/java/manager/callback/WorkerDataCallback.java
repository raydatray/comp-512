package manager.callback;

import manager.DistManager;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.data.Stat;

public class WorkerDataCallback implements AsyncCallback.DataCallback {

    private DistManager manager;

    public WorkerDataCallback(DistManager manager) {
        this.manager = manager;
    }

    public void processResult(
        int rc,
        String path,
        Object ctx,
        byte[] data,
        Stat stat
    ) {
        String worker = (String) ctx;
        manager.handleWorkerDataChange(data, worker);
    }
}
