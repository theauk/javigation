package bfst21.file_io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProgressInputStream extends FilterInputStream {
    private StreamListener streamListener;
    private long totalBytes = 1;

    public ProgressInputStream(InputStream inputStream) {
        super(inputStream);
    }

    public void addInputStreamListener(StreamListener streamListener) {
        this.streamListener = streamListener;
    }

    @Override
    public int read() throws IOException {
        int count = in.read();
        if (count != -1) {
            totalBytes += in.read();
            streamListener.onBytesTouched(totalBytes);
        }

        return count;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int count = super.read(b);
        totalBytes += count;
        streamListener.onBytesTouched(totalBytes);
        return count;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int count = super.read(b, off, len);
        totalBytes += count;
        streamListener.onBytesTouched(totalBytes);
        return count;
    }
}