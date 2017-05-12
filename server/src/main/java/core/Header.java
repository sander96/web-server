package core;

public class Header {
    private String name;
    private String value;

    public Header(String name, String value) {
        this.name = name.trim();
        this.value = value.trim();
    }

    @Override
    public String toString() {
        return name + ": " + value + "\r\n";
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
