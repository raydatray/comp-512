package middleware.tcp;

import client.tcp.TCPResourceManagerClientProxy;
import interfaces.IResourceManagerService;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import middleware.common.Middleware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.tcp.TCPResourceManagerAdapter;
import utils.TCPUtils;

public final class TCPMiddleware {

    private static final Logger logger = LoggerFactory.getLogger(
        TCPMiddleware.class
    );
    private static String middlewareName = "Middleware";
    private static ExecutorService threadPool = Executors.newCachedThreadPool();

    private static String upstreamFlightHost = "localhost";
    private static Integer upstreamFlightPort = 5002;

    private static String upstreamCarHost = "localhost";
    private static Integer upstreamCarPort = 5003;

    private static String upstreamRoomHost = "localhost";
    private static Integer upstreamRoomPort = 5004;

    public static void main(String[] args) {
        try {
            IResourceManagerService service = buildMiddlewareService();
            TCPResourceManagerAdapter adapter = new TCPResourceManagerAdapter(
                service
            );

            Integer middlwarePort = 1122;
            ServerSocket serverSocket = new ServerSocket(middlwarePort);

            logger.info(
                middlewareName +
                    ": socket on port " +
                    serverSocket.getLocalPort() +
                    " open, listening for requests..."
            );

            TCPUtils.addShutdownHook(serverSocket, middlewareName);
            TCPUtils.acceptConnections(threadPool, serverSocket, adapter);
        } catch (Exception e) {
            logger.error("uncaught exception, stack trace follows");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static IResourceManagerService buildMiddlewareService()
        throws IOException {
        IResourceManagerService flightRM = connectTCPBackend(
            upstreamFlightHost,
            upstreamFlightPort,
            "Flights"
        );

        IResourceManagerService carRM = connectTCPBackend(
            upstreamCarHost,
            upstreamCarPort,
            "Cars"
        );

        IResourceManagerService roomRM = connectTCPBackend(
            upstreamRoomHost,
            upstreamRoomPort,
            "Rooms"
        );

        return new Middleware(middlewareName, flightRM, carRM, roomRM);
    }

    private static IResourceManagerService connectTCPBackend(
        String host,
        Integer port,
        String name
    ) throws IOException {
        Socket socket = TCPUtils.waitForConnection(host, port, name);

        logger.info("connect backend {} at {}:{}", name, host, port);
        return new TCPResourceManagerClientProxy(socket);
    }
}
