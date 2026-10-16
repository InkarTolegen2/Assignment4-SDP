package claims.payout;

import claims.exception.RepositoryException;

import java.sql.*;
import java.util.Optional;

public class PayoutRepository {
    private final String url;

    public PayoutRepository(String dbPath) {
        this.url = "jdbc:sqlite:" + dbPath;
        try (Connection c = connect(); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS payouts ("
                    + "claim_id TEXT PRIMARY KEY, amount INTEGER NOT NULL, status TEXT NOT NULL)");
        } catch (SQLException e) {
            throw new RepositoryException("Cannot create payouts table", e);
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(url);
    }

    public void save(String claimId, long amount, String status) {
        String sql = "INSERT INTO payouts(claim_id, amount, status) VALUES(?,?,?) "
                + "ON CONFLICT(claim_id) DO UPDATE SET amount = excluded.amount, status = excluded.status";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, claimId);
            ps.setLong(2, amount);
            ps.setString(3, status);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Cannot save payout " + claimId, e);
        }
    }

    public Optional<String> findStatus(String claimId) {
        String sql = "SELECT status FROM payouts WHERE claim_id = ?";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, claimId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(rs.getString("status")) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Cannot load payout " + claimId, e);
        }
    }
}