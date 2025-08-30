package client.rmi;

import client.common.Client;
import interfaces.IRMIResourceManager;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIClient extends Client {

    private static String s_serverHost = "localhost";
    // recommended to hange port last digits to your group number
    private static Integer s_serverPort = 1099;
    private static String s_serverName = "Server";

    // TODO: ADD YOUR GROUP NUMBER TO COMPILE
    private static String s_rmiPrefix = "group_xx_";

    public static void main(String args[]) {
        if (args.length > 0) {
            s_serverHost = args[0];
        }
        if (args.length > 1) {
            s_serverName = args[1];
        }
        if (args.length > 2) {
            System.err.println(
                (char) 27 +
                "[31;1mClient exception: " +
                (char) 27 +
                "[0mUsage: java client.RMIClient [server_hostname [server_rmiobject]]"
            );
            System.exit(1);
        }

        // Get a reference to the RMIRegister
        try {
            RMIClient client = new RMIClient();
            client.connectServer();
            client.start();
        } catch (Exception e) {
            System.err.println(
                (char) 27 +
                "[31;1mClient exception: " +
                (char) 27 +
                "[0mUncaught exception"
            );
            e.printStackTrace();
            System.exit(1);
        }
    }

    public RMIClient() {
        super();
    }

    public void connectServer() {
        connectServer(s_serverHost, s_serverPort, s_serverName);
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
                        (IRMIResourceManager) registry.lookup(
                            s_rmiPrefix + name
                        );
                    m_resourceManager = new RMIResourceManagerClientProxy(stub);
                    System.out.println(
                        "Connected to '" +
                        name +
                        "' server [" +
                        server +
                        ":" +
                        port +
                        "/" +
                        s_rmiPrefix +
                        name +
                        "]"
                    );
                    break;
                } catch (NotBoundException | RemoteException e) {
                    if (first) {
                        System.out.println(
                            "Waiting for '" +
                            name +
                            "' server [" +
                            server +
                            ":" +
                            port +
                            "/" +
                            s_rmiPrefix +
                            name +
                            "]"
                        );
                        first = false;
                    }
                }
                Thread.sleep(500);
            }
        } catch (Exception e) {
            System.err.println(
                (char) 27 +
                "[31;1mServer exception: " +
                (char) 27 +
                "[0mUncaught exception"
            );
            e.printStackTrace();
            System.exit(1);
        }
    }
}
