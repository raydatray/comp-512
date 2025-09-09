package tcp.responses;

import java.io.Serializable;

public record TCPBooleanResponseMessage(Boolean ok, String errorString) implements Serializable {
}
