package bfst21.Exceptions;

public class NoNavigationResultException extends Exception {
    public NoNavigationResultException() {
        super("It was not possible to find a route between the two points.");
    }
}
