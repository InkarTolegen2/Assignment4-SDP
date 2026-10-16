package claims.payout;

public class PayoutCalculator {
    private final long deductible;

    public PayoutCalculator(long deductible) { this.deductible = deductible; }

    public long calculate(long claimedAmount) {
        return Math.max(0, claimedAmount - deductible);
    }
}