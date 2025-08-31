package client.common;

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
