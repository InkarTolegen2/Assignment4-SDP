package claims.payout;

public interface BankGateway {
    boolean transfer(String claimId, long amount);
}