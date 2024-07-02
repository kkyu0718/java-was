package codesquad.http;

public class HttpBody {
    private byte[] bytes; //TODO mime type 에 따른 구현 필요

    public HttpBody(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }

    @Override
    public String toString() {
        return new String(bytes);
    }
}
