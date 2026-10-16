package claims.intake;

import claims.exception.InvalidTransitionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClaimTest {

    @Test
    void newClaimIsSubmitted() {
        assertEquals(ClaimStatus.SUBMITTED, new Claim("C-1").getStatus());
    }

    @Test
    void submittedClaimCanBeVerified() {
        Claim claim = new Claim("C-1");
        claim.verify();
        assertEquals(ClaimStatus.VERIFIED, claim.getStatus());
    }

    @Test
    void verifiedClaimCanBeCancelled() {
        Claim claim = new Claim("C-1");
        claim.verify();
        claim.cancel();
        assertEquals(ClaimStatus.CANCELLED, claim.getStatus());
    }

    @Test
    void cancelledClaimCannotBeVerified() {
        Claim claim = new Claim("C-1");
        claim.cancel();
        assertThrows(InvalidTransitionException.class, claim::verify);
    }

    @Test
    void cancelledClaimCannotBeCancelledAgain() {
        Claim claim = new Claim("C-1");
        claim.cancel();
        assertThrows(InvalidTransitionException.class, claim::cancel);
    }

    @Test
    void restoreKeepsGivenStatus() {
        Claim claim = Claim.restore("C-1", ClaimStatus.VERIFIED);
        assertEquals("C-1", claim.getId());
        assertEquals(ClaimStatus.VERIFIED, claim.getStatus());
    }
}