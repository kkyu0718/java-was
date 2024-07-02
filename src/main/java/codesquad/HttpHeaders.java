package codesquad;

import codesquad.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class HttpHeaders {
    private final Map<String, String> headers;

    public static String HTTP_VERSION = "Http-Version";
    public static String PATH = "Path";
    public static String HTTP_METHOD = "Http-Method";
    public static String CONTENT_TYPE = "Content-Type";

    public HttpHeaders() {
        this.headers = new HashMap<>();
    }

    public void put(String key, String value) {
        headers.put(key, value);
    }

    public String get(String key) {
        return headers.get(key);
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
