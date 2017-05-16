package app;

import core.*;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StyleSheetHandler implements ResponseHandler {
    @Override
    public void sendResponse(Request request, SocketInputstream inputStream, OutputStream outputStream) throws IOException, SQLException {
        String fileName = request.getPath().substring(request.getPath().lastIndexOf("/") + 1);

        String page = ResourceLoader.loadTemplate(this, fileName);
        if (page == null) {
            ResponseHead.sendResponseHead(outputStream, request.getScheme(), StatusCode.NOT_FOUND, null);
            return;
        }

        byte[] styleSheet = page.getBytes();
        List<Header> headers = new ArrayList<>();
        headers.add(new Header("Content-Type", "text/css"));
        headers.add(new Header("Content-Length", String.valueOf(styleSheet.length)));

        ResponseHead.sendResponseHead(outputStream, request.getScheme(), StatusCode.OK, headers);
        outputStream.write(styleSheet);
    }

    @Override
    public String getKey() {
        return "/stylesheets/*";
    }
}
