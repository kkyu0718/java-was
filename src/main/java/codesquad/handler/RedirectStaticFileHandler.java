package codesquad.handler;

import codesquad.global.Path;
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
    private List<Path> whitelist;

    public RedirectStaticFileHandler(StaticFileReaderSpec staticFileReader, List<Path> whitelist) {
        this.staticFileReader = staticFileReader;
        this.whitelist = whitelist;
    }

    @Override
    public HttpResponse handle(HttpRequest request) {
        Path path = request.getPath();
        Path indexPath = new Path(path.toString() + "/index.html");
        try {
            if (!staticFileReader.exists(indexPath)) {
                return createNotFoundResponse(request);
            }
        } catch (IOException e) {
            throw new RuntimeException("error reading file " + indexPath);
        }

        return readFileAndCreateResponse(indexPath, request);
    }

    private HttpResponse readFileAndCreateResponse(Path path, HttpRequest request) {
        try {
            byte[] bytes = staticFileReader.readFile(path);
            MimeType contentType = MimeType.fromExt(path);
            return createOkResponse(request, bytes, contentType);
        } catch (IOException ex) {
            logger.error("Error reading file: " + path);
            return createErrorResponse(request);
        }
    }

    @Override
    public boolean canHandle(HttpRequest request) {
        Path path = request.getPath();
        return whitelist.contains(path);
    }
}
