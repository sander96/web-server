package core;

import java.io.*;
import java.net.*;
import java.util.*;

public class Request implements Runnable{
    private Socket socket;
    private String request = "";

    public Request(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run() {
        int headLen = 0;
        byte[] requestIn = new byte[8192];
        byte[] delimiterSequence = new byte[3];

        try{
            try(InputStream inputStream = socket.getInputStream();
                OutputStream outStream = socket.getOutputStream()){
                while(true) {
                    byte currentByte = (byte) inputStream.read();
                    if (currentByte < 0){
                        System.out.println(request);
                        throw new RuntimeException("Client closed connection unexpectedly.");
                    }

                    if (headLen < 2){
                        delimiterSequence[headLen] = currentByte;
                    } else {
                        delimiterSequence[0] = delimiterSequence[1];
                        delimiterSequence[1] = delimiterSequence[2];
                        delimiterSequence[2] = currentByte;

                        if (delimiterSequence[0] == '\n' && delimiterSequence[1] == '\r' && delimiterSequence[2] == '\n'){
                            //end of request head
                            request = URLDecoder.decode(new String(requestIn, 0, headLen), "UTF-8");
                            System.out.println(request);
                            analyze(inputStream, outStream);
                            break;
                        }
                    }
                    requestIn[headLen] = currentByte;
                    ++headLen;
                }
            }finally {
                socket.close();
            }
        }catch (IOException ioEx){
            throw new RuntimeException(ioEx);
        }
    }

    private void analyze(InputStream inStream, OutputStream outStream) throws IOException{
        String[] requestLines = request.split("\n");
        String[] methodLine = requestLines[0].split(" ");
        Map<String, String> headers = new HashMap<>();

        for(int i = 1; i < requestLines.length - 1; ++i){
            String[] headerData = requestLines[i].split(": ");

            // TODO bad request error, 400

            if (headerData.length != 2){        // TODO mis jama see on?
                throw new RuntimeException("Bad request.");
            }

            headers.put(headerData[0],
                    headerData[1].substring(0, headerData[1].length()-1));
        }

        switch (methodLine[0]) {
            case "GET": // generate new core.GETRequest
                GETRequest get = new GETRequest(outStream, methodLine[1], headers);
                get.sendResponse();
                break;
            case "POST": // generate new core.POSTRequest
                POSTRequest post = new POSTRequest(inStream, outStream, headers, methodLine[1]);
                post.writeFile();        // TODO todo
                post.sendResponse();
                break;
            default: // generate new errorPage (we have no full support)
                throw new RuntimeException("This method request is not supported."); // TODO 500
        }

        // prolly not going to stay here of course (daaa!?)
        System.out.println("--- DATA OF QUERY ---\n");
        System.out.println("Method: " + methodLine[0]);
        System.out.println("Path: " + methodLine[1]);
        System.out.println("Protocol and version: " + methodLine[2]);
        System.out.println("\n--- HEADER DATA ---\n");

        for (Map.Entry<String, String> entry: headers.entrySet()){
            String key = entry.getKey();
            String value = entry.getValue();
            System.out.println(key + ": " + value);
        }

        System.out.println("\n--- END OF REQUEST ---\n\n");
    }
}
