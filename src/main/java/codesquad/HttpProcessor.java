package codesquad;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface HttpProcessor {
    HttpRequest parseRequest(InputStream is) throws IOException;

    void createResponse(OutputStream os, HttpResponse response) throws IOException;
}
