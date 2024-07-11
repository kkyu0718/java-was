package codesquad.filter;

import codesquad.http.HttpRequest;
import codesquad.http.HttpResponse;

import java.util.ArrayList;
import java.util.List;

public class FilterChain {
    private List<Filter> filters = new ArrayList<>();
    private int currentFilterIndex = 0;

    public void addFilter(Filter filter) {
        filters.add(filter);
    }

    public HttpResponse doFilter(HttpRequest request) {
        if (currentFilterIndex < filters.size()) {
            Filter filter = filters.get(currentFilterIndex);
            currentFilterIndex++;
            return filter.doFilter(request, this);
        } else {
            return null; // 모든 필터를 통과했음을 나타냄
        }
    }
}