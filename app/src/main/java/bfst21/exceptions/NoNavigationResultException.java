package bfst21.Exceptions;

public class NoNavigationResultException extends Exception {
    public NoNavigationResultException() {
        super("Could not find a route e.g. due to vehicle restrictions, island, etc.");
    }
}
