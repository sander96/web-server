package app;

import org.apache.commons.io.FileUtils;
import serverexception.AccessRestrictedException;

import java.io.*;

public class FileHandler {  // TODO better synchronization
    public static synchronized void sendFile(String path, OutputStream outputStream)
            throws FileNotFoundException {

        File file = new File(path);
        String contentType;

        if (path.substring(0, 11).equals("data/files/")) {
            contentType = "multipart/form-data";
        } else {
            if (file.getName().endsWith(".html")) {
                contentType = "text/html";
            } else if (file.getName().endsWith(".css")) {
                contentType = "text/css";
            } else {
                contentType = "multipart/form-data";
            }
        }

        byte[] buffer = new byte[1024];
        FileInputStream fileStream = new FileInputStream(file);

        String responseHeader = "HTTP/1.1 200 OK\r\n" +
                "Content-Length:" + file.length() + "\r\n" +
                "Content-Type: " + contentType + "\r\n\r\n";

        try {
            outputStream.write(responseHeader.getBytes("UTF-8"));

            while (true) {
                int size = fileStream.read(buffer);

                if (size == -1) {
                    break;
                }
                outputStream.write(buffer, 0, size);
            }
        } catch (IOException exception) {
            throw new RuntimeException("Something went wrong with file sending process");
        }
    }

    public static synchronized void writeToDisk(String path, InputStream inputStream,
                                                long contentLength) throws IOException {
        // TODO check if the file gets overwritten, 403 forbidden; NOT READY

        try (FileOutputStream fileWriter = new FileOutputStream(new File(path))) {
            byte[] buffer = new byte[1024];
            long fileLength = contentLength;
            while (fileLength > 0) {
                int bytesRead = inputStream.read(buffer);

                if (bytesRead == -1) {
                    throw new RuntimeException("Client closed connection unexpectedly.");
                }
                fileWriter.write(buffer, 0, bytesRead);

                fileLength -= bytesRead;
            }
        }
    }

    public static synchronized void deleteFile(String path) throws IOException {
        File file = new File("data" + path);

        if (file.isDirectory()) {
            FileUtils.deleteDirectory(file);
        } else {
            file.delete();
        }
    }

    public static void checkServerDirectory(File file) throws FileNotFoundException, AccessRestrictedException {
        if (!file.exists()) {
            throw new FileNotFoundException();
        }

        if (file.isAbsolute()) {
            throw new AccessRestrictedException();
        }

        if (!file.toString().substring(0, 4).equals("data")) {
            throw new RuntimeException();
        }
    }
}
