package tcp.responses;

import java.io.Serializable;

public record TCPIntegerResponseMessage(Integer result, String errorString) implements Serializable {
}
