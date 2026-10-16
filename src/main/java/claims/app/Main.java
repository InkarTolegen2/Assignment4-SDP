package claims.app;

import claims.facade.*;
import claims.intake.ClaimRepository;
import claims.intake.DocumentVerifier;
import claims.payout.*;
import claims.policy.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {
    private static final String DB_PATH = "claims.db";

    public static void main(String[] args) {
        PolicyRepository policies = new PolicyRepository(DB_PATH);
        policies.save(new Policy("P-1", LocalDate.of(2027, 12, 31), 5_000_000));
        policies.save(new Policy("P-OLD", LocalDate.of(2025, 1, 1), 5_000_000));

        SimulatedBankGateway bank = new SimulatedBankGateway();
        ChainRepository chains = new ChainRepository(DB_PATH);

        ClaimFacade facade = new ClaimFacade(
                new DocumentVerifier(),
                new ClaimRepository(DB_PATH),
                new PolicyChecker(policies, new FraudDetector(3_000_000)),
                new PayoutCalculator(100_000),
                new PaymentDisbursement(bank, new PayoutRepository(DB_PATH)),
                chains,
                LocalDate::now);
        ClaimAdminFacade admin = new ClaimAdminFacade(chains);

        // On start: continue everything that was left unfinished in the database
        List<String> resumed = facade.resumeUnfinished();
        if (!resumed.isEmpty()) {
            System.out.println("Resumed from database: " + resumed);
        }

        run(facade, admin, bank);
    }

    private static void run(ClaimFacade facade, ClaimAdminFacade admin,
                            SimulatedBankGateway bank) {
        Scanner in = new Scanner(System.in);
        while (true) {
            printMenu(bank);
            String choice = in.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> {
                        ChainStatus result = facade.processClaim(
                                ask(in, "Claim id"), ask(in, "Policy id (P-1 or P-OLD)"),
                                Long.parseLong(ask(in, "Amount")), askDocuments(in));
                        System.out.println("Result: " + result);
                    }
                    case "2" -> {
                        String id = ask(in, "Claim id");
                        facade.submit(id, ask(in, "Policy id (P-1 or P-OLD)"),
                                Long.parseLong(ask(in, "Amount")), askDocuments(in));
                        System.out.println("Step A done, chain is PENDING_B. "
                                + "Exit (0) and start again to resume.");
                    }
                    case "3" -> {
                        bank.setAccepting(!bank.isAccepting());
                        System.out.println("Bank accepting: " + bank.isAccepting());
                    }
                    case "4" -> System.out.println(admin.describe(ask(in, "Claim id")));
                    case "5" -> admin.unfinished().forEach(r ->
                            System.out.println(r.getClaimId() + ": " + r.getStatus()));
                    case "6" -> System.out.println("Resumed: " + facade.resumeUnfinished());
                    case "0" -> { return; }
                    default -> System.out.println("Unknown option");
                }
            } catch (RuntimeException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void printMenu(SimulatedBankGateway bank) {
        System.out.println();
        System.out.println("1  Process claim (A -> B -> C)");
        System.out.println("2  Submit only (step A, then stop)");
        System.out.println("3  Toggle bank (now accepting: " + bank.isAccepting() + ")");
        System.out.println("4  Show claim status");
        System.out.println("5  List unfinished chains");
        System.out.println("6  Resume unfinished chains");
        System.out.println("0  Exit");
        System.out.print("> ");
    }

    private static String ask(Scanner in, String label) {
        System.out.print(label + ": ");
        return in.nextLine().trim();
    }

    private static List<String> askDocuments(Scanner in) {
        String line = ask(in, "Documents, comma separated (empty = none)");
        if (line.isEmpty()) return List.of();
        return Arrays.stream(line.split(",")).map(String::trim).collect(Collectors.toList());
    }
}