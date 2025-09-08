package middleware.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.tcp.TCPResourceManagerClientProxy;
import interfaces.IResourceManagerService;
import middleware.common.Middleware;
import server.common.ResourceManager;
import server.tcp.TCPResourceManagerAdapter;
import utils.TCPUtils;

public final class TCPMiddleware {
    private static final Logger logger = LoggerFactory.getLogger(TCPMiddleware.class);
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
            TCPResourceManagerAdapter adapter = new TCPResourceManagerAdapter(service);

            Integer middlwarePort = 1099;
            ServerSocket serverSocket = new ServerSocket(middlwarePort);

            logger.info(middlewareName + ": socket on port " + serverSocket.getLocalPort()
                    + " open, listening for requests...");

            TCPUtils.addShutdownHook(serverSocket, middlewareName);
            TCPUtils.acceptConnections(threadPool, serverSocket, adapter);
        } catch (Exception e) {
            logger.error("uncaught exception, stack trace follows");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static IResourceManagerService buildMiddlewareService() throws IOException {
        IResourceManagerService flightRM = new TCPResourceManagerClientProxy(upstreamFlightHost, upstreamFlightPort);
        IResourceManagerService carRM = new TCPResourceManagerClientProxy(upstreamCarHost, upstreamCarPort);
        IResourceManagerService roomRM = new TCPResourceManagerClientProxy(upstreamRoomHost, upstreamRoomPort);

        ResourceManager customerRM = new ResourceManager("Customers");

        return new Middleware(middlewareName, flightRM, carRM, roomRM, customerRM);
    }
}
