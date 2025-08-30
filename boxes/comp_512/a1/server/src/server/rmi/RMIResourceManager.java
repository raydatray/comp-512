package server.rmi;

import interfaces.IRMIResourceManager;
import interfaces.IResourceManagerService;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import server.common.*;

public class RMIResourceManager extends ResourceManager {

    private static String s_serverName = "Server";
    private static String s_rmiPrefix = "group_xx_";

    public static void main(String args[]) {
        if (args.length > 0) {
            s_serverName = args[0];
        }

        try {
            IResourceManagerService service = new ResourceManager(s_serverName);
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
                temp_registry = LocateRegistry.createRegistry(1099);
            } catch (RemoteException e) {
                temp_registry = LocateRegistry.getRegistry(1099);
            }

            final Registry registry = temp_registry;
            registry.rebind(s_rmiPrefix + s_serverName, stub);

            Runtime.getRuntime().addShutdownHook(
                    new Thread() {
                        public void run() {
                            try {
                                registry.unbind(s_rmiPrefix + s_serverName);
                                System.out.println(
                                    "'" +
                                    s_serverName +
                                    "' resource manager unbound"
                                );
                            } catch (Exception e) {
                                System.err.println(
                                    (char) 27 +
                                    "[31;1mServer exception: " +
                                    (char) 27 +
                                    "[0mUncaught exception"
                                );
                                e.printStackTrace();
                            }
                        }
                    }
                );
            System.out.println(
                "'" +
                s_serverName +
                "' resource manager server ready and bound to '" +
                s_rmiPrefix +
                s_serverName +
                "'"
            );
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

    public RMIResourceManager(String name) {
        super(name);
    }
}
