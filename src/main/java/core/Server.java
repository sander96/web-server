package core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    static final Logger logger = LogManager.getLogger(Server.class);

    public static void main(String[] args) throws IOException {
        logger.info("Server bootup");
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            while (true) {
                Socket socket = serverSocket.accept();
                Thread thread = new Thread(new Request(socket));
                thread.start();
            }
        }
    }
}
