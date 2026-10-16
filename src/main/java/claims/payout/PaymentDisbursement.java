package claims.payout;

import claims.exception.PayoutFailedException;

public class PaymentDisbursement {
    private final BankGateway bank;
    private final PayoutRepository repository;

    public PaymentDisbursement(BankGateway bank, PayoutRepository repository) {
        this.bank = bank;
        this.repository = repository;
    }

    public void disburse(String claimId, long amount) {
        if (repository.findStatus(claimId).filter("PAID"::equals).isPresent()) {
            return;   // already paid, do not pay twice
        }
        if (!bank.transfer(claimId, amount)) {
            throw new PayoutFailedException("Bank declined payout for " + claimId);
        }
        repository.save(claimId, amount, "PAID");
    }
}