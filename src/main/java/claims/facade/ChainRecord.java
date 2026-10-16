package claims.facade;

public class ChainRecord {
    private final String claimId;
    private final String policyId;
    private final long amount;
    private final ChainStatus status;
    private final String failureReason;

    public ChainRecord(String claimId, String policyId, long amount,
                       ChainStatus status, String failureReason) {
        this.claimId = claimId;
        this.policyId = policyId;
        this.amount = amount;
        this.status = status;
        this.failureReason = failureReason;
    }

    public String getClaimId() { return claimId; }
    public String getPolicyId() { return policyId; }
    public long getAmount() { return amount; }
    public ChainStatus getStatus() { return status; }
    public String getFailureReason() { return failureReason; }

    public ChainRecord with(ChainStatus newStatus, String reason) {
        return new ChainRecord(claimId, policyId, amount, newStatus, reason);
    }
}