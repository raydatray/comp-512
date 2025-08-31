package middleware.rmi;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.rmi.RMIResourceManagerClientProxy;
import interfaces.IRMIResourceManager;
import interfaces.IResourceManagerService;
import middleware.common.Middleware;
import server.common.ResourceManager;
import server.rmi.RMIResourceManagerAdapter;
import utils.RMIUtils;

public final class RMIMiddleware {
    private static final Logger logger = LoggerFactory.getLogger(RMIMiddleware.class);

    private static String s_upstreamHost = "localhost";
    private static Integer s_upstreamPort = 1099;
    private static String s_upstreamName = "Middleware";
    private static String s_rmiPrefix = "group_xx_";

    private static String s_backendHost = "localhost";
    private static Integer s_backendPort = 1099;

    private static String s_flightName = "Flights";
    private static String s_carName = "Cars";
    private static String s_roomName = "Rooms";

    public static void main(String[] args) {
        try {
            IResourceManagerService service = buildMiddlewareService();
            IRMIResourceManager remote = exportService(service);

            Registry registry = RMIUtils.getOrCreateRegistry(s_backendHost, s_backendPort);
            registry.rebind(s_rmiPrefix + s_upstreamName, remote);
            RMIUtils.addShutdownUnbindHook(registry, s_rmiPrefix + s_upstreamName);

            logger.info(
                s_upstreamName +
                " resource manager server ready and bound to " +
                s_rmiPrefix +
                s_upstreamName
            );
        } catch (Exception e) {
            logger.error("uncaught exception, stack trace follows");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static IResourceManagerService buildMiddlewareService() {
        IResourceManagerService flightRM = connectRMIBackend(s_upstreamHost, s_backendPort, s_rmiPrefix, s_flightName);
        IResourceManagerService carRM = connectRMIBackend(s_upstreamHost, s_backendPort, s_rmiPrefix, s_carName);
        IResourceManagerService roomRM = connectRMIBackend(s_upstreamHost, s_backendPort, s_rmiPrefix, s_roomName);

        ResourceManager customerRM = new ResourceManager("Customers");

        return new Middleware(s_upstreamName, flightRM, carRM, roomRM, customerRM);
    }

    private static IResourceManagerService connectRMIBackend(String host, Integer port, String prefix, String bindName) {
        String fullName = prefix + bindName;
        IRMIResourceManager stub = RMIUtils.waitForLookup(host, port, bindName);

        logger.info("connected backend {} at {}:{}", fullName, host, port);
        return new RMIResourceManagerClientProxy(stub);
    }

    private static IRMIResourceManager exportService(IResourceManagerService service) throws RemoteException {
        RMIResourceManagerAdapter adapter = new RMIResourceManagerAdapter(service);
        return (IRMIResourceManager) UnicastRemoteObject.exportObject(adapter, 0);
    }

}
