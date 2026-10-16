package claims.exception;

public class PolicyCheckFailedException extends RuntimeException {
    public PolicyCheckFailedException(String message) {
        super(message);
    }
    
}
