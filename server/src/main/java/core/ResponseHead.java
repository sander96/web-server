package core;

import java.io.IOException;
import java.io.OutputStream;

public class ResponseHead {

    public void sendResponseHead(OutputStream outputStream, String scheme, StatusCode statusCode,
                                 Header[] headers) throws IOException {
        String responseHead = scheme + statusCode.toString() + "\r\n";
        for (Header header : headers) {
            responseHead += header.toString();
        }
        responseHead += "\r\n";
        outputStream.write(responseHead.getBytes());
    }
}
