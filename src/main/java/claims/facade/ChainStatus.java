package claims.facade;

public enum ChainStatus {
    PENDING_B, PENDING_C, COMPLETED, COMPENSATED;

    public boolean isFinished() {
        return this == COMPLETED || this == COMPENSATED;
    }
}