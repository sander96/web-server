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
        StringBuilder oneRequestLine = new StringBuilder();

        try{
            try(InputStream inputStream = socket.getInputStream();
                InputStreamReader inStream = new InputStreamReader(inputStream);
                OutputStream outStream = socket.getOutputStream()){
                while(true) {

                    int currentChar = inStream.read();
                    if (currentChar < 0){
                        break;
                    }

                    if (currentChar == '\n') {
                        System.out.println(oneRequestLine.toString());
                        if (oneRequestLine.toString().equals("\r")){
                            analyze(inStream, outStream);
                            break;
                        }
                        oneRequestLine.append('\n');
                        request += oneRequestLine.toString();
                        oneRequestLine = new StringBuilder();
                        continue;
                    }
                    oneRequestLine.append((char)currentChar);
                }
            }finally {
                socket.close();
            }
        }catch (IOException ioEx){
            throw new RuntimeException(ioEx);
        }
    }

    private void analyze(InputStreamReader inStream, OutputStream outStream) throws IOException{
        String[] requestLines = request.split("\n");
        String[] methodLine = requestLines[0].split(" ");
        Map<String, String> headers = new HashMap<>();

        for(int i = 1; i < requestLines.length; ++i){
            String[] headerData = requestLines[i].split(": ");

            if (headerData.length != 2){
                continue;
            }

            headers.put(headerData[0], headerData[1].substring(0,
                    headerData[1].length()-1));
        }

        switch (methodLine[0]) {
            case "GET": // generate new core.GETRequest
                GETRequest get = new GETRequest(outStream, methodLine[1], headers);
                get.sendResponse();
                break;
            case "POST": // generate new core.POSTRequest
                POSTRequest post = new POSTRequest(inStream, outStream, headers, methodLine[1]);
                post.readFile();
                post.sendResponse();
                break;
            case "PUT": // same as POST but different but still same
                break;
            default: // generate new errorPage (we have no full support)
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
