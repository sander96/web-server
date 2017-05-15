package core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.h2.tools.RunScript;

import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Server {
    private static final Logger LOGGER = LogManager.getLogger(Server.class);

    public static void main(String[] args) throws IOException, SQLException {
        LOGGER.info("Server bootup");

        try (ServerSocket serverSocket = new ServerSocket(8080)) {

            while (true) {
                Socket socket = serverSocket.accept();
                Thread thread = new Thread(new Request(socket));
                thread.start();
            }
        }
    }
}
