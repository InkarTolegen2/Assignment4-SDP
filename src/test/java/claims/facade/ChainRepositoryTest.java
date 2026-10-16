package claims.facade;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChainRepositoryTest {
    @TempDir Path tempDir;
    private String dbPath;

    @BeforeEach
    void setUp() {
        dbPath = tempDir.resolve("chains.db").toString();
    }

    @Test
    void savedChainSurvivesReopen() {
        new ChainRepository(dbPath).save(
                new ChainRecord("C-1", "P-1", 1000, ChainStatus.PENDING_B, null));

        ChainRecord loaded = new ChainRepository(dbPath).findById("C-1").get();

        assertEquals(ChainStatus.PENDING_B, loaded.getStatus());
        assertEquals("P-1", loaded.getPolicyId());
        assertEquals(1000, loaded.getAmount());
    }

    @Test
    void findUnfinishedSkipsFinishedChains() {
        ChainRepository repo = new ChainRepository(dbPath);
        repo.save(new ChainRecord("C-1", "P-1", 1000, ChainStatus.PENDING_C, null));
        repo.save(new ChainRecord("C-2", "P-1", 1000, ChainStatus.COMPLETED, null));

        List<ChainRecord> unfinished = repo.findUnfinished();

        assertEquals(1, unfinished.size());
        assertEquals("C-1", unfinished.get(0).getClaimId());
    }

    @Test
    void unknownChainIsEmpty() {
        assertTrue(new ChainRepository(dbPath).findById("NOPE").isEmpty());
    }
}