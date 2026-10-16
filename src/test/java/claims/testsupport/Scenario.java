package claims.testsupport;

import claims.facade.*;
import claims.intake.*;
import claims.payout.*;
import claims.policy.*;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class Scenario {
    public static final LocalDate TODAY = LocalDate.of(2026, 10, 16);

    private final String dbPath;
    private final AtomicBoolean bankAccepts = new AtomicBoolean(true);

    public Scenario(Path dir) {
        this.dbPath = dir.resolve("claims.db").toString();
        PolicyRepository policies = new PolicyRepository(dbPath);
        policies.save(new Policy("P-1", LocalDate.of(2027, 1, 1), 5_000_000));
        policies.save(new Policy("P-OLD", LocalDate.of(2025, 1, 1), 5_000_000));
    }

    public Scenario bankDeclines() {
        bankAccepts.set(false);
        return this;
    }

    public ClaimFacade newFacade() {
        return new ClaimFacade(
                new DocumentVerifier(),
                new ClaimRepository(dbPath),
                new PolicyChecker(new PolicyRepository(dbPath), new FraudDetector(3_000_000)),
                new PayoutCalculator(100_000),
                new PaymentDisbursement((id, amount) -> bankAccepts.get(),
                        new PayoutRepository(dbPath)),
                new ChainRepository(dbPath),
                () -> TODAY);
    }

    public ChainRepository chains() {
        return new ChainRepository(dbPath);
    }

    public void assertChainIs(String claimId, ChainStatus expected) {
        assertEquals(Optional.of(expected),
                new ChainRepository(dbPath).findById(claimId).map(ChainRecord::getStatus));
    }

    public void assertClaimIs(String claimId, ClaimStatus expected) {
        assertEquals(Optional.of(expected),
                new ClaimRepository(dbPath).findById(claimId).map(Claim::getStatus));
    }

    public void assertPayoutIs(String claimId, String expected) {
        assertEquals(Optional.of(expected), new PayoutRepository(dbPath).findStatus(claimId));
    }

    public void assertNoPayout(String claimId) {
        assertTrue(new PayoutRepository(dbPath).findStatus(claimId).isEmpty());
    }

    public void assertPolicyChecked(String claimId) {
        assertTrue(new PolicyRepository(dbPath).isChecked(claimId));
    }

    public void assertPolicyNotChecked(String claimId) {
        assertFalse(new PolicyRepository(dbPath).isChecked(claimId));
    }
}