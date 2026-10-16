package claims;

import claims.exception.RepositoryException;
import claims.facade.ChainRepository;
import claims.intake.ClaimRepository;
import claims.payout.PayoutRepository;
import claims.policy.PolicyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;

class RepositoryFailureTest {
    @TempDir Path tempDir;

    private String unreachableDb() {
        return tempDir.resolve("missing/dir/x.db").toString();
    }

    @Test
    void claimRepositoryReportsStorageErrors() {
        assertThrows(RepositoryException.class, () -> new ClaimRepository(unreachableDb()));
    }

    @Test
    void policyRepositoryReportsStorageErrors() {
        assertThrows(RepositoryException.class, () -> new PolicyRepository(unreachableDb()));
    }

    @Test
    void payoutRepositoryReportsStorageErrors() {
        assertThrows(RepositoryException.class, () -> new PayoutRepository(unreachableDb()));
    }

    @Test
    void chainRepositoryReportsStorageErrors() {
        assertThrows(RepositoryException.class, () -> new ChainRepository(unreachableDb()));
    }
}