package app;

import core.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LoginHandler implements ResponseHandler {
    @Override
    public void sendResponse(Request request, SocketInputstream inputStream, OutputStream outputStream) throws IOException, SQLException {
        if (request.getMethod() == Method.POST) {
            String[] userData = readUserData(inputStream);
        } else {
            String page = getLoginPage();
            page = page.replace("#paragraph#", "");
            byte[] page_bytes = page.getBytes();

            List<Header> headers = new ArrayList<>();
            headers.add(new Header("Content-Type", "text/html"));
            headers.add(new Header("Content-Length", String.valueOf(page_bytes.length)));

            ResponseHead.sendResponseHead(outputStream, request.getScheme(), StatusCode.OK, headers);
            outputStream.write(page_bytes);
        }
    }

    @Override
    public String getKey() {
        return "/login/";
    }

    private String getLoginPage() throws IOException {
        StringBuilder styleSheet = new StringBuilder();
        try (InputStream fileInputStream = getClass().getClassLoader()
                .getResourceAsStream("WebContent\\login.html")) {
            byte[] buffer = new byte[1024];

            while (true) {
                int numRead = fileInputStream.read(buffer);
                if (numRead == -1) break;

                styleSheet.append(new String(buffer, 0, numRead, "UTF-8"));
            }
        }

        return styleSheet.toString();
    }

    public String[] readUserData(SocketInputstream inputstream) throws IOException {
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
}
