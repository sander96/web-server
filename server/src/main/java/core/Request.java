package core;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public class Request implements Runnable {
    private Socket socket;
    private ByteList byteList = new ByteList();
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> pathParams = new HashMap<>();
    private Map<String, String> queryParams = new HashMap<>();
    private Map<String, ResponseHandler> handlerMap;
    private Method method;
    private String scheme;
    private String path;

    public Request(Socket socket) {
        this.socket = socket;
        this.handlerMap = loadHandlers();
    }

    @Override
    public void run() {
        try (SocketInputstream inputStream = new SocketInputstream(new BufferedInputStream(socket.getInputStream()));
             OutputStream outputStream = socket.getOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;

            while (true) {
                bytesRead = inputStream.read(buffer);
                if (bytesRead == -1) break;

                byteList.add(buffer, 0, bytesRead);
            }

            String headerData = new String(byteList.getList());
            analyze(headerData);
            inputStream.specifyContentLength(headers.get("Content-Length"));

            // TODO delete when development is finished
            System.out.println(headerData);

            //handlerMap.get(method).sendResponse(this, inputStream, outputStream);
            ResponseHandler handler = getHandler(path);
            if (handler == null) {
                ResponseHead.sendResponseHead(outputStream, getScheme(), StatusCode.NOT_FOUND, null);
            } else {
                handler.sendResponse(this, inputStream, outputStream);
            }
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Method getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getScheme() {
        return scheme;
    }

    public Map<String, String> getPathParams() {
        return pathParams;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public Map<String, ResponseHandler> getHandlerMap() {
        return handlerMap;
    }

    private void analyze(String headData) throws UnsupportedEncodingException {
        String[] lines = headData.split("\r\n");
        String[] methodLine = lines[0].split(" ");

        // set http method
        method = Method.getMethod(methodLine[0]);

        // set path and parameters
        int pathParamsStart = methodLine[1].indexOf(';');
        int queryParamsStart = methodLine[1].indexOf('?');

        methodLine[1] = methodLine[1].replaceAll("\\+", "%2B");

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

        // set scheme
        scheme = methodLine[2];

        // set headers
        for (int i = 1; i < lines.length; i++) {
            String[] tmp = new String[2];
            int sep = lines[i].indexOf(":");

            if (sep < 0) continue;

            tmp[0] = lines[i].substring(0, sep).trim();
            tmp[1] = lines[i].substring(sep + 1).trim();
            headers.put(tmp[0], tmp[1]);
        }
    }

    /*private Map<Method, ResponseHandler> loadHandlers() {
        Map<Method, ResponseHandler> handlers = new HashMap<>();

        for (ResponseHandler handler : ServiceLoader.load(ResponseHandler.class)) {
            handlers.put(handler.getKey(), handler);
        }
        return handlers;
    }*/

    private Map<String, ResponseHandler> loadHandlers() {
        Map<String, ResponseHandler> handlers = new HashMap<>();

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

    private ResponseHandler getHandler(String path) {
        String currentPath = path;
        ResponseHandler handler = handlerMap.get(currentPath);
        if (handler != null) {
            return handler;
        }

        while (true) {
            if (currentPath.endsWith("/")) {
                handler = handlerMap.get(currentPath + "*");

                if (handler != null) {
                    return handler;
                }
                currentPath = currentPath.substring(0, currentPath.lastIndexOf("/"));
            }

            if (currentPath.length() == 0) break;
            currentPath = currentPath.substring(0, currentPath.lastIndexOf("/") + 1);
        }

        return null;
    }
}
