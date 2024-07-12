package codesquad.filter;

import codesquad.http.HttpRequest;
import codesquad.http.HttpResponse;

public interface Filter {
    HttpResponse doFilter(HttpRequest request, FilterChain chain);
}