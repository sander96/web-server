package core;

import java.io.*;

public class GETRequest {

    private OutputStream outStream;
    private String filePath;

    public GETRequest(OutputStream outStream, String path){
        this.outStream = outStream;
        filePath = path;
    }

    public void sendResponse() throws IOException{
        if (filePath.equals("/")){
            File doc = new File("index.html");
            byte[] fileBuf = new byte[2048];
            int num;

            try(InputStream inputStream = new FileInputStream(doc)){
                String responseHead = "HTTP/1.1 200 OK\n" +
                        "Content-Length: " + doc.length() + "\n" +
                        "Content-Type: text/html\n\r\n";
                outStream.write(responseHead.getBytes("UTF-8"));

                while(true){
                    num = inputStream.read(fileBuf);
                    if(num < 0){
                        break;
                    }
                    outStream.write(fileBuf);
                }
            }
        }else{
            outStream.write("HTTP/1.1 404 Not Found\r\n".getBytes());
        }
    }
}
