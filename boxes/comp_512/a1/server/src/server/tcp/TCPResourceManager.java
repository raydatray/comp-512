package server.tcp;

import java.io.EOFException;
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
import tcp.responses.TCPBooleanResponseMessage;
import tcp.responses.TCPIntegerResponseMessage;

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
                    case AddFlight p -> {
                        TCPBooleanResponseMessage response = adapter.addFlight(p);

                        out.writeObject(response);
                        out.flush();
                    }
                    case AddCars p -> {
                        // TODO: handle AddCars
                    }
                    case AddRooms p -> {
                        // TODO: handle AddRooms
                    }
                    case AddCustomerID p -> {
                        // TODO: handle AddCustomerID
                    }
                    case DeleteFlight p -> {
                        // TODO: handle DeleteFlight
                    }
                    case DeleteCars p -> {
                        // TODO: handle DeleteCars
                    }
                    case DeleteRooms p -> {
                        // TODO: handle DeleteRooms
                    }
                    case DeleteCustomer p -> {
                        // TODO: handle DeleteCustomer
                    }
                    case QueryFlight p -> {
                        TCPIntegerResponseMessage response = adapter.queryFlight(p);

                        out.writeObject(response);
                        out.flush();
                    }
                    case QueryCars p -> {
                        // TODO: handle QueryCars
                    }
                    case QueryRooms p -> {
                        // TODO: handle QueryRooms
                    }
                    case QueryCustomer p -> {
                        // TODO: handle QueryCustomer
                    }
                    case QueryFlightPrice p -> {
                        TCPIntegerResponseMessage response = adapter.queryFlightPrice(p);

                        out.writeObject(response);
                        out.flush();
                    }
                    case QueryCarsPrice p -> {
                        // TODO: handle QueryCarsPrice
                    }
                    case QueryRoomsPrice p -> {
                        // TODO: handle QueryRoomsPrice
                    }
                    case ReserveFlight p -> {
                        // TODO: handle ReserveFlight
                    }
                    case ReserveCar p -> {
                        // TODO: handle ReserveCar
                    }
                    case ReserveRoom p -> {
                        // TODO: handle ReserveRoom
                    }
                    case Bundle p -> {
                        // TODO: handle Bundle
                    }
                    // Add other cases
                    default -> logger.warn("Unkown payload format: {}", payload);
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

    public TCPResourceManager(String name) {
        super(name);
    }
}
