package client.rmi;

import client.common.Client;
import interfaces.IRMIResourceManager;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RMIClient extends Client {

    private static final Logger logger = LoggerFactory.getLogger(
        RMIClient.class
    );

    private static String serverHost = "localhost";
    // recommended to hange port last digits to your group number
    private static Integer serverPort = 1099;
    private static String serverName = "Server";

    // TODO: ADD YOUR GROUP NUMBER TO COMPILE
    private static String rmiPrefix = "group_xx_";

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
            RMIClient client = new RMIClient();
            client.connectServer();
            client.start();
        } catch (Exception e) {
            logger.error(
                "Server exception: Uncaught exception, stack trace follows"
            );
            e.printStackTrace();
            System.exit(1);
        }
    }

    public RMIClient() {
        super();
    }

    public void connectServer() {
        connectServer(serverHost, serverPort, serverName);
    }

    public void connectServer(String server, Integer port, String name) {
        try {
            Boolean first = true;
            while (true) {
                try {
                    Registry registry = LocateRegistry.getRegistry(
                        server,
                        port
                    );
                    IRMIResourceManager stub =
                        (IRMIResourceManager) registry.lookup(rmiPrefix + name);
                    resourceManager = new RMIResourceManagerClientProxy(stub);
                    logger.info(
                        "Connected to '" +
                            name +
                            "' server [" +
                            server +
                            ":" +
                            port +
                            "/" +
                            rmiPrefix +
                            name +
                            "]"
                    );
                    break;
                } catch (NotBoundException | RemoteException e) {
                    if (first) {
                        logger.info(
                            "Waiting for '" +
                                name +
                                "' server [" +
                                server +
                                ":" +
                                port +
                                "/" +
                                rmiPrefix +
                                name +
                                "]"
                        );
                        first = false;
                    }
                }
                Thread.sleep(500);
            }
        } catch (Exception e) {
            logger.error(
                "Server exception: Uncaught exception, stack trace follows"
            );
            e.printStackTrace();
            System.exit(1);
        }
    }
}
