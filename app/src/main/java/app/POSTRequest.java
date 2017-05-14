package app;

import core.Method;
import core.Request;
import core.ResponseHandler;
import core.SocketInputstream;

import java.io.*;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class POSTRequest implements ResponseHandler {
    private SocketInputstream inputStream;
    private Connection connection;
    private OutputStream outputStream;
    private Map<String, String> headers;

    @Override
    public void sendResponse(Request request, SocketInputstream inputStream, OutputStream outputStream) throws IOException, SQLException {
        this.inputStream = inputStream;
        this.connection = request.getConnection();
        this.outputStream = outputStream;
        this.headers = request.getHeaders();
        String path = request.getPath();
        Map<String, String> queryParams = request.getQueryParams();

        String cookie = headers.get("Cookie");
        UserManager userManager = new UserManager(connection);

        if (cookie != null && !userManager.checkCookie(cookie)) {
            cookie = null;
        }

        if (path.equals("/login.html")) {
            login();
        } else if (path.equals("/register.html")) {
            register();
        } else if (queryParams.get("checkbox-delete") != null && queryParams.get("checkbox-delete").equals("Delete files")) {
            if (cookie == null) {
                throw new RuntimeException();
            }

            deleteFiles(path);
        } else if (path.equals("/users.html")) {
            if (!userManager.isAdmin(cookie)) {
                throw new RuntimeException();
            }

            deleteUsers(userManager);
        } else {
            if (cookie == null) {
                throw new RuntimeException();
            }

            byte[] buffer = new byte[1024];
            int numRead = inputStream.read(buffer);

            byte[] metaData = getMetaData(buffer, numRead);
            numRead -= metaData.length;

            byte[] boundary = getBoundary(metaData);
            String fileName = path + getFileName(metaData);
            File file = new File("data" + fileName);

            try (FileOutputStream fileWriter = new FileOutputStream(file)) {
                while (true) {
                    int contentEnd = getContentEnd(buffer, boundary);

                    if (contentEnd == -1) {
                        fileWriter.write(buffer, 0, numRead);
                    } else {
                        fileWriter.write(buffer, 0, contentEnd - 2);
                        break;
                    }

                    numRead = inputStream.read(buffer);
                    if (numRead < 0) break;
                }
            }

            request.getHandlerMap().get(Method.GET).sendResponse(request, inputStream, outputStream);
        }
    }

    @Override
    public Method getKey() {
        return Method.POST;
    }

    private void login() throws IOException, SQLException {
        byte[] buffer = new byte[2048];
        int size = 0;
        int maxSize = Integer.parseInt(headers.get("Content-Length"));
        StringBuilder data = new StringBuilder();

        while (maxSize > 0) {
            size = inputStream.read(buffer);
            maxSize -= size;

            if (size == -1) {
                break;
            }

            data.append(new String(buffer, 0, size, "UTF-8"));
        }

        String[] splitData = data.toString().split("&");

        String username = URLDecoder.decode(splitData[0].split("=")[1], "UTF-8");
        String password = URLDecoder.decode(splitData[1].split("=")[1], "UTF-8");

        UserManager userManager = new UserManager(connection);
        boolean loginStatus = userManager.loginUser(username, password);

        byte[] page;
        String headerString;

        if (loginStatus) {
            headerString = "HTTP/1.1 302 Found\r\n" + "Location: /\r\n" + "Set-Cookie: id=" +
                    userManager.createCookie(username) + "; Max-Age=3600\r\n\r\n";
            outputStream.write(headerString.getBytes("UTF-8"));
        } else {
            page = new DynamicPage().createLoginPage(true).getBytes("UTF-8");

            headerString = "HTTP/1.1 200 OK\r\n" + "Content-Type: text/html\r\n" +
                    "Content-Length: " + page.length + "\r\n\r\n";

            outputStream.write(headerString.getBytes("UTF-8"));
            outputStream.write(page);
        }
    }

    private void register() throws IOException, SQLException {
        byte[] buffer = new byte[2048];
        int size = 0;
        int maxSize = Integer.parseInt(headers.get("Content-Length"));
        StringBuilder data = new StringBuilder();

        while (maxSize > 0) {
            size = inputStream.read(buffer);
            maxSize -= size;

            if (size == -1) {
                break;
            }

            data.append(new String(buffer, 0, size, "UTF-8"));
        }

        String[] splitData = data.toString().split("&");

        String username = URLDecoder.decode(splitData[0].split("=")[1], "UTF-8");
        String password = URLDecoder.decode(splitData[1].split("=")[1], "UTF-8");

        UserManager userManager = new UserManager(connection);
        boolean isRegistered = userManager.registerUser(username, password);

        byte[] page;
        String headerString;

        if (isRegistered) {
            headerString = "HTTP/1.1 302 Found\r\n" + "Location: /\r\n" + "Set-Cookie: id=" +
                    userManager.createCookie(username) + "; Max-Age=3600\r\n\r\n";
            outputStream.write(headerString.getBytes("UTF-8"));
        } else {
            page = new DynamicPage().createRegisterPage(true).getBytes("UTF-8");
            headerString = "HTTP/1.1 200 OK\r\n" + "Content-Type: text/html\r\n" +
                    "Content-Length: " + page.length + "\r\n\r\n";

            outputStream.write(headerString.getBytes("UTF-8"));
            outputStream.write(page);
        }
    }

    private void deleteFiles(String path) throws IOException {
        byte[] buffer = new byte[2048];
        int size = 0;
        int maxSize = Integer.parseInt(headers.get("Content-Length"));
        StringBuilder data = new StringBuilder();

        while (maxSize > 0) {
            size = inputStream.read(buffer);
            maxSize -= size;

            if (size == -1) {
                break;
            }

            data.append(new String(buffer, 0, size, "UTF-8"));
        }

        String[] splitData = data.toString().split("&");

        for (String str : splitData) {
            String filename = URLDecoder.decode(str.replace("=on", ""), "UTF-8");
            FileHandler.deleteFile(path + filename);
        }

        String headerString = "HTTP/1.1 302 Found\r\n" + "Location: " + path + "\r\n\r\n";
        outputStream.write(headerString.getBytes("UTF-8"));
    }

    private void deleteUsers(UserManager userManager) throws IOException, SQLException {
        byte[] buffer = new byte[2048];
        int size = 0;
        int maxSize = Integer.parseInt(headers.get("Content-Length"));
        StringBuilder data = new StringBuilder();

        while (maxSize > 0) {
            size = inputStream.read(buffer);
            maxSize -= size;

            if (size == -1) {
                break;
            }

            data.append(new String(buffer, 0, size, "UTF-8"));
        }

        String[] splitData = data.toString().split("&");
        Map<String, Boolean> users = userManager.getUsernames();

        for (String username : splitData) {
            String user = URLDecoder.decode(username.split("=")[0], "UTF-8");

            if (!users.get(user)) {
                userManager.deleteUser(user);
            }
        }

        byte[] page = new DynamicPage().createUsersPage(true, userManager.getUsernames()).getBytes("UTF-8");

        String headerString = "HTTP/1.1 200 OK\r\n" + "Content-Type: text/html\r\n" +
                "Content-Length: " + page.length + "\r\n\r\n";

        outputStream.write(headerString.getBytes("UTF-8"));
        outputStream.write(page);
    }

    private byte[] getMetaData(byte[] b, int length) throws IOException {
        byte[] delimiter = new byte[4];

        for (int i = 0; i < length; i++) {
            delimiter[0] = delimiter[1];
            delimiter[1] = delimiter[2];
            delimiter[2] = delimiter[3];
            delimiter[3] = b[i];

            if (delimiter[0] == '\r' && delimiter[1] == '\n' && delimiter[2] == '\r' && delimiter[3] == '\n') {
                byte[] metaData = new byte[i + 1];
                System.arraycopy(b, 0, metaData, 0, i + 1);
                System.arraycopy(b, i + 1, b, 0, length - i - 1);
                return metaData;
            }
        }
        throw new RuntimeException("Metadata was too long");
    }

    private String getFileName(byte[] metaData) {
        String data = new String(metaData);
        int start = data.indexOf("filename=") + "filename=".length();

        data = data.substring(start + 1);
        int end = data.indexOf("\r");

        return data.substring(0, end).replace("\"", "");
    }


    private byte[] getBoundary(byte[] metaData) {
        for (int i = 0; i < metaData.length; i++) {
            if (metaData[i] == '\r') {
                byte[] boundary = new byte[i];
                System.arraycopy(metaData, 0, boundary, 0, i);
                return boundary;
            }
        }
        throw new RuntimeException("Boundary not found");
    }

    private int getContentEnd(byte[] buffer, byte[] boundary) {
        Outerloop:
        for (int i = 0; i < buffer.length - boundary.length; i++) {
            for (int j = 0; j < boundary.length; j++) {
                if (buffer[i + j] != boundary[j]) continue Outerloop;
            }
            return i;
        }

        return -1;
    }
}
