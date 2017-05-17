package core;

import org.h2.tools.RunScript;

import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Server {
    public static void main(String[] args) throws IOException, SQLException {
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            String url = "jdbc:h2:./data/database/database";

            try (Connection connection = DriverManager.getConnection(url)) {
                RunScript.execute(connection, new FileReader("data/table.sql"));
            }

            while (true) {
                Socket socket = serverSocket.accept();
                Thread thread = new Thread(new Request(socket));
                thread.start();
            }
        }
    }
}
