package app;

import core.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class UsersHandler implements ResponseHandler {
    @Override
    public void sendResponse(Request request, SocketInputstream inputStream, OutputStream outputStream) throws IOException, SQLException {
        if (!isAdmin(request.getHeaders().get("Cookie"))) {
            ResponseHead.sendResponseHead(outputStream, request.getScheme(), StatusCode.NOT_FOUND, null);
            return;
        }

        if (request.getMethod() == Method.POST) {
            List<Header> headerList = new ArrayList<>();
            headerList.add(new Header("Location", "/users"));
            deleteUsers(inputStream);
            ResponseHead.sendResponseHead(outputStream, request.getScheme(), StatusCode.FOUND, headerList);
            return;
        }


        List<Header> headerList = new ArrayList<>();
        String page = ResourceLoader.loadTemplate(this, "users.html");
        page = page.replace("#list#", generateUsersList());
        byte[] page_bytes = page.getBytes();

        headerList.add(new Header("Content-Type", "text/html"));
        headerList.add(new Header("Content-Length", String.valueOf(page_bytes.length)));

        ResponseHead.sendResponseHead(outputStream, request.getScheme(), StatusCode.OK, headerList);
        outputStream.write(page_bytes);
    }

    @Override
    public String getKey() {
        return "/users";
    }

    private boolean isAdmin(String cookie) throws SQLException {
        String url = "jdbc:h2:./data/database/database";
        try (Connection connection = DriverManager.getConnection(url)) {
            UserManager userManager = new UserManager(connection);
            return userManager.isAdmin(cookie);
        }
    }

    private String generateUsersList() throws SQLException {
        StringBuilder htmlList = new StringBuilder();
        Map<String, Boolean> usernames = getUsernames();
        ArrayList<String> sortedUsernames = new ArrayList<>(usernames.keySet());
        Collections.sort(sortedUsernames);

        for (String username : sortedUsernames) {
            htmlList.append("<tr>\n");

            // append username
            htmlList.append("<td>");
            htmlList.append(username);
            htmlList.append("</td>\n");

            //append checkbox
            htmlList.append("<td>");
            if (!usernames.get(username)) {
                htmlList.append("<input type=\"checkbox\" name=\"");
                htmlList.append(username);
                htmlList.append("\">");
            } else {
                htmlList.append("<input type=\"checkbox\" disabled>");
            }

            htmlList.append("</td>\n");
            htmlList.append("</tr>\n");
        }

        return htmlList.toString();
    }

    private Map<String, Boolean> getUsernames() throws SQLException {
        String url = "jdbc:h2:./data/database/database";
        try (Connection connection = DriverManager.getConnection(url)) {
            UserManager userManager = new UserManager(connection);
            return userManager.getUsernames();
        }
    }

    private void deleteUsers(SocketInputstream inputStream) throws IOException, SQLException {
        StringBuilder sb = new StringBuilder();
        byte[] buffer = new byte[1024];
        while (true) {
            int numRead = inputStream.read(buffer);
            if (numRead == -1) break;

            sb.append(new String(buffer, 0, numRead, "UTF-8"));
        }

        String url = "jdbc:h2:./data/database/database";
        try (Connection connection = DriverManager.getConnection(url)) {
            UserManager userManager = new UserManager(connection);

            String[] users = sb.toString().split("&");
            for (String user : users) {
                user = URLDecoder.decode(user.substring(0, user.indexOf("=")), "UTF-8");
                userManager.deleteUser(user);
            }
        }
    }
}
