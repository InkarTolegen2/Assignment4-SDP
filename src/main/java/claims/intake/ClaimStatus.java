package claims.intake;

public enum ClaimStatus {
    SUBMITTED, VERIFIED, CANCELLED;

    public boolean canMoveTo(ClaimStatus next) {
        switch (this) {
            case SUBMITTED: return next == VERIFIED || next == CANCELLED;
            case VERIFIED:  return next == CANCELLED;
            default:        return false; // CANCELLED is final
        }
    }
}