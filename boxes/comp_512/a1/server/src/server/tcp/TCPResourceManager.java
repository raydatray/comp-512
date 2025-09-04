package server.tcp;

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import interfaces.IResourceManagerService;
import interfaces.ITCPRequestPayload;
import server.common.ResourceManager;
import tcp.TCPRequestMessage;
import tcp.TCPResponseMessage;
import tcp.payloads.*;

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

                ITCPRequestPayload payload = ((TCPRequestMessage<? extends ITCPRequestPayload>) request).getPayload();

                switch (payload) {
                    case AddFlight p -> {
                        Boolean success = adapter.addFlight(p);
                        TCPResponseMessage response = new TCPResponseMessage(success);

                        out.writeObject(response);
                        out.flush();
                    }
                    default -> throw new IllegalArgumentException("Unkown payload: " + payload);
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
