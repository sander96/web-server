package core;


import java.io.*;
import java.util.Map;

public class POSTRequest {
    private InputStream inStream;
    private OutputStream outStream;
    private Map<String, String> headers;
    private String filePath;

    public POSTRequest(InputStream inStream, OutputStream outStream, Map<String,
            String> headers, String filePath) {
        this.inStream = inStream;
        this.outStream = outStream;
        this.headers = headers;
        this.filePath = filePath;
    }

    public void readFile() throws IOException{
        long contentLength = getContentLength();
        byte[] buffer = new byte[1024];

        while(contentLength > 0){
            int bytesRead = inStream.read(buffer);
            if (bytesRead < 0){
                // must throw some kind of exception
                break;
            }
            // method body comes here because something must happen to
            // the file sent by client

            // ma arvan, et siin peaks kasutama core.FileHandler'it aga pole kindel kuidas
            // hetkel lihtsalt tetimiseks see sout
            System.out.println(new String(buffer, 0, bytesRead, "UTF-8"));

            contentLength -= bytesRead;
        }
    }

    public void sendResponse() throws IOException{
        // basically should get away by generating new core.GETRequest here
        // because browser needs some kind of response

        GETRequest postResponse = new GETRequest(outStream, filePath);
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
