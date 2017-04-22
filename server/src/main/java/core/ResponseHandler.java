package core;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Map;

public interface ResponseHandler {

    void sendResponse(Map<String, String> headers, Method method, Path path, String scheme, Map<String, String> pathParams,
                      Map<String, String> queryParams, SpecializedInputreader inputreader, OutputStream outputStream) throws IOException;

    Method getKey();
}
