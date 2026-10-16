package claims.intake;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ClaimRepositoryTest {

    @TempDir
    Path tempDir;
    private String dbPath;

    @BeforeEach
    void setUp() {
        dbPath = tempDir.resolve("claims.db").toString();
    }

    @Test
    void savedClaimIsReadFromStoreAfterReopen() {
        ClaimRepository first = new ClaimRepository(dbPath);
        Claim claim = new Claim("C-1");
        claim.verify();
        first.save(claim);

        ClaimRepository second = new ClaimRepository(dbPath); // новый объект, данные только из файла
        Optional<Claim> loaded = second.findById("C-1");

        assertTrue(loaded.isPresent());
        assertEquals(ClaimStatus.VERIFIED, loaded.get().getStatus());
    }

    @Test
    void unknownIdReturnsEmpty() {
        ClaimRepository repo = new ClaimRepository(dbPath);
        assertTrue(repo.findById("NOPE").isEmpty());
    }

    @Test
    void savingTwiceUpdatesStatus() {
        ClaimRepository repo = new ClaimRepository(dbPath);
        Claim claim = new Claim("C-1");
        repo.save(claim);
        claim.cancel();
        repo.save(claim);

        assertEquals(ClaimStatus.CANCELLED, repo.findById("C-1").get().getStatus());
    }
}