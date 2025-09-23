package client.rmi;

import client.common.Client;
import interfaces.IRMIResourceManager;
import interfaces.IResourceManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.RMIUtils;

public class RMIClient {

    private static final Logger logger = LoggerFactory.getLogger(
        RMIClient.class
    );

    private Client client;

    private static String serverHost = "localhost";
    // recommended to hange port last digits to your group number
    private static Integer serverPort = 1022;
    private static String serverName = "Server";
    private static String rmiPrefix = "group_22_";

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

        // Get a reference to the RMIRegister
        try {
            RMIClient rmiClient = new RMIClient();
            rmiClient.start();
        } catch (Exception e) {
            logger.error(
                "Server exception: Uncaught exception, stack trace follows"
            );
            e.printStackTrace();
            System.exit(1);
        }
    }

    public RMIClient() {
        IResourceManagerService resourceManager = createConnection();
        this.client = new Client(resourceManager, this::connectServer);
    }

    public void connectServer() {
        IResourceManagerService newResourceManager = createConnection();
        this.client = new Client(newResourceManager, this::connectServer);
    }

    public void start() {
        this.client.start();
    }

    private IResourceManagerService createConnection() {
        String fullBindName = rmiPrefix + serverName;
        IRMIResourceManager stub = RMIUtils.waitForLookup(
            serverHost,
            serverPort,
            fullBindName
        );

        return new RMIResourceManagerClientProxy(stub);
    }
}
