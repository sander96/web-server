package core;


import java.io.*;
import java.util.Map;

public class POSTRequest {
    private InputStreamReader inStream;
    private OutputStream outStream;
    private Map<String, String> headers;
    private String filePath;

    public POSTRequest(InputStreamReader inStream, OutputStream outStream, Map<String,
            String> headers, String filePath) {
        this.inStream = inStream;
        this.outStream = outStream;
        this.headers = headers;
        this.filePath = filePath;
    }

    public void readFile() throws IOException{
        // TODO write to disk test
        long contentLength = getContentLength();
        //FileHandler.writeToDisk("FileServer\\staticPath.jpg", inStream, contentLength);
    }

    public void sendResponse() throws IOException{
        // basically should get away by generating new core.GETRequest here
        // because browser needs some kind of response

        GETRequest postResponse = new GETRequest(outStream, filePath, headers);
        postResponse.sendResponse();
    }

    private long getContentLength(){
        String len = headers.get("Content-Length");
        if (len == null){
            return -1;
        }

        return Long.parseLong(len);
    }
}
