package server.tcp;

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

import interfaces.IResourceManagerService;
import interfaces.ITCPRequestPayload;
import server.common.ResourceManager;
import tcp.requests.TCPRequestMessage;
import tcp.requests.payloads.AddCars;
import tcp.requests.payloads.AddCustomerID;
import tcp.requests.payloads.AddFlight;
import tcp.requests.payloads.AddRooms;
import tcp.requests.payloads.Bundle;
import tcp.requests.payloads.DeleteCars;
import tcp.requests.payloads.DeleteCustomer;
import tcp.requests.payloads.DeleteFlight;
import tcp.requests.payloads.DeleteRooms;
import tcp.requests.payloads.QueryCars;
import tcp.requests.payloads.QueryCarsPrice;
import tcp.requests.payloads.QueryCustomer;
import tcp.requests.payloads.QueryFlight;
import tcp.requests.payloads.QueryFlightPrice;
import tcp.requests.payloads.QueryRooms;
import tcp.requests.payloads.QueryRoomsPrice;
import tcp.requests.payloads.ReserveCar;
import tcp.requests.payloads.ReserveFlight;
import tcp.requests.payloads.ReserveRoom;

public class TCPResourceManager extends ResourceManager {
    private static final Logger logger = LoggerFactory.getLogger(
            TCPResourceManager.class);
    private static String serverName = "Server";
    private static ExecutorService threadPool = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        if (args.length > 0) {
            serverName = args[0];
        }
        if (args.length > 1) {
            logger.error("Server Exception: Usage: java server.tcp.TCPResourceManager [server_rmi_object]");

            System.exit(1);
        }

        try {
            IResourceManagerService service = new ResourceManager(serverName);
            TCPResourceManagerAdapter adapter = new TCPResourceManagerAdapter(service);

            int serverPort = 5001; // TODO: assign server port dynamically

            ServerSocket serverSocket = new ServerSocket(serverPort);

            logger.info(serverName + ": socket on port " + serverSocket.getLocalPort()
                    + " open, listening for requests...");

            Runtime.getRuntime().addShutdownHook(
                    new Thread() {
                        public void run() {
                            try {
                                serverSocket.close();
                                logger.info(serverName + ": socket on port " + serverSocket.getLocalPort() + " closed");
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
            logger.error(
                    "Server exception: uncaught exception, stack trace follows");
            e.printStackTrace();
            System.exit(1);
        }
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
            logger.warn("Client closed early");
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

    public TCPResourceManager(String name) {
        super(name);
    }
}
