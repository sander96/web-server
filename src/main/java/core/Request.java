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
        String oneRequestLine = "";

        try{
            try(InputStream inStream = socket.getInputStream();
                OutputStream outStream = socket.getOutputStream()){
                while(true) {
//                    byte[] buffer = new byte[1024];
//                    int size = inStream.read(buffer);
//
//                    if (size < 0){
//                        // send bad request error? (or not)
//                        break;
//                    }
//
//                    String tmp = new String(buffer, 0, size, "UTF-8");
//                    request += tmp;
//                    System.out.println(tmp);
//
//                    if (size > 3 && new String(buffer, size-3, 3, "UTF-8").equals("\n\r\n")){
//                        // must ascertain whether request has a body (meaning (for now) if POST or GET)
//                        analyze(inStream, outStream);
//                        break;
//                    }

                    // NB! Ma pidin selle rampe ümber ehitama, sest post requesti
                    // korral eelnev kood ei funktsioneerinud võid ise proovida
                    // ma sellepärast ei kustutanud vana asja veel ära aga kui
                    // sa järgi checkid siis võid sellest vabaneda

                    // muidugi uus kood loeb request headi sisse ühe baidi kaupa
                    // aga kuna see (request head) on nii lühike siis vahet üldiselt pole
                    int nextByte = inStream.read();
                    if (nextByte < 0){
                        // send bad request error? (or not)
                        // because inStream should never reach end of client input stream
                        // maybe new IOException or something similar
                        break;
                    }

                    oneRequestLine += (char)nextByte;
                    if (nextByte == '\n'){
                        request += oneRequestLine;
                        System.out.print(oneRequestLine);
                        if (oneRequestLine.equals("\r\n")){
                            analyze(inStream, outStream);
                            break;
                        }
                        oneRequestLine = "";
                    }
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

            if (headerData.length != 2){
                continue;
            }

            headers.put(headerData[0], headerData[1].substring(0,
                    headerData[1].length()-1));
        }

        switch (methodLine[0]) {
            case "GET": // generate new core.GETRequest
                GETRequest get = new GETRequest(outStream, methodLine[1]);
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
