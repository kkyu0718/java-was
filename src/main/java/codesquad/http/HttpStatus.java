package codesquad.http;

public enum HttpStatus {
    NOT_FOUND(404, "NOT FOUND"),
    OK(200, "OK"),
    NO_CONTENT(204, "NO CONTENT"),
    FOUND(302, "FOUND"),
    ILLEGAL_ARGUMENT(400, "ILLEGAL ARGUMENT"),
    INTERNAL_SERVER_ERROR(500, "INTERNAL SERVER ERROR"),
    METHOD_NOT_ALLOWED(405, "METHOD NOT ALLOWED");

    private int statusCode;
    private String message;

    HttpStatus(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "HttpStatus{" +
                "statusCode=" + statusCode +
                ", message='" + message + '\'' +
                '}';
    }
}
