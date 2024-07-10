package codesquad.adapter;

import codesquad.http.HttpRequest;
import codesquad.http.HttpResponse;

public interface Adapter {
    boolean supports(String path);

    HttpResponse handle(HttpRequest request);
}
