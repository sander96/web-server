package core;

import serverexception.AccessRestrictedException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import static core.Server.logger;

public class GETRequest {
    private OutputStream outStream;
    private String filePath;
    private Map<String, String> headers;    // Not used yet

    public GETRequest(OutputStream outStream, String path, Map<String, String> headers) {
        this.outStream = outStream;
        this.filePath = path;
        this.headers = headers;
    }

    public void sendResponse() throws IOException {
        logger.info("Starting to send response");
        try {
            if (filePath.equals("/")) {
                FileHandler.sendFile("/index.html", outStream);
            } else {
                if (filePath.endsWith("/")) {
                    byte[] page = new DynamicPage().createPage(filePath.substring(1)).getBytes();
                    int pageLength = page.length;

                    String headers = "HTTP/1.1 200 OK\n" +
                            "Content-Type: text/html\n" +
                            "Content-Length: " + pageLength + "\n\r\n";

                    outStream.write(headers.getBytes());
                    outStream.write(page);
                } else {
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
