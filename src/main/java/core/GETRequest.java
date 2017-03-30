package core;

import serverexception.AccessRestrictedException;

import java.io.*;
import java.util.Map;

public class GETRequest {
    private OutputStream outStream;
    private String filePath;
    private Map<String, String> headers;

    public GETRequest(OutputStream outStream, String path, Map<String, String> headers){
        this.outStream = outStream;
        this.filePath = path;
        this.headers = headers;
    }

    public void sendResponse() throws IOException{
        if (filePath.equals("/")){
            try{
                FileHandler.sendFile("/index.html", outStream, headers);
            }catch (FileNotFoundException fnfEx){
                // TODO elimineerida pornograafia
                outStream.write("HTTP/1.1 404 Not Found\n\r\n".getBytes());
            }
        }else{
            try{
                if (filePath.endsWith("/")){
                    byte[] payload = new DynamicPage().createPage(filePath.substring(1)).getBytes();
                    int payloadLen = payload.length;
                    String headers = "HTTP/1.1 200 OK\n" +
                            "Content-Type: text/html\n" +
                            "Content-Length: " + payloadLen + "\n\r\n";

                    outStream.write(headers.getBytes());
                    outStream.write(payload);
                }else{
                    FileHandler.sendFile(filePath, outStream, headers);
                }
            }catch (FileNotFoundException fnfEx){
                // TODO elimineerida pornograafia
                outStream.write("HTTP/1.1 404 Not Found\n\r\n".getBytes());
            }
        }
    }
}
