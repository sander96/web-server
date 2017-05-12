package core;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class ResponseHead {

    public static void sendResponseHead(OutputStream outputStream, String scheme, StatusCode statusCode,
                                 List<Header> headers) throws IOException {
        StringBuilder responseHead = new StringBuilder();

        responseHead.append(scheme);
        responseHead.append(statusCode.toString());
        responseHead.append("\r\n");

        if (headers != null) {
            for (Header header : headers) {
                responseHead.append(header.toString());
            }
        }
        responseHead.append("\r\n");

        outputStream.write(responseHead.toString().getBytes("UTF-8"));
    }
}
