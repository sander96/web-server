package app;

import core.*;
import org.h2.tools.RunScript;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MainPageHandler implements ResponseHandler{
    @Override
    public void sendResponse(Request request, SocketInputstream inputStream, OutputStream outputStream) throws IOException, SQLException {
        if (true) {
            byte[] page = loadTemplate("index_guest.html").getBytes();

            List<Header> headerList = new ArrayList<>();
            headerList.add(new Header("Content-Type", "text.html"));
            headerList.add(new Header("Content-Length", String.valueOf(page.length)));

            ResponseHead.sendResponseHead(outputStream, request.getScheme(),
                    StatusCode.OK, headerList);
            outputStream.write(page);
        } else {
            String userIndexPage = loadTemplate("index_user.html");
            String userName = getUserName(request.getHeaders().get("Cookie"));
        }
    }

    @Override
    public String getKey() {
        return "/";
    }

    private String loadTemplate(String fileName) throws IOException{
        byte[] buffer = new byte[1024];
        StringBuilder builder = new StringBuilder();

        try (InputStream fileInputStream = getClass().getClassLoader()
                .getResourceAsStream("WebContent\\" + fileName)) {

            while (true) {
                int bytesRead = fileInputStream.read(buffer);
                if (bytesRead == -1) break;

                builder.append(new String(buffer, 0, bytesRead, "UTF-8"));
            }
        }

        return builder.toString();
    }

    private String getUserName(String cookie) throws IOException, SQLException{
        String url = "jdbc:h2:./data/database/database";

        try (Connection connection = DriverManager.getConnection(url)) {
            RunScript.execute(connection, new FileReader("data/table.sql"));

            UserManager userManager = new UserManager(connection);
            return userManager.getUsername(cookie);
        }
    }
}
