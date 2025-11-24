package worker.callback;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.data.Stat;
import worker.DistWorker;

public class WorkerDataCallback implements AsyncCallback.DataCallback {

    private DistWorker worker;

    public WorkerDataCallback(DistWorker worker) {
        this.worker = worker;
    }

    public void processResult(
        int rc,
        String path,
        Object ctx,
        byte[] data,
        Stat stat
    ) {
        worker.handleWorkerDataChange(data);
    }
}
