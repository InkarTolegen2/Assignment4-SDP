package claims.intake;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DocumentVerifierTest {
    private final DocumentVerifier verifier = new DocumentVerifier();

    @Test
    void claimWithDocumentsIsValid() {
        assertTrue(verifier.isValid(List.of("passport.pdf", "report.pdf")));
    }

    @Test
    void emptyListIsInvalid() {
        assertFalse(verifier.isValid(List.of()));
    }

    @Test
    void nullListIsInvalid() {
        assertFalse(verifier.isValid(null));
    }

    @Test
    void blankDocumentNameIsInvalid() {
        assertFalse(verifier.isValid(Arrays.asList("passport.pdf", " ")));
    }
}