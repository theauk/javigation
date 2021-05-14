package bfst21.file_io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An InputStream which keeps track of how many bytes it has read.
 * It contains a functional interface {@link StreamListener} which can be used to receive updates while reading a file.
 */
public class ProgressInputStream extends FilterInputStream {
    private StreamListener streamListener;
    private long totalBytes = 1;

    /**
     * <p>Default constructor.<p>
     * <p>Requires an {@link InputStream} to observe.</p>
     *
     * @param inputStream the {@link InputStream} to be observed.
     */
    public ProgressInputStream(InputStream inputStream) {
        super(inputStream);
    }

    /**
     * Adds a {@link StreamListener} to the InputStream.
     *
     * @param streamListener the StreamListener to be added.
     */
    public void addInputStreamListener(StreamListener streamListener) {
        this.streamListener = streamListener;
    }

    /**
     * Overrides default {@link FilterInputStream#read()} to count the number of bytes read from the {@link InputStream}.
     *
     * @return the number of bytes read.
     * @throws IOException if the stream is interrupted.
     */
    @Override
    public int read() throws IOException {
        int count = in.read();
        if (count != -1) {
            totalBytes += in.read();
            streamListener.onBytesTouched(totalBytes);
        }

        return count;
    }

    /**
     * Overrides default {@link FilterInputStream#read(byte[])} to count the number of bytes read from the {@link InputStream}.
     *
     * @param b the buffer which length contains the bytes read.
     * @return the number of bytes read.
     * @throws IOException if the stream is interrupted.
     */
    @Override
    public int read(byte[] b) throws IOException {
        int count = super.read(b);
        totalBytes += count;
        streamListener.onBytesTouched(totalBytes);
        return count;
    }

    /**
     * Overrides default {@link FilterInputStream#read(byte[], int, int)} to count the number of bytes read from the {@link InputStream}.
     * It reads up to len bytes of data from this input stream into an array of bytes.
     * If len is not zero, the method blocks until some input is available; otherwise, no bytes are read and 0 is returned.
     *
     * @param b the buffer which length contains the number of bytes read.
     * @param off the start offset in the destination array b.
     * @param len the maximum number of bytes read.
     * @return the number of bytes read.
     * @throws IOException if the stream is interrupted.
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int count = super.read(b, off, len);
        totalBytes += count;
        streamListener.onBytesTouched(totalBytes);
        return count;
    }
}