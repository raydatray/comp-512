package client.tcp;

import client.common.Client;
import interfaces.IResourceManagerService;
import java.io.IOException;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.TCPUtils;

public class TCPClient {

    private static final Logger logger = LoggerFactory.getLogger(
        TCPClient.class
    );

    private Client client;

    private static String serverHost = "localhost";
    private static Integer serverPort = 1099;
    private static String serverName = "Server";

    public static void main(String args[]) {
        if (args.length > 0) {
            serverHost = args[0];
        }
        if (args.length > 1) {
            serverName = args[1];
        }
        if (args.length > 2) {
            logger.error(
                "Client Exception: Usage: java client.rmi.RMIClient [server_hostname] [server_rmi_object]"
            );

            System.exit(1);
        }

        try {
            TCPClient client = new TCPClient();
            client.start();
        } catch (Exception e) {
            logger.error(
                "Server exception: Uncaught exception, stack trace follows"
            );
            e.printStackTrace();
            System.exit(1);
        }
    }

    public TCPClient() {
        IResourceManagerService resourceManager = createConnection();
        this.client = new Client(resourceManager, this::connectServer);
    }

    public void connectServer() {
        IResourceManagerService resourceManager = createConnection();
        this.client = new Client(resourceManager, this::connectServer);
    }

    public void start() {
        this.client.start();
    }

    private IResourceManagerService createConnection() {
        Socket socket = TCPUtils.waitForConnection(
            serverHost,
            serverPort,
            serverName
        );

        try {
            return new TCPResourceManagerClientProxy(socket);
        } catch (IOException e) {
            // todo: catch and actually do something with this error
            throw new RuntimeException("Failed to create connection", e);
        }
    }
}
