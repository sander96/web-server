package app;

import core.*;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MainPageHandler implements ResponseHandler{
    @Override
    public void sendResponse(Request request, SocketInputstream inputStream, OutputStream outputStream) throws IOException, SQLException {
        byte[] page_bytes;

        if (isAdmin(request.getHeaders().get("Cookie"))){
            String userIndexPage = ResourceLoader.loadTemplate(this, "index_admin.html");
            String userName = getUserName(request.getHeaders().get("Cookie"));
            page_bytes = userIndexPage.replace("#username#", userName).getBytes();

        }else if (isUser(request.getHeaders().get("Cookie"))) {
            String userIndexPage = ResourceLoader.loadTemplate(this, "index_user.html");
            String userName = getUserName(request.getHeaders().get("Cookie"));
            page_bytes = userIndexPage.replace("#username#", userName).getBytes();

        } else {
            page_bytes = ResourceLoader.loadTemplate(this, "index_guest.html").getBytes();
        }

        List<Header> headerList = new ArrayList<>();
        headerList.add(new Header("Content-Type", "text/html"));
        headerList.add(new Header("Content-Length", String.valueOf(page_bytes.length)));

        ResponseHead.sendResponseHead(outputStream, request.getScheme(),
                StatusCode.OK, headerList);
        outputStream.write(page_bytes);
    }

    @Override
    public String getKey() {
        return "/";
    }

    private String getUserName(String cookie) throws IOException, SQLException{
        String url = "jdbc:h2:./data/database/database";
        try (Connection connection = DriverManager.getConnection(url)) {
            UserManager userManager = new UserManager(connection);
            return userManager.getUsername(cookie);
        }
    }

    private boolean isAdmin(String cookie) throws SQLException{
        String url = "jdbc:h2:./data/database/database";
        try (Connection connection = DriverManager.getConnection(url)) {
            UserManager userManager = new UserManager(connection);
            return userManager.isAdmin(cookie);
        }
    }

    private boolean isUser(String cookie) throws SQLException{
        String url = "jdbc:h2:./data/database/database";
        try (Connection connection = DriverManager.getConnection(url)) {
            UserManager userManager = new UserManager(connection);
            return userManager.checkCookie(cookie);
        }
    }
}
