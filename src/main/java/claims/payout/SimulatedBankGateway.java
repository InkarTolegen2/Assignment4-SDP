package claims.payout;

public class SimulatedBankGateway implements BankGateway {
    private boolean accepting = true;

    public void setAccepting(boolean accepting) { this.accepting = accepting; }
    public boolean isAccepting() { return accepting; }

    @Override
    public boolean transfer(String claimId, long amount) {
        return accepting;
    }
}