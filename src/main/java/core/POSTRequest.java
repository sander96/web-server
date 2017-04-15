package core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Map;

public class POSTRequest {
    private InputStream inStream;
    private OutputStream outStream;
    private Map<String, String> headers;
    private String filePath;
    private static final Logger logger = LogManager.getLogger(POSTRequest.class);

    public POSTRequest(InputStream inStream, OutputStream outStream,
                       Map<String, String> headers, String filePath) {
        this.inStream = inStream;
        this.outStream = outStream;
        this.headers = headers;
        this.filePath = filePath;
    }

    public void writeFile() throws IOException {
        // TODO write to disk test
        long contentLength = getContentLength();
        FileHandler.writeToDisk("files\\staticPath.txt", inStream, contentLength);
    }

    public void sendResponse() throws IOException {
        GETRequest postResponse = new GETRequest(outStream, filePath, headers);
        postResponse.sendResponse();
    }

    private long getContentLength() {
        String len = headers.get("Content-Length");
        if (len == null) {
            return -1;
        }

        return Long.parseLong(len);
    }
}
