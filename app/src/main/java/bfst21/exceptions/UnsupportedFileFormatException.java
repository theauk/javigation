package bfst21.Exceptions;

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

