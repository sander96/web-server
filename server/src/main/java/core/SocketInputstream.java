package core;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SocketInputstream extends InputStream {
    private static final int MAX_HEAD_LEN = 1024*100;
    private int contentLength = -1;
    private int headerLength = 0;
    private boolean isHeaderAvailable = true;
    private BufferedInputStream inputStream;
    private byte[] d = new byte[4];

    public SocketInputstream (BufferedInputStream inputStream) {
        this.inputStream = inputStream;
        inputStream.mark(MAX_HEAD_LEN);
    }

    @Override
    public int read(byte[] b) throws IOException {
        if (isHeaderAvailable) {
            byte[] tmp = new byte[b.length];
            int num = inputStream.read(tmp);
            if (num == -1) throw new RuntimeException("Some mysterious error has occurred");

            int delimiterStart = searchDelimiter(tmp, 0, num);

            if (delimiterStart == -1) {

                headerLength += num;
                System.arraycopy(tmp, 0, b, 0, num);
                return num;

            } else {
                headerLength += delimiterStart;
                isHeaderAvailable = false;
                inputStream.reset();
                rollOver(headerLength+1);

                System.arraycopy(tmp, 0, b, 0, delimiterStart);
                return delimiterStart;

            }

        } else if (contentLength > 0) {
            byte[] tmp = new byte[Math.min(b.length, contentLength)];
            int num = inputStream.read(tmp);
            if (num == -1) throw new RuntimeException("Some mysterious error has occurred");

            System.arraycopy(tmp, 0, b, 0, num);
            contentLength -= num;
            return num;

        }

        if (headerLength > MAX_HEAD_LEN) throw new RuntimeException("Error with reading header!");
        return -1;
    }

    public void specifyContentLength(String length) {
        if (length == null) return;
        contentLength = Integer.parseInt(length);
    }

    @Override
    public int read() throws IOException {
        if (isHeaderAvailable || contentLength > 0) {
            return inputStream.read();
        }
        return -1;
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

    private int searchDelimiter(byte[] b, int start, int end) {
        for (int i = start; i < end; i++) {
            d[0] = d[1];
            d[1] = d[2];
            d[2] = d[3];
            d[3] = b[i];

            if (d[0] == '\r' && d[1] == '\n' && d[2] == '\r' && d[3] == '\n') {
                return i;
            }
        }
        return -1;
    }

    private void rollOver(long length) throws IOException{
        long toSkip = length;

        do {
            long actuallySkipped = inputStream.skip(toSkip);
            toSkip -= actuallySkipped;
        } while (toSkip > 0);
    }
}
