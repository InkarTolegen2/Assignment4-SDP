package claims.facade;

import claims.testsupport.Scenario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static claims.intake.ClaimStatus.CANCELLED;
import static claims.intake.ClaimStatus.VERIFIED;
import static org.junit.jupiter.api.Assertions.*;

class ClaimFacadeTest {
    private static final List<String> DOCS = List.of("passport.pdf", "report.pdf");

    @TempDir Path tempDir;
    private Scenario scenario;

    @BeforeEach
    void setUp() {
        scenario = new Scenario(tempDir);
    }

    @Test
    void happyPathCompletesWholeChain() {
        ChainStatus result = scenario.newFacade().processClaim("C-1", "P-1", 1_000_000, DOCS);

        assertEquals(ChainStatus.COMPLETED, result);
        scenario.assertClaimIs("C-1", VERIFIED);
        scenario.assertPolicyChecked("C-1");
        scenario.assertPayoutIs("C-1", "PAID");
        scenario.assertChainIs("C-1", ChainStatus.COMPLETED);
    }

    @Test
    void stepBFailureCompensatesOnlyA() {
        ChainStatus result = scenario.newFacade().processClaim("C-1", "P-OLD", 1_000, DOCS);

        assertEquals(ChainStatus.COMPENSATED, result);
        scenario.assertClaimIs("C-1", CANCELLED);
        scenario.assertPolicyNotChecked("C-1");
        scenario.assertNoPayout("C-1");
        scenario.assertChainIs("C-1", ChainStatus.COMPENSATED);
    }

    @Test
    void fraudSuspicionAlsoCompensatesA() {
        ChainStatus result = scenario.newFacade().processClaim("C-1", "P-1", 4_000_000, DOCS);

        assertEquals(ChainStatus.COMPENSATED, result);
        scenario.assertClaimIs("C-1", CANCELLED);
    }

    @Test
    void stepCFailureCompensatesBThenA() {
        scenario.bankDeclines();

        ChainStatus result = scenario.newFacade().processClaim("C-1", "P-1", 1_000_000, DOCS);

        assertEquals(ChainStatus.COMPENSATED, result);
        scenario.assertPolicyNotChecked("C-1");
        scenario.assertClaimIs("C-1", CANCELLED);
        scenario.assertNoPayout("C-1");
        scenario.assertChainIs("C-1", ChainStatus.COMPENSATED);
    }

    @Test
    void failureReasonIsStored() {
        scenario.newFacade().processClaim("C-1", "P-OLD", 1_000, DOCS);

        String reason = scenario.chains().findById("C-1").get().getFailureReason();

        assertTrue(reason.contains("expired"));
    }

    @Test
    void invalidDocumentsAreRejectedBeforeAnythingIsStored() {
        assertThrows(IllegalArgumentException.class,
                () -> scenario.newFacade().processClaim("C-1", "P-1", 1_000, List.of()));

        assertTrue(scenario.chains().findById("C-1").isEmpty());
    }

    @Test
    void restartResumesUnfinishedChainFromStore() {
        scenario.newFacade().submit("C-1", "P-1", 1_000_000, DOCS);
        scenario.assertChainIs("C-1", ChainStatus.PENDING_B);

        ClaimFacade restarted = scenario.newFacade();
        List<String> resumed = restarted.resumeUnfinished();

        assertEquals(List.of("C-1"), resumed);
        scenario.assertChainIs("C-1", ChainStatus.COMPLETED);
        scenario.assertPayoutIs("C-1", "PAID");
    }

    @Test
    void restartAfterBankDeclineCompensates() {
        scenario.newFacade().submit("C-1", "P-1", 1_000_000, DOCS);
        scenario.bankDeclines();

        scenario.newFacade().resumeUnfinished();

        scenario.assertChainIs("C-1", ChainStatus.COMPENSATED);
        scenario.assertClaimIs("C-1", CANCELLED);
    }

    @Test
    void proceedOnFinishedChainChangesNothing() {
        ClaimFacade facade = scenario.newFacade();
        facade.processClaim("C-1", "P-1", 1_000_000, DOCS);

        assertEquals(ChainStatus.COMPLETED, facade.proceed("C-1"));
    }

    @Test
    void proceedOnUnknownClaimFails() {
        assertThrows(IllegalArgumentException.class,
                () -> scenario.newFacade().proceed("NOPE"));
    }

    @Test
    void resumeWithNothingUnfinishedReturnsEmptyList() {
        assertTrue(scenario.newFacade().resumeUnfinished().isEmpty());
    }
}