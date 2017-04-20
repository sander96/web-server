package core;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Response {
    private Map<String, String> headers = new HashMap<>();
    private Method method;
    private Path path;
    private String scheme;
    private String[] pathParams;
    private String[] queryParams;
    SpecializedInputreader inputreader;
    OutputStream outputStream;

    public Response(Map<String, String> headers, Method method, Path path, String scheme, String[] pathParams,
                    String[] queryParams, SpecializedInputreader inputreader, OutputStream outputStream) {
        this.headers = headers;
        this.method = method;
        this.path = path;
        this.scheme = scheme;
        this.pathParams = pathParams;
        this.queryParams = queryParams;
        this.inputreader = inputreader;
        this.outputStream = outputStream;
    }

    public void sendResponse() throws IOException {
        switch (method) {
            case GET:
                GETRequest get = new GETRequest(outputStream, path, headers);
                get.sendResponse();
                break;
        }
    }
}
