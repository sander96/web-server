package core;

import serverexception.AccessRestrictedException;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileHandler {
    public static synchronized void sendFile(String path, OutputStream outputStream)
            throws IOException{ //TODO panna siia mingi normaalne exception ka veel
        File file = new File(path);
        String name = file.getName();
        System.out.println("NAMEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE " + name);
        String[] nameParams = name.split("\\p{Punct}");
        String fileType = nameParams[1];

        try {
            checkServerDirectory(file);
        } catch (AccessRestrictedException accEx){
            //TODO implementeerida normaalne handling
            throw new RuntimeException("Access restricted");
        }

        byte[] buffer = new byte[4 * 1024];
        FileInputStream fileStream = new FileInputStream(file);

        String responseHeader = "HTTP/1.1 200 OK\n" +
                "Content-Length: " + file.length() + "\n" +
                "Content-Type: text/" +  fileType + "\n\r\n";

        outputStream.write(responseHeader.getBytes("UTF-8"));

        while(true){
            int size = fileStream.read(buffer);

            if(size == -1){
                break;
            }

            outputStream.write(buffer, 0, size);
        }
    }

    public static synchronized void writeToDisk(String path, InputStreamReader inputStream,
                                                long contentLength) throws IOException {
        // TODO check if the file gets overwritten

        try(PrintWriter fileWriter = new PrintWriter(new File(path))){
            char[] buffer = new char[1024];
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

    private static void checkServerDirectory(File file) throws FileNotFoundException,AccessRestrictedException {
        if(!file.exists()){
            throw new FileNotFoundException("\"" + file.getPath() + "\" does not exist.");
        }

        if(file.isAbsolute()){
            throw new AccessRestrictedException();
        }
    }
}
