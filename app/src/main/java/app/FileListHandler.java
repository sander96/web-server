package app;


import core.*;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URLDecoder;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileListHandler implements ResponseHandler {

    @Override
    public void sendResponse(Request request, SocketInputstream inputStream, OutputStream outputStream) throws IOException, SQLException {
        File file = new File(Paths.get("data", request.getPath()).toUri());

        if (file.isDirectory()) {
            List<Header> headerList = new ArrayList<>();
            String responsePage;
            String htmlList;

            if (isUser(request)) {
                if (request.getMethod() == Method.POST) {
                    if (request.getQueryParams().get("delete") != null &&
                            request.getQueryParams().get("delete").equals("Delete+Files")) {

                        delete(request, inputStream);
                        headerList.add(new Header("Location", request.getPath()));
                        ResponseHead.sendResponseHead(outputStream, request.getScheme(), StatusCode.FOUND, headerList);
                        return;
                    }
                    uploadFile(request, inputStream);
                }
                if (request.getQueryParams().get("delete") != null &&
                        request.getQueryParams().get("delete").equals("Delete+Files")) {

                    responsePage = ResourceLoader.loadTemplate(this, "list-template-delete.html");
                    htmlList = generateList(file, true, request);
                } else {
                    responsePage = ResourceLoader.loadTemplate(this, "list-template-user-normal.html");
                    htmlList = generateList(file, false, request);
                }
                if (request.getQueryParams().get("folder") != null &&
                        request.getQueryParams().get("folder").equals("Create+folder")) {

                    String folderName = URLDecoder.decode(request.getQueryParams().get("folder-name"), "UTF-8");
                    new File("data" + request.getPath() + folderName).mkdir();

                    headerList.add(new Header("Location", request.getPath()));
                    ResponseHead.sendResponseHead(outputStream, request.getScheme(), StatusCode.FOUND, headerList);
                }
            } else {
                responsePage = ResourceLoader.loadTemplate(this, "list-template-guest.html");
                htmlList = generateList(file, false, request);
            }

            responsePage = responsePage.replace("#title#", request.getPath());
            responsePage = responsePage.replace("#list#", htmlList);
            byte[] response = responsePage.getBytes();

            headerList.add(new Header("Content-Type", "text/html"));
            headerList.add(new Header("Content-Length", String.valueOf(response.length)));
            ResponseHead.sendResponseHead(outputStream, request.getScheme(), StatusCode.OK, headerList);

            outputStream.write(response);

        } else if (file.isFile()) {
            List<Header> headerList = new ArrayList<>();

            String type = request.getQueryParams().get("type");

            if (type != null) {
                headerList.add(new Header("Content-Type", type));
            } else {
                headerList.add(new Header("Content-Type", "multipart/form-data"));
            }

            headerList.add(new Header("Content-Length", String.valueOf(file.length())));
            ResponseHead.sendResponseHead(outputStream, request.getScheme(), StatusCode.OK, headerList);

            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                byte[] buffer = new byte[1024];

                while (true) {
                    int numRead = fileInputStream.read(buffer);
                    if (numRead == -1) {
                        break;
                    }

                    outputStream.write(buffer, 0, numRead);
                }
            }
        } else {
            ResponseHead.sendResponseHead(outputStream, request.getScheme(),
                    StatusCode.NOT_FOUND, null);
        }
    }

    @Override
    public String getKey() {
        return "/files/*";
    }

    private void delete(Request request, SocketInputstream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        byte[] buffer = new byte[1024];

        while (true) {
            int numRead = inputStream.read(buffer);
            if (numRead == -1) {
                break;
            }

            sb.append(new String(buffer, 0, numRead, "UTF-8"));
        }

        String[] fileList = sb.toString().split("&");

        for (String filename : fileList) {
            filename = URLDecoder.decode(filename.split("=")[0], "UTF-8");
            File file = new File("data" + request.getPath() + filename);

            if (file.isDirectory()) {
                FileUtils.deleteDirectory(file);
            } else {
                file.delete();
            }
        }
    }

    private String generateList(File parentFolder, boolean checkbox, Request request) throws IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String[] fileList = parentFolder.list();
        String htmlList = "";

        if (fileList != null) {
            for (String filename : fileList) {
                String slash = "";
                File folderContent = new File(Paths.get("data", request.getPath(), filename).toUri());
                if (folderContent.isDirectory()) {
                    slash = "/";
                }

                StringBuilder tmp1 = new StringBuilder();
                tmp1.append("<tr>\n");

                // link and filename
                tmp1.append("<td>");
                tmp1.append("<a href=\"").append(request.getPath()).append(filename)
                        .append(slash).append("\">").append(filename).append(slash).append("</a> ");
                tmp1.append("</td>\n");

                // MIMEs
                tmp1.append("<td>");

                if (folderContent.isDirectory()) {
                    tmp1.append("-");
                } else {
                    String type = MimeHandler.getType(filename);

                    if (type == null) {
                        tmp1.append("-");
                    } else {
                        tmp1.append("<a href=\"").append(request.getPath()).append(filename)
                                .append("?type=").append(type).append("\">").append(type).append("</a> ");
                    }
                }

                tmp1.append("</td>\n");

                // last modified
                tmp1.append("<td>");
                Date date = new Date(folderContent.lastModified());
                tmp1.append(dateFormat.format(date));
                tmp1.append("</td>\n");

                // file size
                tmp1.append("<td>");
                if (folderContent.isDirectory()) tmp1.append("-");
                else tmp1.append(folderContent.length());
                tmp1.append("</td>\n");

                // checkbox
                if (checkbox) {
                    tmp1.append("<td>");
                    tmp1.append("<input type=\"checkbox\" ").append("name=\"").append(filename).append("\">");
                    tmp1.append("</td>\n");
                }

                tmp1.append("</tr>\n");
                htmlList += tmp1;
            }
        }
        return htmlList;
    }

    private boolean isUser(Request request) throws SQLException {
        String url = "jdbc:h2:./data/database/database";
        try (Connection connection = DriverManager.getConnection(url)) {
            UserManager userManager = new UserManager(connection);
            return userManager.checkCookie(request.getHeaders().get("Cookie"));
        }
    }

    private void uploadFile(Request request, SocketInputstream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int numRead = inputStream.read(buffer);

        byte[] metaData = getMetaData(buffer, numRead);
        numRead -= metaData.length;

        byte[] boundary = getBoundary(metaData);
        String fileName = request.getPath() + getFileName(metaData);
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
}
