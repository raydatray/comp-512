package tcp;

import java.io.Serializable;

public class TCPResponseMessage implements Serializable {
    private Boolean success;
    private String errorString;

    public TCPResponseMessage(Boolean success) {
        this.success = success;
    }

    public Boolean getSuccess() {
        return success;
    }

    public String getErrorString() {
        return errorString;
    }
}
