package app;

import core.*;
import serverexception.AccessRestrictedException;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GETRequest implements ResponseHandler {
    private static final String SCHEME = "HTTP/1.1";

    public void sendResponse(Request request, SocketInputstream inputStream, OutputStream outputStream) throws IOException, SQLException {
        Map<String, String> headers = request.getHeaders();
        Connection connection = request.getConnection();
        String path = request.getPath();
        Map<String, String> queryParams = request.getQueryParams();

        try {
            String cookie = headers.get("Cookie");
            UserManager userManager = new UserManager(connection);

            if (cookie != null && !userManager.checkCookie(cookie)) {
                cookie = null;
            }

            if (path.equals("/")) {
                byte[] page = new DynamicPage().createIndexPage(cookie, userManager.getUsername(cookie), userManager.isAdmin(cookie)).getBytes("UTF-8");
                int pageLength = page.length;

                List<Header> headerList = new ArrayList<>();
                headerList.add(new Header("Content-Type", "text/html"));
                headerList.add(new Header("Content-Length", String.valueOf(pageLength)));

                ResponseHead.sendResponseHead(outputStream, SCHEME, StatusCode.OK, headerList);
                outputStream.write(page);
            } else if (path.equals("/login.html")) {   // TODO refactor code
                byte[] page = new DynamicPage().createLoginPage(false).getBytes("UTF-8");
                int pageLength = page.length;

                List<Header> headerList = new ArrayList<>();
                headerList.add(new Header("Content-Type", "text/html"));
                headerList.add(new Header("Content-Length", String.valueOf(pageLength)));

                ResponseHead.sendResponseHead(outputStream, SCHEME, StatusCode.OK, headerList);
                outputStream.write(page);
            } else if (path.equals("/register.html")) {
                byte[] page = new DynamicPage().createRegisterPage(false).getBytes("UTF-8");
                int pageLength = page.length;

                List<Header> headerList = new ArrayList<>();
                headerList.add(new Header("Content-Type", "text/html"));
                headerList.add(new Header("Content-Length", String.valueOf(pageLength)));

                ResponseHead.sendResponseHead(outputStream, SCHEME, StatusCode.OK, headerList);
                outputStream.write(page);
            } else if (path.equals("/users.html")) {
                byte[] page = new DynamicPage().createUsersPage(userManager.isAdmin(cookie), userManager.getUsernames()).getBytes("UTF-8");
                int pageLength = page.length;

                List<Header> headerList = new ArrayList<>();
                headerList.add(new Header("Content-Type", "text/html"));
                headerList.add(new Header("Content-Length", String.valueOf(pageLength)));

                ResponseHead.sendResponseHead(outputStream, SCHEME, StatusCode.OK, headerList);
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

                    List<Header> headerList = new ArrayList<>();
                    headerList.add(new Header("Content-Type", "text/html"));
                    headerList.add(new Header("Content-Length", String.valueOf(pageLength)));

                    ResponseHead.sendResponseHead(outputStream, SCHEME, StatusCode.OK, headerList);
                    outputStream.write(page);
                } else {
                    FileHandler.sendFile(path, outputStream);
                }
            }
        } catch (FileNotFoundException e) {
            ResponseHead.sendResponseHead(outputStream, SCHEME, StatusCode.NOT_FOUND, null);
        } catch (AccessRestrictedException e) {
            ResponseHead.sendResponseHead(outputStream, SCHEME, StatusCode.BAD_REQUEST, null);
        }
    }

    @Override
    public Method getKey() {
        return Method.GET;
    }
}
