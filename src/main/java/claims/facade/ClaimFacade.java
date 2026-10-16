package claims.facade;

import claims.exception.PayoutFailedException;
import claims.exception.PolicyCheckFailedException;
import claims.intake.Claim;
import claims.intake.ClaimRepository;
import claims.intake.DocumentVerifier;
import claims.payout.PaymentDisbursement;
import claims.payout.PayoutCalculator;
import claims.policy.PolicyChecker;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ClaimFacade {
    private final DocumentVerifier verifier;
    private final ClaimRepository claims;
    private final PolicyChecker policyChecker;
    private final PayoutCalculator calculator;
    private final PaymentDisbursement disbursement;
    private final ChainRepository chains;
    private final Supplier<LocalDate> today;

    public ClaimFacade(DocumentVerifier verifier, ClaimRepository claims,
                       PolicyChecker policyChecker, PayoutCalculator calculator,
                       PaymentDisbursement disbursement, ChainRepository chains,
                       Supplier<LocalDate> today) {
        this.verifier = verifier;
        this.claims = claims;
        this.policyChecker = policyChecker;
        this.calculator = calculator;
        this.disbursement = disbursement;
        this.chains = chains;
        this.today = today;
    }

    public ChainStatus processClaim(String claimId, String policyId, long amount,
                                    List<String> documents) {
        submit(claimId, policyId, amount, documents);
        return proceed(claimId);
    }

    public void submit(String claimId, String policyId, long amount, List<String> documents) {
        if (!verifier.isValid(documents)) {
            throw new IllegalArgumentException("Invalid documents for claim " + claimId);
        }
        Claim claim = new Claim(claimId);
        claim.verify();
        claims.save(claim);
        chains.save(new ChainRecord(claimId, policyId, amount, ChainStatus.PENDING_B, null));
    }

    public ChainStatus proceed(String claimId) {
        ChainRecord record = chains.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown claim: " + claimId));
        if (record.getStatus() == ChainStatus.PENDING_B) record = stepB(record);
        if (record.getStatus() == ChainStatus.PENDING_C) record = stepC(record);
        return record.getStatus();
    }

    public List<String> resumeUnfinished() {
        List<String> resumed = new ArrayList<>();
        for (ChainRecord record : chains.findUnfinished()) {
            proceed(record.getClaimId());
            resumed.add(record.getClaimId());
        }
        return resumed;
    }

    private ChainRecord stepB(ChainRecord r) {
        try {
            policyChecker.check(r.getClaimId(), r.getPolicyId(), r.getAmount(), today.get());
        } catch (PolicyCheckFailedException e) {
            return compensate(r, e.getMessage());      
        }
        return save(r.with(ChainStatus.PENDING_C, null));
    }

    private ChainRecord stepC(ChainRecord r) {
        try {
            long payout = calculator.calculate(r.getAmount());
            disbursement.disburse(r.getClaimId(), payout);
        } catch (PayoutFailedException e) {
            policyChecker.revoke(r.getClaimId());    
            return compensate(r, e.getMessage());    
        }
        return save(r.with(ChainStatus.COMPLETED, null));
    }

    private ChainRecord compensate(ChainRecord r, String reason) {
        claims.findById(r.getClaimId()).ifPresent(claim -> {
            claim.cancel();
            claims.save(claim);
        });
        return save(r.with(ChainStatus.COMPENSATED, reason));
    }

    private ChainRecord save(ChainRecord r) {
        chains.save(r);
        return r;
    }
}