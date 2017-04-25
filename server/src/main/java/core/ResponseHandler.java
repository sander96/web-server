package core;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public interface ResponseHandler {

    void sendResponse(Map<String, String> headers, Method method, Path path, String scheme, Map<String, String> pathParams,
                      Map<String, String> queryParams, BufferedInputStream inputStream, OutputStream outputStream, Connection connection) throws IOException, SQLException;

    Method getKey();
}
