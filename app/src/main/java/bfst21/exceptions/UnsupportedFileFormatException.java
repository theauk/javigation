package bfst21.exceptions;

/**
 * Thrown if the user tries to load an unsupported file format.
 */
public class UnsupportedFileFormatException extends Exception {
    private final String format;

    public UnsupportedFileFormatException(String format) {
        super("Unsupported file format ");
        this.format = format.substring(format.lastIndexOf("."));
    }

    @Override
    public String getMessage() {
        return super.getMessage() + "\"" + format + "\"";
    }
}

