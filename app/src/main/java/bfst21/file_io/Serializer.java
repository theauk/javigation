package bfst21.file_io;

import bfst21.utils.MapMath;
import javafx.concurrent.Task;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

/**
 * A {@link Task} for Serializing an object into a {@link File} on another Thread.
 * It uses an {@link ProgressOutputStream} to give real time updates on how many bytes have been written to the file.
 */
public class Serializer extends Task<Void> {
    private final Object object;
    private final File file;

    /**
     * <p>Default constructor.</p>
     * <p>Needs an object to serialize and a file to write data to.</p>
     *
     * @param object the object to be serialized.
     * @param file the file to write the object to.
     */
    public Serializer(Object object, File file) {
        this.object = object;
        this.file = file;
    }

    /**
     * <p>Overrides default {@link Task#call()} method.</p>
     * Serializes the object into the file, while updating the progress of the process.
     *
     * @return null.
     * @throws Exception if any kind of exception is thrown.
     */
    @Override
    protected Void call() throws Exception {
        updateMessage("Initializing dump...");
        ProgressOutputStream progressOutputStream = new ProgressOutputStream(new FileOutputStream(file));
        progressOutputStream.addInputStreamListener(totalBytes -> {
            double size = totalBytes;
            String unit = "B";

            if (size > 1000 && size < 1000000) {
                size /= 1000;
                unit = "kB";
            } else if (size > 1000000 && size < 1000000000) {
                size /= 1000000;
                unit = "MB";
            } else if (size > 1000000000) {
                size /= 1000000000;
                unit = "GB";
            }

            size = MapMath.round(size, 1);

            updateMessage("Dumping MapData (" + size + " " + unit + ")");
        });
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(progressOutputStream));
        objectOutputStream.writeObject(object);
        objectOutputStream.close();
        updateMessage("Done.");

        return null;
    }
}
