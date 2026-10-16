package claims.policy;

import claims.exception.RepositoryException;

import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;

public class PolicyRepository {
    private final String url;

    public PolicyRepository(String dbPath) {
        this.url = "jdbc:sqlite:" + dbPath;
        try (Connection c = connect(); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS policies ("
                    + "id TEXT PRIMARY KEY, expires_on TEXT NOT NULL, coverage_limit INTEGER NOT NULL)");
            s.execute("CREATE TABLE IF NOT EXISTS policy_checks ("
                    + "claim_id TEXT PRIMARY KEY, passed INTEGER NOT NULL)");
        } catch (SQLException e) {
            throw new RepositoryException("Cannot create policy tables", e);
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(url);
    }

    public void save(Policy p) {
        String sql = "INSERT INTO policies(id, expires_on, coverage_limit) VALUES(?,?,?) "
                + "ON CONFLICT(id) DO UPDATE SET expires_on = excluded.expires_on, "
                + "coverage_limit = excluded.coverage_limit";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getId());
            ps.setString(2, p.getExpiresOn().toString());
            ps.setLong(3, p.getCoverageLimit());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Cannot save policy " + p.getId(), e);
        }
    }

    public Optional<Policy> findById(String id) {
        String sql = "SELECT expires_on, coverage_limit FROM policies WHERE id = ?";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new Policy(id,
                        LocalDate.parse(rs.getString("expires_on")),
                        rs.getLong("coverage_limit")));
            }
        } catch (SQLException e) {
            throw new RepositoryException("Cannot load policy " + id, e);
        }
    }

    public void markChecked(String claimId, boolean passed) {
        String sql = "INSERT INTO policy_checks(claim_id, passed) VALUES(?,?) "
                + "ON CONFLICT(claim_id) DO UPDATE SET passed = excluded.passed";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, claimId);
            ps.setInt(2, passed ? 1 : 0);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Cannot save check for " + claimId, e);
        }
    }

    public boolean isChecked(String claimId) {
        String sql = "SELECT passed FROM policy_checks WHERE claim_id = ?";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, claimId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt("passed") == 1;
            }
        } catch (SQLException e) {
            throw new RepositoryException("Cannot read check for " + claimId, e);
        }
    }
}