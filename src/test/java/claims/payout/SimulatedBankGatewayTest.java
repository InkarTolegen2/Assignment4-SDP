package claims.payout;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SimulatedBankGatewayTest {
    @Test
    void acceptsByDefault() {
        assertTrue(new SimulatedBankGateway().transfer("C-1", 100));
    }

    @Test
    void declinesWhenSwitchedOff() {
        SimulatedBankGateway bank = new SimulatedBankGateway();
        bank.setAccepting(false);
        assertFalse(bank.transfer("C-1", 100));
    }
}