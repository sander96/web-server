package app;

import core.*;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LogOutHandler implements ResponseHandler{
    @Override
    public void sendResponse(Request request, SocketInputstream inputStream, OutputStream outputStream) throws IOException, SQLException {
        List<Header> headers = new ArrayList<>();
        headers.add(new Header("Location", "/"));
        headers.add(new Header("Set-Cookie", "id=" + getUsername(request.getHeaders().get("Cookie")) +
                "; Expires=Thu, 01 Jan 1970 00:00:00 GMT"));

        ResponseHead.sendResponseHead(outputStream, request.getScheme(), StatusCode.FOUND, headers);
    }

    @Override
    public String getKey() {
        return "/logout";
    }

    public String getUsername(String cookie) throws SQLException{
        String url = "jdbc:h2:./data/database/database";

        try (Connection connection = DriverManager.getConnection(url)) {
            UserManager userManager = new UserManager(connection);
            return userManager.getUsername(cookie);
        }
    }
}
