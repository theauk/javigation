package bfst21.exceptions;

/**
 * Thrown if the user tries to open a zip file which does not contain an OSM-file-
 */
public class NoOSMInZipFileException extends Exception {
    private final String fileName;

    public NoOSMInZipFileException(String fileName) {
        super(" does not contain a zipped OSM file.");
        this.fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
    }

    @Override
    public String getMessage() {
        return "\"" + fileName + "\"" + super.getMessage();
    }
}
