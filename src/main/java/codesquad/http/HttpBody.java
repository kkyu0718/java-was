package codesquad.http;

import java.net.URLDecoder;

public class HttpBody {
    private byte[] bytes;
    private MimeType contentType;

    public HttpBody(byte[] bytes, MimeType contentType) {
        this.bytes = bytes;
        this.contentType = contentType;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public Parameters getParameters() {
        if (contentType == MimeType.X_WWW_FORM_URLENCODED) {
            String decode = URLDecoder.decode(new String(bytes));
            return Parameters.of(decode);
        }

        //TODO contentType 처리
        return null;
    }

    @Override
    public String toString() {
        return new String(bytes);
    }
}
