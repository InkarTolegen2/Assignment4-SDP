package claims.facade;

import claims.testsupport.Scenario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClaimAdminFacadeTest {
    private static final List<String> DOCS = List.of("passport.pdf");

    @TempDir Path tempDir;
    private Scenario scenario;
    private ClaimAdminFacade admin;

    @BeforeEach
    void setUp() {
        scenario = new Scenario(tempDir);
        admin = new ClaimAdminFacade(scenario.chains());
    }

    @Test
    void describesCompensatedClaimWithReason() {
        scenario.newFacade().processClaim("C-1", "P-OLD", 1_000, DOCS);

        String text = admin.describe("C-1");

        assertTrue(text.contains("COMPENSATED"));
        assertTrue(text.contains("expired"));
    }

    @Test
    void unknownClaimIsReported() {
        assertEquals("C-9: not found", admin.describe("C-9"));
    }

    @Test
    void listsUnfinishedChains() {
        scenario.newFacade().submit("C-1", "P-1", 1_000, DOCS);

        assertEquals(1, admin.unfinished().size());
    }
}