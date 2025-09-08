package server.tcp;

import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import interfaces.IResourceManagerService;
import server.common.ResourceManager;
import utils.TCPUtils;

public class TCPResourceManager extends ResourceManager {
    private static final Logger logger = LoggerFactory.getLogger(
            TCPResourceManager.class);
    private static String serverName = "Server";
    private static ExecutorService threadPool = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        if (args.length > 0) {
            serverName = args[0];
        }
        if (args.length > 1) {
            logger.error("Server Exception: Usage: java server.tcp.TCPResourceManager [server_rmi_object]");
            System.exit(1);
        }

        try {
            IResourceManagerService service = new ResourceManager(serverName);
            TCPResourceManagerAdapter adapter = new TCPResourceManagerAdapter(service);
            int serverPort;
            switch (serverName) {
                case "Flights" -> serverPort = 5002;
                case "Cars" -> serverPort = 5003;
                case "Rooms" -> serverPort = 5004;
                default -> {
                    throw new IllegalArgumentException("Invalid [server_rmi_object]: " + serverName);
                }
            }

            ServerSocket serverSocket = new ServerSocket(serverPort);
            logger.info(serverName + ": socket on port " + serverSocket.getLocalPort()
                    + " open, listening for requests...");

            TCPUtils.addShutdownHook(serverSocket, serverName);
            TCPUtils.acceptConnections(threadPool, serverSocket, adapter);
        } catch (Exception e) {
            logger.error(
                    "Server exception: uncaught exception, stack trace follows");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public TCPResourceManager(String name) {
        super(name);
    }
}
