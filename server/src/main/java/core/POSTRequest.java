package core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class POSTRequest {
    private InputStream inStream;
    private OutputStream outStream;
    private Map<String, String> headers;
    private Path filePath;
    private static final Logger logger = LogManager.getLogger(POSTRequest.class);

    public POSTRequest(InputStream inStream, OutputStream outStream,
                       Map<String, String> headers, Path filePath) {
        this.inStream = inStream;
        this.outStream = outStream;
        this.headers = headers;
        this.filePath = filePath;
    }

    public void writeFile() throws IOException {
        // TODO write to disk test
        long contentLength = getContentLength();
        String contentType = getContentType();

        if (contentType != null && contentType.equals("multipart/form-data")) {
            byte[] boundary = getBoundary().getBytes();
            long remaining = contentLength;
            byte[] buffer = new byte[1024];

            try {
                do {
                    int bytesRead = inStream.read(buffer);
                    if (bytesRead < 0) {
                        throw new RuntimeException("Error with reading data from input stream.");
                    }

                    remaining -= bytesRead;

                    int indexOfBoundary = getFirstIndex(buffer, 0, bytesRead, boundary);
                    if (indexOfBoundary > 0) {
                        int indexOfEmptyLine = getFirstIndex(buffer, 0, bytesRead, "\n\r\n".getBytes());

                        if (indexOfEmptyLine > 0) {
                            indexOfEmptyLine += 3;
                            int offset = indexOfBoundary + boundary.length;
                            String[] information = extractBodyInformation(new String(buffer, offset,
                                    indexOfEmptyLine - offset, "UTF-8"));

                            String filename = information[2];
                            File validFile = getNonexistentFile(filename);
                            int endOfCurrentFile = getFirstIndex(buffer, indexOfEmptyLine, bytesRead, boundary);
                            if (endOfCurrentFile > 0) {

                                while (true) {
                                    if (buffer[endOfCurrentFile] == '\n') {
                                        endOfCurrentFile--;
                                        break;
                                    }
                                    endOfCurrentFile--;
                                }

                                try (FileOutputStream fileWriter = new FileOutputStream(validFile)) {
                                    fileWriter.write(buffer, indexOfEmptyLine, endOfCurrentFile - indexOfEmptyLine);
                                }
                            } else {
                                try (FileOutputStream fileWriter = new FileOutputStream(validFile)) {
                                    fileWriter.write(buffer, indexOfEmptyLine, bytesRead - indexOfEmptyLine);
                                    while (true) {
                                        bytesRead = inStream.read(buffer);
                                        if (bytesRead < 0) {
                                            throw new RuntimeException("Problem with input stream.");
                                        }
                                        remaining -= bytesRead;
                                        endOfCurrentFile = getFirstIndex(buffer, 0, bytesRead, boundary);
                                        if (endOfCurrentFile > 0) {
                                            while (true) {
                                                if (buffer[endOfCurrentFile] == '\n') {
                                                    endOfCurrentFile--;
                                                    break;
                                                }
                                                endOfCurrentFile--;
                                            }
                                            fileWriter.write(buffer, 0, endOfCurrentFile);
                                            break;
                                        }
                                        fileWriter.write(buffer, 0, bytesRead);
                                    }
                                }
                            }
                        }
                    }
                } while (remaining > 0);

            } catch (IOException ioEx) {
                throw new RuntimeException();
            }
        }
    }

    private long getContentLength() {
        String len = headers.get("Content-Length");
        if (len == null) {
            return -1;
        }

        return Long.parseLong(len);
    }

    private String getContentType() {
        String data = headers.get("Content-Type");
        if (data == null) return null;

        return data.split(";")[0];
    }

    private String getBoundary() {
        String data = headers.get("Content-Type");
        String tmp_boundary = data.substring(data.indexOf("boundary=") + 9);
        tmp_boundary = tmp_boundary.replace("\"", "");

        return tmp_boundary;
    }

    private int getFirstIndex(byte[] data, int offset, int dataLen, byte[] sequence) {
        if (data == null || sequence == null) return -1;

        for (int i = offset; i < dataLen - sequence.length; i++) {
            for (int j = 0; j < sequence.length; j++) {
                if (data[i + j] != sequence[j]) {
                    break;
                }

                if (j == sequence.length - 1) {
                    return i;
                }
            }
        }

        return -1;
    }

    private String[] extractBodyInformation(String information) {
        String[] info = new String[4];
        String[] lines = information.split("\n");
        for (String s : lines) {
            if (s.contains("Content-Disposition:")) {
                String[] s_parts = s.split(";");
                info[0] = s_parts[0].split(": ")[1];
                if (s_parts.length > 1) {
                    info[1] = s_parts[1].substring(s_parts[1].indexOf("\""), s_parts[1].lastIndexOf("\""));
                }
                if (s_parts.length > 2) {
                    info[2] = s_parts[2].substring(s_parts[2].indexOf("\"") + 1, s_parts[2].lastIndexOf("\""));
                }
            } else if (s.contains("Content-Type")) {
                info[3] = s.substring(s.indexOf(":"));
                info[3] = info[3].replace(" ", "");
            }
        }
        return info;
    }

    private File getNonexistentFile(String filename) {
        File file = new File("files" + File.separator + filename);
        int append = 0;
        while (file.exists()) {
            int indexOfSeparator = filename.lastIndexOf(".");
            String[] nameParts = new String[2];
            if (indexOfSeparator > 0) {
                nameParts[0] = filename.substring(0, indexOfSeparator);
                nameParts[1] = filename.substring(indexOfSeparator);
            } else {
                nameParts[0] = filename;
            }
            nameParts[0] += String.valueOf(append);
            filename = nameParts[0];
            if (nameParts[1] != null) {
                filename += nameParts[1];
            }
            append++;
            file = new File("files" + File.separator + filename);
        }
        return file;
    }

    public void login() throws IOException {    // TODO more secure login
        byte[] buffer = new byte[4096];
        int size = inStream.read(buffer);

        String data = new String(buffer, 0, size, "utf-8");

        String[] splitData = data.split("&");   // TODO what if & is part of the username or the password

        String username = splitData[0].split("=")[1];
        String password = splitData[1].split("=")[1];

        boolean loginStatus = false;
        byte[] page;

        if (loginStatus) {
            page = new DynamicPage().createIndexPage().getBytes("utf-8");
        } else {
            page = new DynamicPage().createLoginPage(true).getBytes("utf-8");
        }

        int pageLength = page.length;

        String headers = "HTTP/1.1 302 Found\n" + "Location: /\r\n" +
                "Content-Type: text/html\r\n" + "Set-Cookie: name=" + username + "; Max-Age=15\r\n" + // 15 sec, temp
                "Content-Length: " + pageLength + "\r\n\r\n";

        outStream.write(headers.getBytes("utf-8"));
        outStream.write(page);
    }
}
