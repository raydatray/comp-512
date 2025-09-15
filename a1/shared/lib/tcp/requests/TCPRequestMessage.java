package tcp.requests;

import interfaces.ITCPRequestPayload;
import java.io.Serializable;

public record TCPRequestMessage<T extends ITCPRequestPayload>(
    T payload
) implements Serializable {}
