package app;


import core.*;
import serverexception.AccessRestrictedException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class GETRequest implements ResponseHandler{
    public void sendResponse(Map<String, String> headers, Method method, Path path, String scheme, Map<String, String> pathParams,
                             Map<String, String> queryParams, SpecializedInputreader inputreader, OutputStream outputStream, Connection connection) throws IOException {
        try {
            if (path.toString().equals("")) {
                byte[] page = new DynamicPage().createIndexPage().getBytes("utf-8");
                int pageLength = page.length;

                String responseHeaders = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: text/html\r\n" +
                        "Content-Length: " + pageLength + "\r\n\r\n";

                outputStream.write(responseHeaders.getBytes());
                outputStream.write(page);
            } else if (path.toString().equals("login.html")){   // TODO refactor code
                byte[] page = new DynamicPage().createLoginPage(false).getBytes("utf-8");
                int pageLength = page.length;

                String responseHeaders = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: text/html\r\n" +
                        "Content-Length: " + pageLength + "\r\n\r\n";

                outputStream.write(responseHeaders.getBytes());
                outputStream.write(page);
            } else if (path.toString().equals("register.html")){
                byte[] page = new DynamicPage().createRegisterPage(false).getBytes("utf-8");
                int pageLength = page.length;

                String responseHeaders = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: text/html\r\n" +
                        "Content-Length: " + pageLength + "\r\n\r\n";

                outputStream.write(responseHeaders.getBytes());
                outputStream.write(page);
            } else {
                if (path.toFile().isDirectory()) {
                    byte[] page = new DynamicPage().createFilePage(path).getBytes("utf-8");
                    int pageLength = page.length;

                    String responseHeaders = "HTTP/1.1 200 OK\n" +
                            "Content-Type: text/html\n" +
                            "Content-Length: " + pageLength + "\n\r\n";

                    outputStream.write(responseHeaders.getBytes());
                    outputStream.write(page);
                } else {
                    FileHandler.checkServerDirectory(path);
                    FileHandler.sendFile(path, outputStream);
                }
            }
        } catch (FileNotFoundException e) {
            outputStream.write("HTTP/1.1 404 File Not Found\n\r\n".getBytes());
        } catch (AccessRestrictedException e) {
            outputStream.write("HTTP/1.1 400 Bad Request\n\r\n".getBytes());
        }
    }

    public Method getKey() {
        return Method.GET;
    }
}
