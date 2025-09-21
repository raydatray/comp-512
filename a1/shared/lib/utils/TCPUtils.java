package utils;

import interfaces.ITCPRequestPayload;
import interfaces.ITCPResourceManager;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

public class TCPUtils {

    private static final Logger logger = LoggerFactory.getLogger(
        TCPUtils.class
    );

    public static void sendResponse(ObjectOutputStream out, Object response)
        throws IOException {
        out.writeObject(response);
        out.flush();
    }

    public static void handleRequest(
        Socket socket,
        ITCPResourceManager adapter
    ) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(
                socket.getOutputStream()
            );
            out.flush();
            ObjectInputStream in = new ObjectInputStream(
                socket.getInputStream()
            );

            while (true) {
                Object request = in.readObject();
                ITCPRequestPayload payload = ((TCPRequestMessage<
                        ? extends ITCPRequestPayload
                    >) request).payload();

                switch (payload) {
                    case AddFlight p -> sendResponse(out, adapter.addFlight(p));
                    case AddCars p -> sendResponse(out, adapter.addCars(p));
                    case AddRooms p -> sendResponse(out, adapter.addRooms(p));
                    case AddCustomerID p -> {
                        if (p.customerID() == null) {
                            sendResponse(out, adapter.newCustomer());
                        } else {
                            sendResponse(out, adapter.newCustomer(p));
                        }
                    }
                    case DeleteFlight p -> sendResponse(
                        out,
                        adapter.deleteFlight(p)
                    );
                    case DeleteCars p -> sendResponse(
                        out,
                        adapter.deleteCars(p)
                    );
                    case DeleteRooms p -> sendResponse(
                        out,
                        adapter.deleteRooms(p)
                    );
                    case DeleteCustomer p -> sendResponse(
                        out,
                        adapter.deleteCustomer(p)
                    );
                    case QueryFlight p -> sendResponse(
                        out,
                        adapter.queryFlight(p)
                    );
                    case QueryCars p -> sendResponse(out, adapter.queryCars(p));
                    case QueryRooms p -> sendResponse(
                        out,
                        adapter.queryRooms(p)
                    );
                    case QueryCustomer p -> sendResponse(
                        out,
                        adapter.queryCustomerInfo(p)
                    );
                    case QueryFlightPrice p -> sendResponse(
                        out,
                        adapter.queryFlightPrice(p)
                    );
                    case QueryCarsPrice p -> sendResponse(
                        out,
                        adapter.queryCarsPrice(p)
                    );
                    case QueryRoomsPrice p -> sendResponse(
                        out,
                        adapter.queryRoomsPrice(p)
                    );
                    case ReserveFlight p -> sendResponse(
                        out,
                        adapter.reserveFlight(p)
                    );
                    case ReserveCar p -> sendResponse(
                        out,
                        adapter.reserveCar(p)
                    );
                    case ReserveRoom p -> sendResponse(
                        out,
                        adapter.reserveRoom(p)
                    );
                    case Bundle p -> sendResponse(out, adapter.bundle(p));
                    default -> logger.warn(
                        "Unknown payload format: {}",
                        payload
                    );
                }
            }
        } catch (EOFException e) {
            logger.warn("Client connection closed");
        } catch (Exception e) {
            logger.error(
                "Server exception: uncaught exception, stack trace follows"
            );
            e.printStackTrace();
        }
    }

    public static void addShutdownHook(
        ServerSocket serverSocket,
        String serverName
    ) {
        Runtime.getRuntime().addShutdownHook(
            new Thread() {
                public void run() {
                    try {
                        serverSocket.close();
                        logger.info(
                            serverName +
                                ": socket on port " +
                                serverSocket.getLocalPort() +
                                " closed"
                        );
                    } catch (Exception e) {
                        logger.error(
                            "Server exception: uncaught exception, stack trace follows"
                        );
                        e.printStackTrace();
                    }
                }
            }
        );
    }

    public static void acceptConnections(
        ExecutorService threadPool,
        ServerSocket serverSocket,
        ITCPResourceManager adapter
    ) throws IOException {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                threadPool.submit(() ->
                    TCPUtils.handleRequest(clientSocket, adapter)
                );
            } catch (SocketException e) {
                logger.info("Server socket closed, exiting");
                break;
            }
        }
    }

    public static Socket waitForConnection(
        String host,
        Integer port,
        String serverName
    ) {
        try {
            Boolean first = true;

            while (true) {
                try {
                    Socket socket = new Socket(host, port);
                    return socket;
                } catch (IOException e) {
                    if (first) {
                        logger.info("waiting for backend {}", serverName);
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
}
