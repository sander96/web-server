package core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import serverexception.AccessRestrictedException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Map;

public class GETRequest {
    private OutputStream outStream;
    private Path filePath;
    private Map<String, String> headers;
    private static final Logger logger = LogManager.getLogger(GETRequest.class);

    public GETRequest(OutputStream outStream, Path path, Map<String, String> headers) {
        this.outStream = outStream;
        this.filePath = path;
        this.headers = headers;
    }

    public void sendResponse() throws IOException {
        logger.info("Starting to send response");
        try {
            if (filePath.toString().equals("")) {
                byte[] page = new DynamicPage().createIndexPage().getBytes("utf-8");
                int pageLength = page.length;

                String headers = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: text/html\r\n" +
                        "Content-Length: " + pageLength + "\r\n\r\n";

                outStream.write(headers.getBytes());
                outStream.write(page);
            } else if (filePath.toString().equals("login.html")){
                byte[] page = new DynamicPage().createLoginPage(false).getBytes("utf-8");
                int pageLength = page.length;

                String headers = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: text/html\r\n" +
                        "Content-Length: " + pageLength + "\r\n\r\n";

                outStream.write(headers.getBytes());
                outStream.write(page);
            } else {
                if (filePath.toFile().isDirectory()) {
                    byte[] page = new DynamicPage().createFilePage(filePath).getBytes("utf-8");
                    int pageLength = page.length;

                    String headers = "HTTP/1.1 200 OK\n" +
                            "Content-Type: text/html\n" +
                            "Content-Length: " + pageLength + "\n\r\n";

                    outStream.write(headers.getBytes());
                    outStream.write(page);
                } else {
                    FileHandler.checkServerDirectory(filePath);
                    FileHandler.sendFile(filePath, outStream);
                }
            }
        } catch (FileNotFoundException e) {
            logger.error("Error while sending response. Message: " + e.getMessage());
            outStream.write("HTTP/1.1 404 File Not Found\n\r\n".getBytes());
        } catch (AccessRestrictedException e) {
            logger.error("Error while sending response. Message: " + e.getMessage());
            outStream.write("HTTP/1.1 400 Bad Request\n\r\n".getBytes());
        }
    }
}
