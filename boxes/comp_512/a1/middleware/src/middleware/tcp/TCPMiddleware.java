package middleware.tcp;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.tcp.TCPResourceManagerClientProxy;
import interfaces.IResourceManagerService;
import interfaces.ITCPRequestPayload;
import middleware.common.Middleware;
import server.common.ResourceManager;
import server.tcp.TCPResourceManagerAdapter;
import tcp.requests.TCPRequestMessage;
import tcp.requests.payloads.*;

public final class TCPMiddleware {
    private static final Logger logger = LoggerFactory.getLogger(TCPMiddleware.class);
    private static String middlewareName = "Middleware";
    private static ExecutorService threadPool = Executors.newCachedThreadPool();

    private static String upstreamFlightHost = "localhost";
    private static Integer upstreamFlightPort = 5002;

    private static String upstreamCarHost = "localhost";
    private static Integer upstreamCarPort = 5003;

    private static String upstreamRoomHost = "localhost";
    private static Integer upstreamRoomPort = 5004;

    public static void main(String[] args) {
        try {
            IResourceManagerService service = buildMiddlewareService();
            TCPResourceManagerAdapter adapter = new TCPResourceManagerAdapter(service);

            Integer middlwarePort = 1099;
            ServerSocket serverSocket = new ServerSocket(middlwarePort);

            logger.info(middlewareName + ": socket on port " + serverSocket.getLocalPort()
                    + " open, listening for requests...");

            Runtime.getRuntime().addShutdownHook(
                    new Thread() {
                        public void run() {
                            try {
                                serverSocket.close();
                                logger.info(
                                        middlewareName + ": socket on port " + serverSocket.getLocalPort() + " closed");
                            } catch (Exception e) {
                                logger.error("Server exception: uncaught exception, stack trace follows");
                                e.printStackTrace();
                            }
                        }
                    });

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    threadPool.submit(() -> handleClient(clientSocket, adapter));
                } catch (SocketException e) {
                    logger.info("Server socket closed, exiting");
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("uncaught exception, stack trace follows");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static IResourceManagerService buildMiddlewareService() throws IOException {
        IResourceManagerService flightRM = new TCPResourceManagerClientProxy(upstreamFlightHost, upstreamFlightPort);
        IResourceManagerService carRM = new TCPResourceManagerClientProxy(upstreamCarHost, upstreamCarPort);
        IResourceManagerService roomRM = new TCPResourceManagerClientProxy(upstreamRoomHost, upstreamRoomPort);

        ResourceManager customerRM = new ResourceManager("Customers");

        return new Middleware(middlewareName, flightRM, carRM, roomRM, customerRM);
    }

    private static void handleClient(Socket socket, TCPResourceManagerAdapter adapter) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            while (true) {
                Object request = in.readObject();
                ITCPRequestPayload payload = ((TCPRequestMessage<? extends ITCPRequestPayload>) request).payload();

                switch (payload) {
                    case AddFlight p -> sendResponse(out, adapter.addFlight(p));
                    case AddCars p -> sendResponse(out, adapter.addCars(p));
                    case AddRooms p -> sendResponse(out, adapter.addRooms(p));
                    case AddCustomerID p -> {
                        if (p.customerID() != null) {
                            sendResponse(out, adapter.newCustomer());
                        } else {
                            sendResponse(out, adapter.newCustomer(p));
                        }
                    }
                    case DeleteFlight p -> sendResponse(out, adapter.deleteFlight(p));
                    case DeleteCars p -> sendResponse(out, adapter.deleteCars(p));
                    case DeleteRooms p -> sendResponse(out, adapter.deleteRooms(p));
                    case DeleteCustomer p -> sendResponse(out, adapter.deleteCustomer(p));
                    case QueryFlight p -> sendResponse(out, adapter.queryFlight(p));
                    case QueryCars p -> sendResponse(out, adapter.queryCars(p));
                    case QueryRooms p -> sendResponse(out, adapter.queryRooms(p));
                    case QueryCustomer p -> sendResponse(out, adapter.queryCustomerInfo(p));
                    case QueryFlightPrice p -> sendResponse(out, adapter.queryFlightPrice(p));
                    case QueryCarsPrice p -> sendResponse(out, adapter.queryCarsPrice(p));
                    case QueryRoomsPrice p -> sendResponse(out, adapter.queryRoomsPrice(p));
                    case ReserveFlight p -> sendResponse(out, adapter.reserveFlight(p));
                    case ReserveCar p -> sendResponse(out, adapter.reserveCar(p));
                    case ReserveRoom p -> sendResponse(out, adapter.reserveRoom(p));
                    case Bundle p -> sendResponse(out, adapter.bundle(p));
                    default -> logger.warn("Unknown payload format: {}", payload);
                }
            }
        } catch (EOFException e) {
            logger.info("Client connection closed");
        } catch (Exception e) {
            logger.error(
                    "Server exception: uncaught exception, stack trace follows");
            e.printStackTrace();
        }
    }

    private static void sendResponse(ObjectOutputStream out, Object response) throws IOException {
        out.writeObject(response);
        out.flush();
    }
}
