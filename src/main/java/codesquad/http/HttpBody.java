package codesquad.http;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;

import static codesquad.utils.ParserUtils.parseJson;
import static codesquad.utils.ParserUtils.parseXWWWFormUrlEncoded;

public class HttpBody {
    private byte[] bytes;
    private MimeType contentType;

    private HttpBody(byte[] bytes, MimeType contentType) {
        this.bytes = bytes;
        this.contentType = contentType;
    }

    public static HttpBody of(byte[] bytes, MimeType contentType) {
        return new HttpBody(bytes, contentType);
    }

    public static HttpBody empty() {
        return new HttpBody(new byte[0], null);
    }

    public byte[] getBytes() {
        return bytes;
    }

    @Override
    public String toString() {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public boolean isEmpty() {
        return bytes.length == 0;
    }

    public <T> T parse(Class<T> clazz) {
        String jsonString = new String(bytes, StandardCharsets.UTF_8);
        return parseByContentType(jsonString, contentType, clazz);
    }

    private <T> T parseByContentType(String data, MimeType contentType, Class<T> clazz) {
        T instance;
        try {
            instance = clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException("Error creating new instance", e);
        }

        if (contentType == MimeType.APPLICATION_JSON) {
            return parseJson(data, clazz);
        } else if (contentType == MimeType.X_WWW_FORM_URLENCODED) {
            return parseXWWWFormUrlEncoded(data, clazz);
        } else {
            throw new UnsupportedOperationException("Content type not supported: " + contentType);
        }

    }


}
