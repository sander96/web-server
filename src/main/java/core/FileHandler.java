package core;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileHandler {
    public static synchronized void sendFile(String path, OutputStream outputStream) throws Exception {
        File file = new File(path.substring(1));
        checkServerDirectory(file);

        byte[] buffer = new byte[4 * 1024];
        FileInputStream fileStream = new FileInputStream(file);

        String responseHeader = "HTTP/1.1 200 OK\n" +
                "Content-Length: " + file.length() + "\n" +
                "Content-Type: text/css\n" +
                "Content-Disposition: attachment; filename=\"default.css\"\n\r\n";

        outputStream.write(responseHeader.getBytes("UTF-8"));

        while(true){
            int size = fileStream.read(buffer);

            if(size == -1){
                break;
            }

            outputStream.write(buffer);
        }
    }

    public static synchronized void writeToDisk(String path, DataInputStream inputStream, long contentLength) throws IOException {
        // TODO check if the file gets overwritten

        FileOutputStream fileOutputStream = new FileOutputStream(path);
        byte[] buffer = new byte[1024];

        while(true){
            int bytesRead = inputStream.read(buffer);

            if (bytesRead == -1){
                // must throw some kind of exception ???
                break;
            }

            fileOutputStream.write(buffer);


            contentLength -= bytesRead;
        }
    }

    public static synchronized List<String> getDirectory(String path) throws Exception {
        File file = new File(path.substring(1));
        checkServerDirectory(file);

        return new ArrayList<String>(Arrays.asList(file.list()));
    }

    public static synchronized void deleteFile(String path) throws Exception {
        File file = new File(path.substring(1));
        checkServerDirectory(file);
        file.delete();
    }

    private static void checkServerDirectory(File file) throws Exception {
        if(!file.exists()){
            throw new FileNotFoundException("\"" + file.getPath() + "\" does not exist.");
        }

        if(file.isAbsolute()){
            throw new Exception("Client tried to access a file that might be outside the server folder.");
        }
    }
}
