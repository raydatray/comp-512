package tcp.responses;

import java.io.Serializable;

public record TCPStringResponseMessage(
    String result,
    String errorString
) implements Serializable {}
