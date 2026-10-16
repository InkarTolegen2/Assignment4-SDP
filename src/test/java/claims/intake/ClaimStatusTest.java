package claims.intake;

import org.junit.jupiter.api.Test;

import static claims.intake.ClaimStatus.*;
import static org.junit.jupiter.api.Assertions.*;

class ClaimStatusTest {

    @Test
    void submittedMovesToVerifiedOrCancelled() {
        assertTrue(SUBMITTED.canMoveTo(VERIFIED));
        assertTrue(SUBMITTED.canMoveTo(CANCELLED));
        assertFalse(SUBMITTED.canMoveTo(SUBMITTED));
    }

    @Test
    void verifiedMovesOnlyToCancelled() {
        assertTrue(VERIFIED.canMoveTo(CANCELLED));
        assertFalse(VERIFIED.canMoveTo(SUBMITTED));
        assertFalse(VERIFIED.canMoveTo(VERIFIED));
    }

    @Test
    void cancelledIsFinal() {
        assertFalse(CANCELLED.canMoveTo(SUBMITTED));
        assertFalse(CANCELLED.canMoveTo(VERIFIED));
        assertFalse(CANCELLED.canMoveTo(CANCELLED));
    }
}