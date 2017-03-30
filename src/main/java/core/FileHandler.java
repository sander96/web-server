package core;

import serverexception.AccessRestrictedException;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FileHandler {
    public static synchronized void sendFile(String path, OutputStream outputStream, Map<String, String> headers)
            throws IOException, RuntimeException{ //TODO panna siia mingi normaalne exception ka veel
        File file = new File(path);

        //System.out.println(headers.get("Accept"));
        //String[] acceptable = headers.get("Accept").split(",");     // TODO vigane, kuid algsed asjad t88tavad

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
                "Content-Type: multipart/form-data\n" +
                "Content-Disposition: attachment; filename=" + path + "\n\r\n";

        outputStream.write(responseHeader.getBytes("UTF-8"));

        while(true){
            int size = fileStream.read(buffer);

            if(size == -1){
                break;
            }

            outputStream.write(buffer, 0, size);
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

    public static synchronized List<String> getDirectory(String path) throws Exception {
        File file = new File(path.substring(1));
        checkServerDirectory(file);

        List<String> fileNames = Arrays.asList(file.list());


        return new ArrayList<String>(Arrays.asList(file.list()));
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

    public static boolean isFolder(String path) throws FileNotFoundException, AccessRestrictedException {
        File file = new File(path);
        checkServerDirectory(file);

        return !file.isFile();      // TODO vigane
    }
}
