package claims.exception;

public class InvalidTransitionException extends RuntimeException {
    public InvalidTransitionException(String from, String to) {
        super("Cannot move from " + from + " to " + to);
    }
    
}
