package claims.payout;

import claims.exception.PayoutFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PayoutTest {
    @TempDir Path tempDir;
    private String dbPath;

    @BeforeEach
    void setUp() {
        dbPath = tempDir.resolve("payout.db").toString();
    }

    @Test
    void calculatorSubtractsDeductible() {
        assertEquals(900_000, new PayoutCalculator(100_000).calculate(1_000_000));
    }

    @Test
    void calculatorNeverReturnsNegative() {
        assertEquals(0, new PayoutCalculator(100_000).calculate(50_000));
    }

    @Test
    void successfulDisbursementIsStoredAsPaid() {
        PaymentDisbursement d = new PaymentDisbursement(
                (claimId, amount) -> true, new PayoutRepository(dbPath));
        d.disburse("C-1", 900_000);
        assertEquals("PAID", new PayoutRepository(dbPath).findStatus("C-1").get());
    }

    @Test
    void bankDeclineThrowsAndStoresNothing() {
        PaymentDisbursement d = new PaymentDisbursement(
                (claimId, amount) -> false, new PayoutRepository(dbPath));
        assertThrows(PayoutFailedException.class, () -> d.disburse("C-1", 900_000));
        assertTrue(new PayoutRepository(dbPath).findStatus("C-1").isEmpty());
    }
}