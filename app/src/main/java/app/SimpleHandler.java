package app;


import core.*;

import java.io.*;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SimpleHandler implements ResponseHandler{

    @Override
    public void sendResponse(Request request, SocketInputstream inputStream, OutputStream outputStream) throws IOException, SQLException {
        boolean isUser = isUser(request);

        if (request.getMethod() == Method.POST && isUser) {
            uploadFile(request, inputStream);
        }

        File file = new File(Paths.get("data", request.getPath()).toUri());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        if (file.isDirectory()) {
            List<Header> headerList = new ArrayList<>();
            String responsePage = loadTemplate();
            String[] fileList = file.list();
            String htmlList = "<td><a href=\"../\">../</a></td>";

            if (fileList != null) {
                for (String s : fileList) {
                    String slash = "";
                    File folderContent = new File(Paths.get("data", request.getPath(), s).toUri());
                    if (folderContent.isDirectory()) {
                        slash = "/";
                    }

                    StringBuilder tmp1 = new StringBuilder();
                    tmp1.append("<tr>\n");

                    // link and filename
                    tmp1.append("<td>");
                    tmp1.append("<a href=\"").append(request.getPath()).append(s)
                            .append(slash).append("\">").append(s).append("</a> ");
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

                    tmp1.append("</tr>\n");
                    htmlList += tmp1;
                }
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
            headerList.add(new Header("Content-Type", "multipart/form-data"));
            headerList.add(new Header("Content-Length", String.valueOf(file.length())));
            ResponseHead.sendResponseHead(outputStream, request.getScheme(),
                    StatusCode.OK, headerList);

            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                byte[] buffer = new byte[1024];

                while (true) {
                    int numRead = fileInputStream.read(buffer);
                    if (numRead == -1) break;

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

    private String loadTemplate() throws IOException{
        byte[] buffer = new byte[1024];
        StringBuilder builder = new StringBuilder();

        try (InputStream fileInputStream = getClass().getClassLoader()
                .getResourceAsStream("WebContent\\list-template.html")) {

            while (true) {
                int bytesRead = fileInputStream.read(buffer);
                if (bytesRead == -1) break;

                builder.append(new String(buffer, 0, bytesRead, "UTF-8"));
            }
        }

        return builder.toString();
    }

    private boolean isUser(Request request) {
        return request.getHeaders().get("Cookie") != null;
    }

    private void uploadFile(Request request, SocketInputstream inputStream) throws IOException{
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
                    fileWriter.write(buffer, 0, contentEnd-2);
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

        data = data.substring(start+1);
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
        for (int i = 0; i < buffer.length-boundary.length; i++) {
            for (int j = 0; j < boundary.length; j++) {
                if (buffer[i+j] != boundary[j]) continue Outerloop;
            }
            return i;
        }

        return -1;
    }

    private byte[] getMetaData(byte[] b, int length) throws IOException{
        byte[] delimiter = new byte[4];

        for (int i = 0; i < length; i++) {
            delimiter[0] = delimiter[1];
            delimiter[1] = delimiter[2];
            delimiter[2] = delimiter[3];
            delimiter[3] = b[i];

            if (delimiter[0] == '\r' && delimiter[1] == '\n' && delimiter[2] == '\r' && delimiter[3] == '\n') {
                byte[] metaData = new byte[i+1];
                System.arraycopy(b, 0, metaData, 0, i+1);
                System.arraycopy(b, i+1, b, 0, length-i-1);
                return metaData;
            }
        }
        throw new RuntimeException("Metadata was too long");
    }
}
