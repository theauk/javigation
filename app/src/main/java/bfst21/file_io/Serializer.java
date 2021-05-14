package bfst21.file_io;

import javafx.concurrent.Task;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

public class Serializer extends Task<Void> {
    private final Object object;
    private final File file;

    public Serializer(Object object, File file) {
        this.object = object;
        this.file = file;
    }

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

            size = Math.round(size * Math.pow(10, 1)) / Math.pow(10, 1);

            updateMessage("Dumping MapData (" + size + " " + unit + ")");
        });
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(progressOutputStream));
        objectOutputStream.writeObject(object);
        objectOutputStream.close();
        updateMessage("Done.");

        return null;
    }
}
