package core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class Request implements Runnable {
    private Socket socket;
    private String request = "";
    private static final Logger logger = LogManager.getLogger(Request.class);

    public Request(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        logger.info("New request thread started.");
        int headLen = 0;
        byte[] requestIn = new byte[8192];
        byte[] delimiterSequence = new byte[3];

        try {        // TODO reading bytes more efficiently
            try (InputStream inputStream = socket.getInputStream();
                 OutputStream outStream = socket.getOutputStream()) {
                while (true) {
                    byte currentByte = (byte) inputStream.read();

                    if (headLen < 2) {
                        delimiterSequence[headLen] = currentByte;
                    } else {
                        delimiterSequence[0] = delimiterSequence[1];
                        delimiterSequence[1] = delimiterSequence[2];
                        delimiterSequence[2] = currentByte;

                        if (delimiterSequence[0] == '\n' && delimiterSequence[1] == '\r' && delimiterSequence[2] == '\n') {
                            //end of request head
                            request = URLDecoder.decode(new String(requestIn, 0, headLen), "UTF-8");
                            analyze(inputStream, outStream);
                            break;
                        }
                    }
                    requestIn[headLen] = currentByte;
                    ++headLen;
                }
            } finally {
                socket.close();
            }
        } catch (IOException e) {
            logger.error("IOException: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void analyze(InputStream inStream, OutputStream outStream) throws IOException {
        String[] requestLines = request.split("\n");
        String[] methodLine = requestLines[0].split(" ");
        Map<String, String> headers = new HashMap<>();
        String filePath = "";

        for (int i = 1; i < methodLine.length - 1; ++i) {
            filePath += methodLine[i] + " ";
        }
        filePath = filePath.substring(0, filePath.length() - 1);

        for (int i = 1; i < requestLines.length - 1; ++i) {
            String[] headerData = requestLines[i].split(": ");
            headers.put(headerData[0],
                    headerData[1].substring(0, headerData[1].length() - 1));
        }

        switch (methodLine[0]) {
            case "GET":
                GETRequest get = new GETRequest(outStream, filePath, headers);
                get.sendResponse();
                break;
            case "POST":
                POSTRequest post = new POSTRequest(inStream, outStream, headers, filePath);

                if (filePath.equals("/login.html")) { // TODO kolida postrequesti classi sisse?
                    post.login();
                } else {
                    post.writeFile();
                    post.sendResponse();
                }

                break;
            default:
                logger.error("Request method was not GET or POST");
                outStream.write("HTTP/1.1 500 Internal Server Error\n\r\n".getBytes());
        }

        System.out.println("--- DATA OF QUERY ---\n");
        System.out.println("Method: " + methodLine[0]);
        System.out.println("Path: " + filePath);
        System.out.println("Protocol and version: " + methodLine[2]);
        System.out.println("\n--- HEADER DATA ---\n");

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            System.out.println(key + ": " + value);
        }

        System.out.println("\n--- END OF REQUEST ---\n\n");
    }
}
