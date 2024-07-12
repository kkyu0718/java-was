package codesquad.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class HttpCookies {
    private List<HttpCookie> cookies;

    public HttpCookies() {
        this.cookies = new ArrayList<>();
    }

    public void setCookie(HttpCookie cookie) {
        cookies.add(cookie);
    }

    public Optional<HttpCookie> getCookie(String key) {
        return cookies.stream().filter(httpCookie -> httpCookie.getName().equals(key)).findAny();
    }

    public List<HttpCookie> getCookies() {
        return Collections.unmodifiableList(cookies);
    }

    public boolean contains(String key) {
        return cookies.stream().anyMatch(httpCookie -> httpCookie.getName().equals(key));
    }

    public void extend(HttpCookies other) {
        this.cookies.addAll(other.getCookies());
    }

    public boolean isEmpty() {
        return cookies.isEmpty();
    }
}
