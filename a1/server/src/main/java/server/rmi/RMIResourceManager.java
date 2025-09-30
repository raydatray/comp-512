package server.rmi;

import interfaces.IRMIResourceManager;
import interfaces.IResourceManagerService;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.common.*;
import utils.RMIUtils;

public class RMIResourceManager extends ResourceManager {

    private static final Logger logger = LoggerFactory.getLogger(
        RMIResourceManager.class
    );
    private static String registryHost = "localhost";
    private static Integer registryPort = 1122;
    private static String serverName = "Server";
    private static String rmiPrefix = "group_22_";

    public static void main(String args[]) {
        if (args.length > 0) {
            serverName = args[0];
        }
        if (args.length > 1) {
            logger.error(
                "Server Exception: Usage: java server.rmi.RMIResourceManager [rm_object]"
            );

            System.exit(1);
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

            final Registry registry = RMIUtils.getOrCreateRegistry(
                registryHost,
                registryPort
            );
            registry.rebind(rmiPrefix + serverName, stub);
            RMIUtils.addShutdownUnbindHook(registry, rmiPrefix + serverName);

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
