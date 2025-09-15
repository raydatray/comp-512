package utils;

import interfaces.IRMIResourceManager;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RMIUtils {

    private static final Logger logger = LoggerFactory.getLogger(
        RMIUtils.class
    );

    public static IRMIResourceManager waitForLookup(
        String host,
        Integer port,
        String fullBindName
    ) {
        try {
            Boolean first = true;

            while (true) {
                try {
                    Registry registry = LocateRegistry.getRegistry(host, port);
                    return (IRMIResourceManager) registry.lookup(fullBindName);
                } catch (NotBoundException | RemoteException e) {
                    if (first) {
                        logger.info("waiting for backend {}", fullBindName);
                        first = false;
                    }
                }
                Thread.sleep(500);
            }
        } catch (Exception e) {
            logger.error(
                "server exception: uncaught exception, stack trace follows"
            );
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }

    public static void addShutdownUnbindHook(
        Registry registry,
        String fullBindName
    ) {
        Runtime.getRuntime().addShutdownHook(
            new Thread(() -> {
                try {
                    registry.unbind(fullBindName);
                    logger.info("unbound: {}", fullBindName);
                } catch (Exception e) {
                    logger.warn(
                        "Server exception: uncaught exception, stack trace follows"
                    );
                    e.printStackTrace();
                }
            })
        );
    }

    public static Registry getOrCreateRegistry(String host, Integer port)
        throws RemoteException {
        try {
            return LocateRegistry.createRegistry(port);
        } catch (RemoteException e) {
            return LocateRegistry.getRegistry(host, port);
        }
    }
}
