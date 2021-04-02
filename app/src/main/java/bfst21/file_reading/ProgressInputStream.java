package bfst21.file_reading;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProgressInputStream extends FilterInputStream {
    private InputStreamListener inputStreamListener;
    private long totalBytes = 1;

    public ProgressInputStream(InputStream inputStream) {
        super(inputStream);
    }

    public void addInputStreamListener(InputStreamListener inputStreamListener) {
        this.inputStreamListener = inputStreamListener;
    }

    @Override
    public int read() throws IOException {
        int count = in.read();
        if (count != -1) {
            totalBytes += in.read();
            inputStreamListener.onBytesRead(totalBytes);
        }

        return count;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int count = super.read(b);
        totalBytes += count;
        inputStreamListener.onBytesRead(totalBytes);
        return count;
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
        int count = super.read(b, off, len);
        totalBytes += count;
        inputStreamListener.onBytesRead(totalBytes);
        return count;
    }
}
