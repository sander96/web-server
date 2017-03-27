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
            File doc = new File("Server\\index.html");
            byte[] fileBuffer = new byte[2048];

            try(InputStream inputStream = new FileInputStream(doc)){
                String responseHead = "HTTP/1.1 200 OK\n" +
                        "Content-Length: " + doc.length() + "\n" +
                        "Content-Type: text/html\n\r\n";

                outStream.write(responseHead.getBytes("UTF-8"));

                while(true){
                    if(inputStream.read(fileBuffer) == -1){
                        break;
                    }

                    outStream.write(fileBuffer);
                }
            }
        }else if(filePath.equals("/favicon.ico")){
            try {
                FileHandler.sendFile("\\Server\\favicon.ico", outStream);
            } catch (Exception e) {
                outStream.write("HTTP/1.1 404 Not Found\n\r\n".getBytes());
            }
        }else if(filePath.equals("/default.css")){
            try {
                FileHandler.sendFile("\\Server\\default.css", outStream);
            } catch (Exception e) {
                outStream.write("HTTP/1.1 404 Not Found\n\r\n".getBytes());
            }
        }else{
            try {
                FileHandler.sendFile("\\Server\\FileServer\\sloth.jpg", outStream);
            } catch (Exception e) {
                outStream.write("HTTP/1.1 404 Not Found\n\r\n".getBytes());
            }
        }
    }
}
