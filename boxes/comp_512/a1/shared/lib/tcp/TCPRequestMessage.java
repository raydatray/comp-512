package tcp;

import interfaces.ITCPRequestPayload;

import java.io.Serializable;

public class TCPRequestMessage<T extends ITCPRequestPayload> implements Serializable {
    private T payload;

    public TCPRequestMessage(T payload) {
        this.payload = payload;
    }

    public T getPayload() {
        return payload;
    }
}
