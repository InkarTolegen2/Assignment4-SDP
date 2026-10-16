package claims.payout;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class PaymentDisbursementIdempotencyTest {
    @TempDir Path tempDir;

    @Test
    void secondDisburseDoesNotCallBankAgain() {
        AtomicInteger bankCalls = new AtomicInteger();
        PaymentDisbursement disbursement = new PaymentDisbursement(
                (claimId, amount) -> { bankCalls.incrementAndGet(); return true; },
                new PayoutRepository(tempDir.resolve("p.db").toString()));

        disbursement.disburse("C-1", 900_000);
        disbursement.disburse("C-1", 900_000);

        assertEquals(1, bankCalls.get());
    }
}