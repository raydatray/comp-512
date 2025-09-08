package client.tcp;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.common.Client;

public class TCPClient extends Client {
    private static final Logger logger = LoggerFactory.getLogger(
            TCPClient.class);
    private static String serverHost = "localhost";
    private static Integer serverPort = 5001; // TODO: support connection to multiple servers
    private static String serverName = "Server";

    public static void main(String args[]) {
        if (args.length > 0) {
            serverHost = args[0];
        }
        if (args.length > 1) {
            serverName = args[1];
        }
        if (args.length > 2) {
            logger.error(
                    "Client Exception: Usage: java client.rmi.RMIClient [server_hostname] [server_rmi_object]");

            System.exit(1);
        }

        try {
            TCPClient client = new TCPClient();
            client.connectServer();
            client.start();
        } catch (Exception e) {
            logger.error(
                    "Server exception: Uncaught exception, stack trace follows");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public TCPClient() {
        super();
    }

    public void connectServer() {
        connectServer(serverHost, serverPort, serverName);
    }

    public void connectServer(String server, Integer port, String name) {
        try {
            Boolean first = true;

            while (true) {
                try {
                    m_resourceManager = new TCPResourceManagerClientProxy(serverHost, serverPort);
                    logger.info(
                            "Connected to '" +
                                    name +
                                    "' server [" +
                                    server +
                                    ":" +
                                    port +
                                    "/" +
                                    name +
                                    "]");
                    break;
                } catch (IOException e) {
                    if (first) {
                        logger.info("Waiting for '" +
                                name +
                                "' server [" +
                                server +
                                ":" +
                                port +
                                "/" +
                                name +
                                "]");
                    }
                    first = false;
                }
                Thread.sleep(500);
            }
        } catch (Exception e) {
            logger.error(
                    "Server exception: Uncaught exception, stack trace follows");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
