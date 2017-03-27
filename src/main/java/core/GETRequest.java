package core;

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
                FileHandler.sendFile("Server\\index.html", outStream);
            }catch (IOException e){
                // TODO elimineerida pornograafia
                outStream.write("HTTP/1.1 404 Not Found\n\r\n".getBytes());
            }
        }else{
            filePath = "Server\\" + filePath.substring(1);
            try{
                FileHandler.sendFile(filePath, outStream);
            } catch (IOException e){
                // TODO elimineerida pornograafia
                outStream.write("HTTP/1.1 404 Not Found\n\r\n".getBytes());
            }
        }
    }
}
