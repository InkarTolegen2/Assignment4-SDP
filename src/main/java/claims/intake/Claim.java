package claims.intake;

import claims.exception.InvalidTransitionException;

public class Claim {
    private final String id;
    private ClaimStatus status;

    public Claim(String id) {
        this.id = id;
        this.status = ClaimStatus.SUBMITTED;
    }

    private Claim(String id, ClaimStatus status) {
        this.id = id;
        this.status = status;
    }

    public static Claim restore(String id, ClaimStatus status) {
        return new Claim(id, status);
    }

    public String getId() { return id; }
    public ClaimStatus getStatus() { return status; }

    public void verify() { moveTo(ClaimStatus.VERIFIED); }
    public void cancel() { moveTo(ClaimStatus.CANCELLED); }

    private void moveTo(ClaimStatus next) {
        if (!status.canMoveTo(next)) {
            throw new InvalidTransitionException(status.name(), next.name());
        }
        status = next;
    }
}