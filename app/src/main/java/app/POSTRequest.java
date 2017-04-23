package app;

import core.Method;
import core.ResponseHandler;
import core.SpecializedInputreader;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class POSTRequest implements ResponseHandler {
    private SpecializedInputreader inputReader;
    private Connection connection;
    private OutputStream outputStream;
    private Map<String, String> headers;

    @Override
    public void sendResponse(Map<String, String> headers, Method method, Path path, String scheme, Map<String, String> pathParams,
                             Map<String, String> queryParams, SpecializedInputreader inputreader, OutputStream outputStream, Connection connection) throws IOException, SQLException {

        this.inputReader = inputreader;
        this.connection = connection;
        this.outputStream = outputStream;
        this.headers = headers;

        if (path.toString().equals("login.html")) {
            login();
        } else if (path.toString().equals("register.html")) {
            register();
        }

    }

    @Override
    public Method getKey() {
        return Method.POST;
    }

    private void login() throws IOException, SQLException {
        byte[] buffer = inputReader.read(Integer.parseInt(headers.get("Content-Length")));

        String data = new String(buffer, "utf-8");
        String[] splitData = data.split("&");   // TODO what if & is part of the username or the password

        String username = splitData[0].split("=")[1];
        String password = splitData[1].split("=")[1];

        System.out.println(username);

        UserManager userManager = new UserManager(connection);
        boolean loginStatus = userManager.loginUser(username, password);

        byte[] page;
        String headerTemp;

        if (loginStatus) {
            headerTemp = "HTTP/1.1 302 Found\r\n" + "Location: /\r\n" + "Set-Cookie: id=" + userManager.createCookie(username) + "; Max-Age=15\r\n\r\n";
            outputStream.write(headerTemp.getBytes("utf-8"));
        } else {
            page = new DynamicPage().createLoginPage(true).getBytes("utf-8");

            headerTemp = "HTTP/1.1 200 OK\r\n" + "Content-Type: text/html\r\n" + // 15 sec, temp
                    "Content-Length: " + page.length + "\r\n\r\n";
            ;
            outputStream.write(headerTemp.getBytes("utf-8"));
            outputStream.write(page);
        }
    }

    private void register() throws IOException, SQLException {
        byte[] buffer = inputReader.read(Integer.parseInt(headers.get("Content-Length")));

        String data = new String(buffer, "utf-8");

        String[] splitData = data.split("&");   // TODO what if & is part of the username or the password

        String username = splitData[0].split("=")[1];
        String password = splitData[1].split("=")[1];

        UserManager userManager = new UserManager(connection);
        boolean isRegistered = userManager.registerUser(username, password);

        byte[] page;
        String headerTemp;

        if (isRegistered) {
            headerTemp = "HTTP/1.1 302 Found\r\n" + "Location: /\r\n" + "Set-Cookie: id=" + userManager.createCookie(username) + "; Max-Age=15\r\n\r\n";
            outputStream.write(headerTemp.getBytes("utf-8"));
        } else {
            page = new DynamicPage().createRegisterPage(true).getBytes("utf-8");
            headerTemp = "HTTP/1.1 200 OK\r\n" + "Content-Type: text/html\r\n" + // 15 sec, temp
                    "Content-Length: " + page.length + "\r\n\r\n";
            ;
            outputStream.write(headerTemp.getBytes("utf-8"));
            outputStream.write(page);
        }
    }
}
