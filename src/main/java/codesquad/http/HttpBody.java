package codesquad.http;

import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static codesquad.utils.ParserUtils.parseJson;
import static codesquad.utils.ParserUtils.populateObject;

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

    public Parameters getParameters() {
        if (contentType == MimeType.X_WWW_FORM_URLENCODED) {
            String decode = URLDecoder.decode(new String(bytes));
            return Parameters.of(decode);
        }

        // TODO contentType 처리
        return null;
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
            Map<String, Object> stringObjectMap = parseJson(data);
            populateObject(instance, stringObjectMap);
            return instance;
        }

        throw new UnsupportedOperationException("Content type not supported: " + contentType);
    }


}
