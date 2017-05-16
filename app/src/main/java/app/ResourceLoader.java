package app;

import core.ResponseHandler;

import java.io.IOException;
import java.io.InputStream;

public class ResourceLoader {
    public static String loadTemplate(ResponseHandler handler, String filename) throws IOException {
        byte[] buffer = new byte[1024];
        StringBuilder builder = new StringBuilder();

        try (InputStream fileInputStream = handler.getClass().getClassLoader()
                .getResourceAsStream("WebContent\\" + filename)) {

            if (fileInputStream == null) return null;

            while (true) {
                int bytesRead = fileInputStream.read(buffer);
                if (bytesRead == -1) break;

                builder.append(new String(buffer, 0, bytesRead, "UTF-8"));
            }
        }

        return builder.toString();
    }
}
