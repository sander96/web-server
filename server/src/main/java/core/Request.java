package core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public class Request implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger(Request.class);
    private Socket socket;
    private Map<String, String> headers = new HashMap<>();
    private Map<Method, ResponseHandler> handlerMap;
    private Method method;
    private String path;
    private String protocol;
    private Map<String, String> pathParams = new HashMap<>();
    private Map<String, String> queryParams = new HashMap<>();
    private Connection connection;
    private final int MAX_HEADER_SIZE = 16 * 1024;

    public Request(Socket socket, Connection connection) {
        this.socket = socket;
        this.handlerMap = loadHandlers();
        this.connection = connection;
    }

    @Override
    public void run() {
        try (BufferedInputStream inputStream = new BufferedInputStream(socket.getInputStream(), MAX_HEADER_SIZE);
             OutputStream outputStream = socket.getOutputStream()) {

            byte[] buffer = new byte[MAX_HEADER_SIZE];
            inputStream.mark(MAX_HEADER_SIZE);

            inputStream.read(buffer);
            int index = new String(buffer).indexOf("\r\n\r\n");

            if (index == -1) {
                throw new RuntimeException("Header size too large or no delimiter?");
            } else {
                index += 4;
            }

            String headerData = new String(buffer, 0, index, "UTF-8");
            inputStream.reset();

            do {
                long skippedBytes = inputStream.skip(index);
                index -= skippedBytes;
            } while (index != 0);

            analyze(headerData);

            System.out.println(headerData);
            System.out.println();

            handlerMap.get(method).sendResponse(headers, method, path, protocol, pathParams, queryParams, inputStream, outputStream, connection);
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e.getMessage());
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
            path = URLDecoder.decode(methodLine[1], "UTF-8");
        } else if (pathParamsStart >= 0 && queryParamsStart < 0) {
            path = URLDecoder.decode(methodLine[1].substring(0, pathParamsStart), "UTF-8");
            pathParams = getParams(methodLine[1].substring(pathParamsStart), ";");
        } else if (pathParamsStart < 0 && queryParamsStart >= 0) {
            path = URLDecoder.decode(methodLine[1].substring(0, queryParamsStart), "UTF-8");
            queryParams = getParams(methodLine[1].substring(queryParamsStart), "&");
        } else {
            path = URLDecoder.decode(methodLine[1].substring(0, pathParamsStart), "UTF-8");
            pathParams = getParams(methodLine[1].substring(pathParamsStart, queryParamsStart), ";");
            queryParams = getParams(methodLine[1].substring(queryParamsStart), "&");
        }

        // set protocol
        protocol = methodLine[2];

        // set headers
        for (int i = 1; i < lines.length; i++) {
            String[] tmp = lines[i].split(":");
            if (tmp[1].charAt(0) == ' ') tmp[1] = tmp[1].substring(1);
            headers.put(tmp[0], tmp[1]);
        }
    }

    private Map<Method, ResponseHandler> loadHandlers() {
        Map<Method, ResponseHandler> handlers = new HashMap<>();

        for (ResponseHandler handler : ServiceLoader.load(ResponseHandler.class)) {
            handlers.put(handler.getKey(), handler);
        }
        return handlers;
    }

    private Map<String, String> getParams(String data, String separator) throws UnsupportedEncodingException {
        Map<String, String> params = new HashMap<>();
        data = data.substring(1);

        while (true) {
            String key = data.substring(0, data.indexOf("="));
            data = data.substring(data.indexOf("=") + 1);

            int separatorIndex = data.indexOf(separator);

            if (separatorIndex == -1) {
                params.put(URLDecoder.decode(key, "UTF-8"), URLDecoder.decode(data, "UTF-8"));
                break;
            } else {
                String value = data.substring(0, separatorIndex);
                data = data.substring(data.indexOf("&") + 1);

                params.put(URLDecoder.decode(key, "UTF-8"), URLDecoder.decode(value, "UTF-8"));
            }
        }
        return params;
    }
}
