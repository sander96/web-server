package app;

import core.Method;
import core.ResponseHandler;
import core.SpecializedInputreader;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Map;

public class POSTRequest implements ResponseHandler{

    @Override
    public void sendResponse(Map<String, String> headers, Method method, Path path, String scheme, Map<String, String> pathParams,
                             Map<String, String> queryParams, SpecializedInputreader inputreader, OutputStream outputStream) throws IOException {

    }

    @Override
    public Method getKey() {
        return Method.POST;
    }
}
