package codesquad.http;

import codesquad.utils.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HttpHeaders {
    private final Map<String, String> headers;

    public static String HTTP_VERSION = "Http-Version";
    public static String PATH = "Path";
    public static String HTTP_METHOD = "Http-Method";
    public static String CONTENT_TYPE = "Content-Type";
    public static String CONTENT_LENGTH = "Content-Length";
    public static String HOST = "Host";

    public HttpHeaders() {
        this.headers = new HashMap<>();
    }

    public void put(String key, String value) {
        headers.put(key, value);
    }

    public String get(String key) {
        return headers.get(key);
    }

    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    public int size() {
        return headers.size();
    }

    public boolean contains(String key) {
        return headers.containsKey(key);
    }

    public Set<String> keySet() {
        return headers.keySet();
    }

    public void extend(HttpHeaders others) {
        this.headers.putAll(others.getHeaders());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String key : headers.keySet()) {
            sb.append(String.format("%s : %s", key, headers.get(key)))
                    .append(StringUtils.LINE_SEPERATOR);
        }
        return sb.toString();
    }
}
