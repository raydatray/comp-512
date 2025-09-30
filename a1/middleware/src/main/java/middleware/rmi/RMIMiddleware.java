package middleware.rmi;

import client.rmi.RMIResourceManagerClientProxy;
import interfaces.IRMIResourceManager;
import interfaces.IResourceManagerService;
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

    private static String upstreamFlightHost = "localhost";
    private static String flightName = "Flights";

    private static String upstreamCarHost = "localhost";
    private static String carName = "Cars";

    private static String upstreamRoomHost = "localhost";
    private static String roomName = "Rooms";

    private static String registryHost = "localhost";
    private static Integer registryPort = 1122;
    private static String serverName = "Middleware";
    private static String rmiPrefix = "group_22_";

    public static void main(String[] args) {
        if (args.length > 0) {
            upstreamFlightHost = args[0];
        }
        if (args.length > 1) {
            upstreamCarHost = args[1];
        }
        if (args.length > 2) {
            upstreamRoomHost = args[2];
        }
        if (args.length > 3) {
            logger.error(
                "Middleware Exception: Usage: java middleware.rmi.RMIMiddleware [flight_rm_host] [car_rm_host] [room_rm_host]"
            );

            System.exit(1);
        }

        try {
            IResourceManagerService service = buildMiddlewareService();
            RMIResourceManagerAdapter adapter = new RMIResourceManagerAdapter(
                service
            );
            IRMIResourceManager remote =
                (IRMIResourceManager) UnicastRemoteObject.exportObject(
                    adapter,
                    0
                );

            Registry registry = RMIUtils.getOrCreateRegistry(
                registryHost,
                registryPort
            );
            registry.rebind(rmiPrefix + serverName, remote);
            RMIUtils.addShutdownUnbindHook(registry, rmiPrefix + serverName);

            logger.info(
                serverName +
                    " resource manager server ready and bound to " +
                    rmiPrefix +
                    serverName
            );
        } catch (Exception e) {
            logger.error("uncaught exception, stack trace follows");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static IResourceManagerService buildMiddlewareService() {
        IResourceManagerService flightRM = connectRMIBackend(
            upstreamFlightHost,
            registryPort,
            rmiPrefix,
            flightName
        );
        IResourceManagerService carRM = connectRMIBackend(
            upstreamCarHost,
            registryPort,
            rmiPrefix,
            carName
        );
        IResourceManagerService roomRM = connectRMIBackend(
            upstreamRoomHost,
            registryPort,
            rmiPrefix,
            roomName
        );

        return new Middleware(serverName, flightRM, carRM, roomRM);
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
}
