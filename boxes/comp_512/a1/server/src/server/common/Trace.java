package server.common;

public class Trace {
    public static void info(String msg) {
        System.out.println(getThreadID() + " INFO: " + msg);
    }

    public static void warn(String msg) {
        System.out.println(getThreadID() + " WARN: " + msg);
    }

    public static void error(String msg) {
        System.err.println(getThreadID() + " ERROR: " + msg);
    }

    private static String getThreadID() {
        String s = Thread.currentThread().getName();

        // Shorten
        // "RMI TCP Connection(x)-hostname/99.99.99.99"
        // to
        // "RMI TCP Cx(x)"
        if (s.startsWith("RMI TCP Connection(")) {
            return "RMI Cx" + s.substring(s.indexOf('('), s.indexOf(')')) + ")";
        }
        return s;
    }
}
