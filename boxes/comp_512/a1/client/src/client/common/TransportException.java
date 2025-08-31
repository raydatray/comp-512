package client.common;

/**
 * A minimal, transport-agnostic runtime exception used by client-side proxies
 * (RMI/TCP) to signal network/transport failures without leaking transport-specific
 * checked exceptions into the CLI layer.
 *
 * This intentionally keeps logic simple for a school assignment: just a type that
 * carries a message/cause and prints cleanly.
 */
public class TransportException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public TransportException(String message) {
        super(message);
    }

    public TransportException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransportException(Throwable cause) {
        super(cause == null ? null : cause.toString(), cause);
    }

    @Override
    public String toString() {
        String msg = getMessage();
        return (
            "TransportException" +
            (msg == null || msg.isEmpty() ? "" : (": " + msg))
        );
    }
}
