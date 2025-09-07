package middleware.rmi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import interfaces.IRMIResourceManager;
import interfaces.IResourceManagerService;
import middleware.common.Middleware;

public final class RMIMiddleware {
    private static final Logger logger = LoggerFactory.getLogger(RMIMiddleware.class);

    String s_upstreamHost = "localhost";
    Integer s_upstreamPort = 1099;
    String s_upstreamName = "Middleware";
    String s_rmiPrefix = "group_xx_";

    String s_backendHost = "localhost";
    Integer s_backendPort = 1099;

    String s_flightName = "Flights";
    String s_carName = "Cars";
    String s_roomName = "Rooms";

    public static void main(String[] args) {

    }

    private static IResourceManagerService buildMiddlewareService() {

    }

    private static IResourceManagerService connectRMIBackend(String host, Integer port, String prefix, String bindName) {
    }

    private static IRMIResourceManager exportService()

    private static IRMIResourceManager waitForLookup(String host, Integer port, String fullBindName)

}
