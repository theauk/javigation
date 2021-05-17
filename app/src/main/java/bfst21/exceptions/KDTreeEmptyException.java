package bfst21.exceptions;

/**
 * Thrown if the KD-tree is empty.
 */
public class KDTreeEmptyException extends Exception {
    public KDTreeEmptyException(){
        super("KDTree is empty!");
    }
}
