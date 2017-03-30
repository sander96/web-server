package core;

import serverexception.AccessRestrictedException;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FileHandler {
    public static synchronized void sendFile(String path, OutputStream outputStream,
                    Map<String, String> headers) throws FileNotFoundException{

        path = path.substring(1);
        File file = new File(path);
        String contentType;

        if (file.getName().endsWith(".html")){
            contentType = "text/html";
        }else if (file.getName().endsWith(".css")) {
            contentType = "text/css";
        } else {
            contentType = "multipart/form-data";
        }

        try {
            checkServerDirectory(file);
        } catch (AccessRestrictedException accEx){
            //TODO implementeerida normaalne handling
            throw new RuntimeException("Access restricted");
        }

        byte[] buffer = new byte[1024];
        FileInputStream fileStream = new FileInputStream(file);
        String responseHeader = "HTTP/1.1 200 OK\n" +
                "Content-Length:" + file.length() + "\n" +
                "Content-Type: " + contentType + "; charset=utf-8\n\r\n";

        try {
            outputStream.write(responseHeader.getBytes("UTF-8"));
            while (true) {
                int size = fileStream.read(buffer);

                if (size == -1) {
                    break;
                }
                outputStream.write(buffer, 0, size);
            }
        }catch (IOException ioEx){
            throw new RuntimeException("Something went wrong with file sending process");
        }
    }

    public static synchronized void writeToDisk(String path, InputStream inputStream,
                                                long contentLength) throws IOException {
        // TODO check if the file gets overwritten, 403 forbidden

        try(FileOutputStream fileWriter = new FileOutputStream(new File(path))){
            byte[] buffer = new byte[1024];
            long fileLength = contentLength;

            while(fileLength > 0){
                int bytesRead = inputStream.read(buffer);

                if (bytesRead == -1){
                    throw new RuntimeException("Client closed connection unexpectedly.");
                }
                fileWriter.write(buffer, 0, bytesRead);

                fileLength -= bytesRead;
            }
        }
    }

    public static synchronized void deleteFile(String path) throws Exception {
        File file = new File(path.substring(1));
        checkServerDirectory(file);
        file.delete();
    }

    private static void checkServerDirectory(File file) throws FileNotFoundException, AccessRestrictedException {
        if(!file.exists()){
            throw new FileNotFoundException("\"" + file.getPath() + "\" does not exist.");
        }

        if(file.isAbsolute()){
            throw new AccessRestrictedException();  // TODO 400 Bad Request
        }
    }
}
