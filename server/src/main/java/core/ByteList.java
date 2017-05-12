package core;

public class ByteList {
    private byte[] list = new byte[0];
    private int size = 0;

    public void add(byte[] data, int startPos, int len) {
        if (list.length <= len+size) {
            byte[] tmp = new byte[(size+len)*2];
            System.arraycopy(list, 0, tmp, 0, size);
            System.arraycopy(data, startPos, tmp, size, len);
            list = tmp;
            size += len;
        } else {
            System.arraycopy(data, startPos, list, size, len);
            size += len;
        }
    }

    public byte[] getList() {
        byte[] resized = new byte[size];
        System.arraycopy(list, 0, resized, 0, size);
        return resized;
    }

    public int getSize() {
        return size;
    }

    public void clear() {
        size = 0;
        list = new byte[size];
    }
}
