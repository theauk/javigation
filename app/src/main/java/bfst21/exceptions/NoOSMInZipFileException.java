package bfst21.exceptions;

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
