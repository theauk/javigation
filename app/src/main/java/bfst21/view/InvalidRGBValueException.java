package bfst21.view;

public class InvalidRGBValueException extends Exception {
    private final int r;
    private final int g;
    private final int b;

    public InvalidRGBValueException(String key, int r, int g, int b) {
        super("Warning: RGB value out of range for key: '" + key + "'. Expects values from 0-255");
        this.r = r;
        this.g = g;
        this.b = b;
    }

    @Override
    public String getMessage() {
        String message = "";
        if (r < 0 || r > 255) message += "-> Red is: " + r + "\n";
        if (g < 0 || g > 255) message += "-> Green is: " + g + "\n";
        if (b < 0 || b > 255) message += "-> Blue is: " + b + "\n";

        return super.getMessage() + " -> \n" + message;
    }
}
