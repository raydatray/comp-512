package interfaces;

import java.io.Serializable;

public interface DistTask extends Serializable {
    public void compute() throws InterruptedException;
}
