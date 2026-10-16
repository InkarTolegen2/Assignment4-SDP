package claims.facade;

import java.util.List;
import java.util.Optional;

public class ClaimAdminFacade {
    private final ChainRepository chains;

    public ClaimAdminFacade(ChainRepository chains) {
        this.chains = chains;
    }

    public Optional<ChainRecord> find(String claimId) {
        return chains.findById(claimId);
    }

    public List<ChainRecord> unfinished() {
        return chains.findUnfinished();
    }

    public String describe(String claimId) {
        return chains.findById(claimId)
                .map(r -> r.getClaimId() + ": " + r.getStatus()
                        + (r.getFailureReason() == null ? "" : " (" + r.getFailureReason() + ")"))
                .orElse(claimId + ": not found");
    }
}