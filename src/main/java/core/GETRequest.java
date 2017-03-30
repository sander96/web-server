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
                FileHandler.sendFile("index.html", outStream, headers);
            }catch (IOException e){
                // TODO elimineerida pornograafia
                outStream.write("HTTP/1.1 404 Not Found\n\r\n".getBytes());
            }
        }else{
            try{        // TODO tyhikud failinimes
                System.out.println(filePath);
                if(FileHandler.isFolder(filePath.substring(1))){
                    DynamicPage page = new DynamicPage();

                    String newPage = page.createPage(FileHandler.getDirectory(filePath), filePath);
                    outStream.write(("HTTP/1.1 200 OK\n" +
                            "Content-Length: " + newPage.getBytes().length + "\n" +
                            "Content-Type:" + "text/html" + "\n\r\n").getBytes("UTF-8"));

                    outStream.write(newPage.getBytes("UTF-8"));
                }else{
                    filePath = filePath.substring(1);
                    System.out.println(filePath);

                    try{
                        FileHandler.sendFile(filePath, outStream, headers);
                    } catch (Exception e){
                        // TODO elimineerida pornograafia
                        outStream.write("HTTP/1.1 404 Not Found\n\r\n".getBytes());
                    }
                }
            } catch (AccessRestrictedException e) {     // TODO fix exceptions

            } catch (Exception e) {
            }
        }
    }
}
