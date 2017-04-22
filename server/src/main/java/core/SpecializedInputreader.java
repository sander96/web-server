package core;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class SpecializedInputreader implements Closeable {
    private static final int MAX_HEAD_LENGTH = 1024 * 100; // aka 100 KB
    private static final int STANDARD_READ_OP_LEN = 2048;
    private byte[] unusedBytes = new byte[0];
    private int unusedBytesCounter = 0;
    private InputStream inputStream;

    public SpecializedInputreader(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public byte[] read() throws IOException {
        byte[] buffer = new byte[STANDARD_READ_OP_LEN - unusedBytesCounter];
        int bytesRead = inputStream.read(buffer);
        if (bytesRead < 0) throw new RuntimeException("Error with read operation");

        byte[] endResult = new byte[unusedBytesCounter + bytesRead];
        if (unusedBytesCounter > 0) {
            System.arraycopy(unusedBytes, 0, endResult, 0, unusedBytesCounter);
            unusedBytesCounter = 0;
            unusedBytes = new byte[0];
        }
        System.arraycopy(buffer, 0, endResult, unusedBytesCounter, bytesRead);
        return endResult;
    }

    public byte[] read(byte[] delimiterSequence) throws IOException {
        ByteList endResult = new ByteList();

        while (true) {
            byte[] tmp = read();
            int delimiterStart = checkDelimiterSequence(tmp, delimiterSequence, 0);
            if (delimiterStart >= 0) {
                endResult.add(tmp, 0, delimiterStart);
                unusedBytesCounter = tmp.length - delimiterStart - delimiterSequence.length;
                unusedBytes = new byte[unusedBytesCounter];
                System.arraycopy(tmp, delimiterStart + delimiterSequence.length, unusedBytes, 0, unusedBytesCounter);
                break;
            }
            endResult.add(tmp, 0, tmp.length);

            if (endResult.getSize() > MAX_HEAD_LENGTH) {
                throw new RuntimeException("Request head too big");
            }
        }
        return endResult.getList();
    }

    public boolean read(ByteList dest, byte[] delimiter) throws IOException {
        byte[] buffer = new byte[STANDARD_READ_OP_LEN];
        int bytesRead = inputStream.read(buffer, unusedBytesCounter, STANDARD_READ_OP_LEN-unusedBytesCounter);
        dest.clear();

        if (unusedBytesCounter > 0) {
            System.arraycopy(unusedBytes, 0, buffer, 0, unusedBytesCounter);
            unusedBytesCounter = 0;
            unusedBytes = new byte[0];
        }

        int delimiterStart = checkDelimiterSequence(buffer, delimiter, 0);

        if (delimiterStart >= 0) {
            dest.add(buffer, 0, delimiterStart);
            unusedBytesCounter = buffer.length-delimiterStart-delimiter.length;
            unusedBytes = new byte[unusedBytesCounter];
            System.arraycopy(buffer, delimiterStart + delimiter.length, unusedBytes, 0, unusedBytesCounter);
            return true;
        }

        dest.add(buffer, 0, bytesRead);
        return false;
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

    private int checkDelimiterSequence(byte[] data, byte[] sequence, int startPos) {
        if (sequence.length > data.length) throw new RuntimeException("Sequence longer than data array");

        Outerloop:
        for (int i = startPos; i <= data.length - sequence.length; i++) {
            for (int j = 0; j < sequence.length; j++) {
                if (data[i + j] != sequence[j]) {
                    continue Outerloop;
                }
            }
            return i;
        }
        return -1;
    }
}
