package core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import serverexception.AccessRestrictedException;

import java.io.*;
import java.nio.file.Path;

public class FileHandler {  // TODO better synchronization
    private static final Logger logger = LogManager.getLogger(FileHandler.class);

    public static synchronized void sendFile(String path, OutputStream outputStream)
            throws FileNotFoundException {

        File file = new File(path);
        String contentType;

        if (path.substring(0, 5).equals("files")) {
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


        String responseHeader = "HTTP/1.1 200 OK\n" +
                "Content-Length:" + file.length() + "\n" +
                "Content-Type: " + contentType + "\n\r\n";

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
            logger.error("Error while sending file. Message: " + exception.getMessage());
            throw new RuntimeException("Something went wrong with file sending process");
        }
    }

    public static synchronized void writeToDisk(String path, InputStream inputStream,
                                                long contentLength) throws IOException {
        // TODO check if the file gets overwritten, 403 forbidden; NOT READY

        try (FileOutputStream fileWriter = new FileOutputStream(new File(path))) {
            byte[] buffer = new byte[1024];
            long fileLength = contentLength;
            logger.info("Starting to write file");
            while (fileLength > 0) {
                int bytesRead = inputStream.read(buffer);

                if (bytesRead == -1) {
                    logger.error("Error while writing file");
                    throw new RuntimeException("Client closed connection unexpectedly.");
                }
                fileWriter.write(buffer, 0, bytesRead);

                fileLength -= bytesRead;
            }
        }
    }

    public static synchronized void deleteFile(String path) throws Exception {
        File file = new File(path.substring(1));
        file.delete();
    }

    public static void checkServerDirectory(File file) throws FileNotFoundException, AccessRestrictedException {
        if (!file.exists()) {
            logger.error("File not found");
            throw new FileNotFoundException();
        }

        if (file.isAbsolute()) {
            logger.error("Error: AccessRestricted");
            throw new AccessRestrictedException();
        }
    }
}
