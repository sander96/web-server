package core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Request implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger(Request.class);
    private static final byte[] HEAD_DELIMITER = "\r\n\r\n".getBytes();
    private Socket socket;
    private Map<String, String> headers = new HashMap<>();
    private Method method;
    private Path path;
    private String scheme;
    private String[] pathParams;
    private String[] queryParams;

    public Request(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (SpecializedInputreader inputreader = new SpecializedInputreader(socket.getInputStream());
             OutputStream outputStream = socket.getOutputStream()) {

            String headData = new String(inputreader.read(HEAD_DELIMITER), "UTF-8");
            analyze(headData);
            Response response = new Response(headers, method, path, scheme, pathParams, queryParams, inputreader, outputStream);
            response.sendResponse();
        } catch (IOException ioEx) {
            throw new RuntimeException(ioEx);
        }
    }

    private void analyze(String headData) throws UnsupportedEncodingException {
        String[] lines = headData.split("\r\n");
        String[] methodLine = lines[0].split(" ");

        // set http method
        method = Method.getMethod(methodLine[0]);

        // set path and parameters
        int pathParamsStart = methodLine[1].indexOf(';');
        int queryParamsStart = methodLine[1].indexOf('?');

        if (pathParamsStart < 0 && queryParamsStart < 0) {
            path = Paths.get(URLDecoder.decode(methodLine[1].substring(1), "UTF-8"));
        } else if (pathParamsStart >= 0 && queryParamsStart < 0) {
            path = Paths.get(URLDecoder.decode(methodLine[1].substring(1, pathParamsStart), "UTF-8"));
            pathParams = getPathParams(methodLine[1].substring(pathParamsStart));
        } else if (pathParamsStart < 0 && queryParamsStart >= 0) {
            path = Paths.get(URLDecoder.decode(methodLine[1].substring(1, queryParamsStart), "UTF-8"));
            queryParams = getQueryParams(methodLine[1].substring(queryParamsStart));
        } else {
            path = Paths.get(URLDecoder.decode(methodLine[1].substring(1, pathParamsStart), "UTF-8"));
            pathParams = getPathParams(methodLine[1].substring(pathParamsStart, queryParamsStart));
            queryParams = getQueryParams(methodLine[1].substring(queryParamsStart));
        }

        // set scheme
        scheme = methodLine[2];

        // set headers
        for (int i = 1; i < lines.length; i++) {
            String[] tmp = lines[i].split(":");
            if (tmp[1].charAt(0) == ' ') tmp[1] = tmp[1].substring(1);
            headers.put(tmp[0], tmp[1]);
        }
    }

    private String[] getPathParams(String initial) {
        // TODO method body
        return null;
    }

    private String[] getQueryParams(String initial) {
        // TODO method body
        return null;
    }
}
