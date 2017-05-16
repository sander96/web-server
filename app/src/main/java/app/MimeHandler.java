package app;

import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MimeHandler {
    private static Map<String, String> types;

    private static void readMimes() throws IOException {
        types = new HashMap<>();
        List<String> lines = Files.readAllLines(Paths.get("data/MIME.txt"), StandardCharsets.UTF_8);

        for (String line : lines) {
            String[] splitLine = line.split("\t");
            types.put(splitLine[0], splitLine[1]);
        }
    }

    public static String getType(String filename) throws IOException {
        if (types == null) {
            readMimes();
        }

        String extension = FilenameUtils.getExtension(String.valueOf(filename)).toLowerCase();
        String test = types.get(extension);
        return test;
    }
}
