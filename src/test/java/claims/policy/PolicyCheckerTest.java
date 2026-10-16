package claims.policy;

import claims.exception.PolicyCheckFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PolicyCheckerTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 16);

    @TempDir Path tempDir;
    private String dbPath;
    private PolicyChecker checker;

    @BeforeEach
    void setUp() {
        dbPath = tempDir.resolve("policy.db").toString();
        PolicyRepository repo = new PolicyRepository(dbPath);
        repo.save(new Policy("P-1", LocalDate.of(2027, 1, 1), 5_000_000));
        repo.save(new Policy("P-OLD", LocalDate.of(2025, 1, 1), 5_000_000));
        checker = new PolicyChecker(repo, new FraudDetector(3_000_000));
    }

    @Test
    void validClaimPassesAndIsStored() {
        checker.check("C-1", "P-1", 1_000_000, TODAY);
        assertTrue(new PolicyRepository(dbPath).isChecked("C-1")); // читаем из файла
    }

    @Test
    void expiredPolicyFails() {
        assertThrows(PolicyCheckFailedException.class,
                () -> checker.check("C-1", "P-OLD", 1_000, TODAY));
    }

    @Test
    void unknownPolicyFails() {
        assertThrows(PolicyCheckFailedException.class,
                () -> checker.check("C-1", "NOPE", 1_000, TODAY));
    }

    @Test
    void amountAboveCoverageFails() {
        assertThrows(PolicyCheckFailedException.class,
                () -> checker.check("C-1", "P-1", 9_000_000, TODAY));
    }

    @Test
    void suspiciousAmountFails() {
        assertThrows(PolicyCheckFailedException.class,
                () -> checker.check("C-1", "P-1", 4_000_000, TODAY));
    }

    @Test
    void revokeClearsStoredCheck() {
        checker.check("C-1", "P-1", 1_000_000, TODAY);
        checker.revoke("C-1");
        assertFalse(new PolicyRepository(dbPath).isChecked("C-1"));
    }
}