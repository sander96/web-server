package app;

import core.*;
import serverexception.AccessRestrictedException;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class GETRequest implements ResponseHandler {
    public void sendResponse(Map<String, String> headers, Method method, String path, String scheme, Map<String, String> pathParams,
                             Map<String, String> queryParams, BufferedInputStream inputStream, OutputStream outputStream, Connection connection) throws IOException, SQLException {
        try {
            String cookie = headers.get("Cookie");
            UserManager userManager = new UserManager(connection);

            if (cookie != null && !userManager.checkCookie(cookie)) {
                cookie = null;
            }

            if (path.equals("/")) {
                byte[] page = new DynamicPage().createIndexPage(cookie, userManager.getUsername(cookie)).getBytes("UTF-8");
                int pageLength = page.length;

                String responseHeaders = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: text/html\r\n" +
                        "Content-Length: " + pageLength + "\r\n\r\n";

                outputStream.write(responseHeaders.getBytes());
                outputStream.write(page);
            } else if (path.equals("/login.html")) {   // TODO refactor code
                byte[] page = new DynamicPage().createLoginPage(false).getBytes("UTF-8");
                int pageLength = page.length;

                String responseHeaders = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: text/html\r\n" +
                        "Content-Length: " + pageLength + "\r\n\r\n";

                outputStream.write(responseHeaders.getBytes());
                outputStream.write(page);
            } else if (path.equals("/register.html")) {
                byte[] page = new DynamicPage().createRegisterPage(false).getBytes("UTF-8");
                int pageLength = page.length;

                String responseHeaders = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: text/html\r\n" +
                        "Content-Length: " + pageLength + "\r\n\r\n";

                outputStream.write(responseHeaders.getBytes());
                outputStream.write(page);
            } else if (path.equals("/logout")) {
                String responseHeaders = "HTTP/1.1 302 Found\r\nLocation: /\r\n" +
                        "Set-Cookie: id=" + userManager.getUsername(cookie) +
                        "; expires=Thu, 01 Jan 1970 00:00:00 GMT\r\n\r\n";
                outputStream.write(responseHeaders.getBytes());
            } else {
                path = "data" + path;
                File file = new File(path);
                FileHandler.checkServerDirectory(file);

                boolean checkboxes = false;

                if (queryParams.get("checkbox-delete") != null && queryParams.get("checkbox-delete").equals("Delete files")) {
                    checkboxes = true;
                }

                if (file.isDirectory()) {
                    byte[] page = new DynamicPage().createFilePage(path, cookie != null, checkboxes).getBytes("UTF-8");
                    int pageLength = page.length;

                    String responseHeaders = "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: text/html\r\n" +
                            "Content-Length: " + pageLength + "\r\n\r\n";

                    outputStream.write(responseHeaders.getBytes());
                    outputStream.write(page);
                } else {
                    FileHandler.sendFile(path, outputStream);
                }
            }
        } catch (FileNotFoundException e) {
            outputStream.write("HTTP/1.1 404 File Not Found\r\n\r\n".getBytes());
        } catch (AccessRestrictedException e) {
            outputStream.write("HTTP/1.1 400 Bad Request\r\n\r\n".getBytes());
        }
    }

    public Method getKey() {
        return Method.GET;
    }
}
