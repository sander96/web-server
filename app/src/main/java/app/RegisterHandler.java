package app;

import core.*;
import org.h2.tools.RunScript;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RegisterHandler implements ResponseHandler {
    @Override
    public void sendResponse(Request request, SocketInputstream inputStream, OutputStream outputStream) throws IOException, SQLException {
        String page = getRegisterPage();

        if (request.getMethod() == Method.POST) {
            String[] userData = readUserData(inputStream);

            if (isRegisterSuccessful(userData[0], userData[1])) {
                List<Header> headers = new ArrayList<>();
                headers.add(new Header("Location", "/"));
                headers.add(new Header("Set-Cookie", generateCookie(userData[0])));

                ResponseHead.sendResponseHead(outputStream, request.getScheme(), StatusCode.FOUND, headers);
                return;
            } else {
                page = page.replace("#paragraph#", "Username \"" + userData[0] + "\" already exists.");
            }
        }

        page = page.replace("#paragraph#", "");
        byte[] page_bytes = page.getBytes();

        List<Header> headers = new ArrayList<>();
        headers.add(new Header("Content-Type", "text/html"));
        headers.add(new Header("Content-Length", String.valueOf(page_bytes.length)));

        ResponseHead.sendResponseHead(outputStream, request.getScheme(), StatusCode.OK, headers);
        outputStream.write(page_bytes);
    }

    @Override
    public String getKey() {
        return "/register";
    }

    private String getRegisterPage() throws IOException {
        StringBuilder styleSheet = new StringBuilder();
        try (InputStream fileInputStream = getClass().getClassLoader()
                .getResourceAsStream("WebContent\\register.html")) {
            byte[] buffer = new byte[1024];

            while (true) {
                int numRead = fileInputStream.read(buffer);
                if (numRead == -1) break;

                styleSheet.append(new String(buffer, 0, numRead, "UTF-8"));
            }
        }

        return styleSheet.toString();
    }

    private String[] readUserData(SocketInputstream inputstream) throws IOException {
        StringBuilder rawData = new StringBuilder();
        byte[] buffer = new byte[1024];

        while (true) {
            int numRead = inputstream.read(buffer);
            if (numRead == -1) break;

            rawData.append(new String(buffer, 0, numRead));
        }
        String[] parts = rawData.toString().split("&");
        parts[0] = URLDecoder.decode(parts[0].substring(parts[0].indexOf("username=")+9), "UTF-8");
        parts[1] = URLDecoder.decode(parts[1].substring(parts[1].indexOf("password=")+9), "UTF-8");

        return parts;
    }

    private boolean isRegisterSuccessful(String username, String password) throws IOException, SQLException {
        String url = "jdbc:h2:./data/database/database";

        try (Connection connection = DriverManager.getConnection(url)) {
            RunScript.execute(connection, new FileReader("data/table.sql"));

            UserManager userManager = new UserManager(connection);
            return userManager.registerUser(username, password);
        }
    }

    private String generateCookie(String username) throws SQLException{
        String url = "jdbc:h2:./data/database/database";

        try (Connection connection = DriverManager.getConnection(url)) {
            UserManager userManager = new UserManager(connection);
            return "id=" + userManager.createCookie(username) + "; Max-Age=60";
        }
    }
}
