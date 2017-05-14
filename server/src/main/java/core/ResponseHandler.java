package core;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;

public interface ResponseHandler {

    void sendResponse(Request request, SocketInputstream inputStream, OutputStream outputStream) throws IOException, SQLException;

    //Method getKey();
    String getKey();
}
