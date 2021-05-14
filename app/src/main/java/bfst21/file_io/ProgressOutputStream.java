package bfst21.file_io;

import java.io.*;

/**
 * An OutputStream which keeps track of how many bytes it has written.
 * It contains a functional interface {@link StreamListener} which can be used to receive updates while writing a file.
 */
public class ProgressOutputStream extends FilterOutputStream {
    private StreamListener streamListener;
    private long totalBytes = 0;

    /**
     * <p>Default constructor.<p>
     * <p>Requires an {@link OutputStream} to observe.</p>
     *
     * @param outputStream the {@link OutputStream} to be tracked.
     */
    public ProgressOutputStream(OutputStream outputStream) {
        super(outputStream);
    }

    /**
     * Adds a {@link StreamListener} to the OutputStream.
     *
     * @param streamListener the StreamListener to be added.
     */
    public void addInputStreamListener(StreamListener streamListener) {
        this.streamListener = streamListener;
    }

    /**
     * Overrides default {@link FilterOutputStream#write(int)} to count the number of bytes written from the {@link OutputStream}.
     *
     * @throws IOException if the stream is interrupted.
     */
    @Override
    public void write(int b) throws IOException {
        totalBytes += b;
        streamListener.onBytesTouched(totalBytes);
        out.write(b);
    }

    /**
     * Overrides default {@link FilterOutputStream#write(byte[])} to count the number of bytes written from the {@link OutputStream}.
     *
     * @param b the buffer which length contains the bytes written.
     * @throws IOException if the stream is interrupted.
     */
    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    /**
     * Overrides default {@link FilterOutputStream#write(byte[], int, int)} to count the number of bytes written from the {@link OutputStream}.
     * It writes up to len bytes of data from this output stream into an array of bytes.
     * If len is not zero, the method blocks until some output is available; otherwise, no bytes are written and 0 is returned.
     *
     * @param b the buffer which length contains the number of bytes write.
     * @param off the start offset in the destination array b.
     * @param len the maximum number of bytes written.
     * @throws IOException if the stream is interrupted.
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        totalBytes += len;
        streamListener.onBytesTouched(totalBytes);
        out.write(b, off, len);
    }
}