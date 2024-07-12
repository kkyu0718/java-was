package codesquad.handler;

import codesquad.http.HttpRequest;
import codesquad.http.HttpResponse;
import codesquad.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RedirectStaticFileHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(RedirectStaticFileHandler.class);
    private List<String> whitelist;


    public RedirectStaticFileHandler(
            List<String> whitelist
    ) {
        this.whitelist = whitelist;
    }

    @Override
    public HttpResponse handle(HttpRequest request) {
        String indexPath = request.getPath().equals("/") ? request.getPath() + "index.html" : request.getPath() + "/index.html";
        logger.debug("send redirect response to " + indexPath);

        return new HttpResponse.Builder(request, HttpStatus.FOUND)
                .redirect(indexPath)
                .build();
    }

    @Override
    public boolean canHandle(HttpRequest request) {
        return whitelist.contains(request.getPath());
    }
}
