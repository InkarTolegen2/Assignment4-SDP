package claims.policy;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PolicyAndFraudTest {
    private final Policy policy = new Policy("P-1", LocalDate.of(2027, 1, 1), 5_000_000);

    @Test
    void policyIsActiveOnExpiryDay() {
        assertTrue(policy.isActiveOn(LocalDate.of(2027, 1, 1)));
    }

    @Test
    void policyIsExpiredDayAfter() {
        assertFalse(policy.isActiveOn(LocalDate.of(2027, 1, 2)));
    }

    @Test
    void policyExposesItsData() {
        assertEquals("P-1", policy.getId());
        assertEquals(LocalDate.of(2027, 1, 1), policy.getExpiresOn());
        assertEquals(5_000_000, policy.getCoverageLimit());
    }

    @Test
    void amountEqualToThresholdIsNotSuspicious() {
        assertFalse(new FraudDetector(3_000_000).isSuspicious(3_000_000));
    }

    @Test
    void amountAboveThresholdIsSuspicious() {
        assertTrue(new FraudDetector(3_000_000).isSuspicious(3_000_001));
    }
}