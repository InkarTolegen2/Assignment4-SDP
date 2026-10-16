package claims.policy;

import claims.exception.PolicyCheckFailedException;

import java.time.LocalDate;

public class PolicyChecker {
    private final PolicyRepository repository;
    private final FraudDetector fraudDetector;

    public PolicyChecker(PolicyRepository repository, FraudDetector fraudDetector) {
        this.repository = repository;
        this.fraudDetector = fraudDetector;
    }

    public void check(String claimId, String policyId, long amount, LocalDate today) {
        Policy policy = repository.findById(policyId)
                .orElseThrow(() -> new PolicyCheckFailedException("Policy not found: " + policyId));
        if (!policy.isActiveOn(today)) {
            throw new PolicyCheckFailedException("Policy expired: " + policyId);
        }
        if (amount > policy.getCoverageLimit()) {
            throw new PolicyCheckFailedException("Amount exceeds coverage limit");
        }
        if (fraudDetector.isSuspicious(amount)) {
            throw new PolicyCheckFailedException("Fraud suspected");
        }
        repository.markChecked(claimId, true);
    }

    public void revoke(String claimId) {
        repository.markChecked(claimId, false);
    }

    public boolean isChecked(String claimId) {
        return repository.isChecked(claimId);
    }
}