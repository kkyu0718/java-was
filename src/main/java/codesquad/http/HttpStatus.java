package codesquad.http;

public enum HttpStatus {
    NOT_FOUND(404, "NOT FOUND"),
    OK(200, "OK"),
    INTERNAL_SERVER_ERROR(500, "INTERNAL SERVER ERROR");

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
}
