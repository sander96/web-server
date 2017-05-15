package core;


public enum StatusCode {
    OK(200, "OK"),
    FOUND(302, "Found"),
    BAD_REQUEST(400, "Bad Request"),
    NOT_FOUND(404, "Not found");

    private String status;
    private int code;

    StatusCode(int code, String status) {
        this.code = code;
        this.status = status;
    }

    @Override
    public String toString() {
        return String.valueOf(code) + " " + status;
    }
}
