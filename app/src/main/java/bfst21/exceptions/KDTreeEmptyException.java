package bfst21.Exceptions;

public class KDTreeEmptyException extends Exception {
    public KDTreeEmptyException(String message) {
        super(message);
    }
    public KDTreeEmptyException(){
        super("KDTree is empty!");
    }
}
