package claims.policy;

import java.time.LocalDate;

public class Policy {
    private final String id;
    private final LocalDate expiresOn;
    private final long coverageLimit;

    public Policy(String id, LocalDate expiresOn, long coverageLimit) {
        this.id = id;
        this.expiresOn = expiresOn;
        this.coverageLimit = coverageLimit;
    }

    public String getId() { return id; }
    public LocalDate getExpiresOn() { return expiresOn; }
    public long getCoverageLimit() { return coverageLimit; }

    public boolean isActiveOn(LocalDate date) {
        return !date.isAfter(expiresOn);
    }
}