package bfst21.exceptions;

public class NoNavigationResultException extends Exception {
    public NoNavigationResultException() {
        super("Could not find a route e.g. due to vehicle restrictions, island, etc.");
    }
}
