package core;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public interface ResponseHandler {

    void sendResponse(Request request, SocketInputstream inputStream, OutputStream outputStream) throws IOException, SQLException;

    Method getKey();
}
