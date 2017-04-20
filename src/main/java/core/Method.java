package core;

public enum  Method {
    GET, POST;

    public static Method getMethod(String method) {
        switch (method) {
            case "GET":
                return GET;
            case "POST":
                return POST;
            default:
                return null;
        }
    }
}
