package bfst21.file_io;

import java.io.IOException;

public interface StreamListener {
    void onBytesTouched(long totalBytes) throws IOException;
}