package middleware.rmi;

import client.rmi.RMIResourceManagerClientProxy;
import interfaces.IRMIResourceManager;
import interfaces.IResourceManagerService;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import middleware.common.Middleware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.rmi.RMIResourceManagerAdapter;
import utils.RMIUtils;

public final class RMIMiddleware {

    private static final Logger logger = LoggerFactory.getLogger(
        RMIMiddleware.class
    );

    private static String upstreamHost = "localhost";
    private static Integer upstreamPort = 1122;
    private static String upstreamName = "Middleware";
    private static String rmiPrefix = "group_22_";

    private static String backendHost = "localhost";
    private static Integer backendPort = 1122;

    private static String flightName = "Flights";
    private static String carName = "Cars";
    private static String roomName = "Rooms";

    public static void main(String[] args) {
        try {
            IResourceManagerService service = buildMiddlewareService();
            IRMIResourceManager remote = exportService(service);

            Registry registry = RMIUtils.getOrCreateRegistry(
                backendHost,
                backendPort
            );
            registry.rebind(rmiPrefix + upstreamName, remote);
            RMIUtils.addShutdownUnbindHook(registry, rmiPrefix + upstreamName);

            logger.info(
                upstreamName +
                    " resource manager server ready and bound to " +
                    rmiPrefix +
                    upstreamName
            );
        } catch (Exception e) {
            logger.error("uncaught exception, stack trace follows");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static IResourceManagerService buildMiddlewareService() {
        IResourceManagerService flightRM = connectRMIBackend(
            upstreamHost,
            backendPort,
            rmiPrefix,
            flightName
        );
        IResourceManagerService carRM = connectRMIBackend(
            upstreamHost,
            backendPort,
            rmiPrefix,
            carName
        );
        IResourceManagerService roomRM = connectRMIBackend(
            upstreamHost,
            backendPort,
            rmiPrefix,
            roomName
        );

        return new Middleware(upstreamName, flightRM, carRM, roomRM);
    }

    private static IResourceManagerService connectRMIBackend(
        String host,
        Integer port,
        String prefix,
        String bindName
    ) {
        String fullName = prefix + bindName;
        IRMIResourceManager stub = RMIUtils.waitForLookup(host, port, fullName);

        logger.info("connected backend {} at {}:{}", fullName, host, port);
        return new RMIResourceManagerClientProxy(stub);
    }

    private static IRMIResourceManager exportService(
        IResourceManagerService service
    ) throws RemoteException {
        RMIResourceManagerAdapter adapter = new RMIResourceManagerAdapter(
            service
        );
        return (IRMIResourceManager) UnicastRemoteObject.exportObject(
            adapter,
            0
        );
    }
}
