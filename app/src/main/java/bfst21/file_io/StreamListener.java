package bfst21.file_io;

/**
 * A functional Interface with a method for tracking bytes read/written.
 */
public interface StreamListener {
    void onBytesTouched(long totalBytes);
}