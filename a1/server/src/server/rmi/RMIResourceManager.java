package server.rmi;

import interfaces.IRMIResourceManager;
import interfaces.IResourceManagerService;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.common.*;

public class RMIResourceManager extends ResourceManager {

    private static final Logger logger = LoggerFactory.getLogger(
        RMIResourceManager.class
    );
    private static String serverName = "Server";
    private static String rmiPrefix = "group_22_";

    public static void main(String args[]) {
        if (args.length > 0) {
            serverName = args[0];
        }

        try {
            IResourceManagerService service = new ResourceManager(serverName);
            RMIResourceManagerAdapter adapter = new RMIResourceManagerAdapter(
                service
            );
            IRMIResourceManager stub =
                (IRMIResourceManager) UnicastRemoteObject.exportObject(
                    adapter,
                    0
                );

            Registry temp_registry;
            try {
                temp_registry = LocateRegistry.createRegistry(1122);
            } catch (RemoteException e) {
                temp_registry = LocateRegistry.getRegistry(1122);
            }

            final Registry registry = temp_registry;
            registry.rebind(rmiPrefix + serverName, stub);

            Runtime.getRuntime().addShutdownHook(
                new Thread() {
                    public void run() {
                        try {
                            registry.unbind(rmiPrefix + serverName);
                            logger.info(
                                serverName + ": resource manager unbound"
                            );
                        } catch (Exception e) {
                            logger.warn(
                                "Server exception: uncaught exception, stack trace follows"
                            );
                            e.printStackTrace();
                        }
                    }
                }
            );
            logger.info(
                serverName +
                    " resource manager server ready and bound to " +
                    rmiPrefix +
                    serverName
            );
        } catch (Exception e) {
            logger.warn(
                "Server exception: uncaught exception, stack trace follows"
            );
            e.printStackTrace();
            System.exit(1);
        }
    }

    public RMIResourceManager(String name) {
        super(name);
    }
}
