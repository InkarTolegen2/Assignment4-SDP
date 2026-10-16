package claims.exception;

public class PayoutFailedException extends RuntimeException{
    public PayoutFailedException(String message) {
        super(message);
    }
}
