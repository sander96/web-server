package core;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileHandler {
    public static synchronized void readFile(){

    }

    public static synchronized void writeFile(){

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
