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
            File doc = new File("index.html");
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
        }else{
            try {
                //FileHandler.deleteFile("\\Files\\Folder\\delete.txt");

//                List<String> files = new ArrayList<>(FileHandler.getDirectory(filePath));     // may throw an exception; where should it be handled?
//                StringBuilder response = new StringBuilder();
//
//                for(String fileName : files){
//                    response.append(fileName + "\n");
//                }

                System.out.println("TERE");

                FileHandler.sendFile("\\Files\\sloth.jpg", outStream);
//                String responseHead = "HTTP/1.1 200 OK\n" +
//                        "Content-Length: " + response.length() + "\n" +
//                        "Content-Type: text/html\n\r\n";
//
//                outStream.write(responseHead.getBytes("UTF-8"));
//                outStream.write(response.toString().getBytes("UTF-8"));     // just an example...
            } catch (Exception e) {
                outStream.write("HTTP/1.1 404 Not Found\n\r\n".getBytes());
            }
        }
    }
}
