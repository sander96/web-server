package core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Server {
    private static final Logger logger = LogManager.getLogger(Server.class);

    public static void main(String[] args) throws IOException, SQLException {
        logger.info("Server bootup");

        String url = "jdbc:h2:./database";
        DriverManager.getConnection(url);

        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            while (true) {
                Socket socket = serverSocket.accept();
                Thread thread = new Thread(new Request(socket));
                thread.start();
            }
        }
    }
}
