package codesquad.handler;

import codesquad.http.HttpRequest;
import codesquad.http.HttpResponse;
import codesquad.http.MimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import static codesquad.http.HttpResponse.*;

public class RedirectStaticFileHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(RedirectStaticFileHandler.class);
    private StaticFileReaderSpec staticFileReader;
    private List<String> whitelist;

    public RedirectStaticFileHandler(StaticFileReaderSpec staticFileReader, List<String> whitelist) {
        this.staticFileReader = staticFileReader;
        this.whitelist = whitelist;
    }

    @Override
    public HttpResponse handle(HttpRequest request) {
        String path = request.getPath();
        String indexPath = path + "/index.html";
        try {
            if (!staticFileReader.exists(indexPath)) {
                return createNotFoundResponse(request);
            }
        } catch (IOException e) {
            throw new RuntimeException("error reading file " + indexPath);
        }

        return readFileAndCreateResponse(indexPath, request);
    }

    private HttpResponse readFileAndCreateResponse(String path, HttpRequest request) {
        try {
            byte[] bytes = staticFileReader.readFile(path);
            MimeType contentType = MimeType.fromExt("html");
            return createOkResponse(request, bytes, contentType);
        } catch (IOException ex) {
            logger.error("Error reading file: " + path);
            return createErrorResponse(request);
        }
    }

    @Override
    public boolean canHandle(HttpRequest request) {
        return whitelist.contains(request.getPath());
    }
}
