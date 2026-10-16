package claims.facade;

import claims.exception.RepositoryException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ChainRepository {
    private final String url;

    public ChainRepository(String dbPath) {
        this.url = "jdbc:sqlite:" + dbPath;
        try (Connection c = connect(); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS chains ("
                    + "claim_id TEXT PRIMARY KEY, policy_id TEXT NOT NULL, "
                    + "amount INTEGER NOT NULL, status TEXT NOT NULL, reason TEXT)");
        } catch (SQLException e) {
            throw new RepositoryException("Cannot create chains table", e);
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(url);
    }

    public void save(ChainRecord r) {
        String sql = "INSERT INTO chains(claim_id, policy_id, amount, status, reason) "
                + "VALUES(?,?,?,?,?) ON CONFLICT(claim_id) DO UPDATE SET "
                + "status = excluded.status, reason = excluded.reason";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, r.getClaimId());
            ps.setString(2, r.getPolicyId());
            ps.setLong(3, r.getAmount());
            ps.setString(4, r.getStatus().name());
            ps.setString(5, r.getFailureReason());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Cannot save chain " + r.getClaimId(), e);
        }
    }

    public Optional<ChainRecord> findById(String claimId) {
        String sql = "SELECT * FROM chains WHERE claim_id = ?";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, claimId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Cannot load chain " + claimId, e);
        }
    }

    public List<ChainRecord> findUnfinished() {
        String sql = "SELECT * FROM chains WHERE status IN ('PENDING_B','PENDING_C') "
                + "ORDER BY claim_id";
        List<ChainRecord> result = new ArrayList<>();
        try (Connection c = connect(); Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) result.add(map(rs));
            return result;
        } catch (SQLException e) {
            throw new RepositoryException("Cannot list unfinished chains", e);
        }
    }

    private ChainRecord map(ResultSet rs) throws SQLException {
        return new ChainRecord(rs.getString("claim_id"), rs.getString("policy_id"),
                rs.getLong("amount"), ChainStatus.valueOf(rs.getString("status")),
                rs.getString("reason"));
    }
}