package claims.policy;

public class FraudDetector {
    private final long threshold;

    public FraudDetector(long threshold) { this.threshold = threshold; }

    public boolean isSuspicious(long amount) {
        return amount > threshold;
    }
}
