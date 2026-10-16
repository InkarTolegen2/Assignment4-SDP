package claims.intake;

import claims.exception.RepositoryException;

import java.sql.*;
import java.util.Optional;

public class ClaimRepository {
    private final String url;

    public ClaimRepository(String dbPath) {
        this.url = "jdbc:sqlite:" + dbPath;
        createTable();
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(url);
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS claims ("
                + "id TEXT PRIMARY KEY, status TEXT NOT NULL)";
        try (Connection c = connect(); Statement s = c.createStatement()) {
            s.execute(sql);
        } catch (SQLException e) {
            throw new RepositoryException("Cannot create claims table", e);
        }
    }

    public void save(Claim claim) {
        String sql = "INSERT INTO claims(id, status) VALUES(?, ?) "
                + "ON CONFLICT(id) DO UPDATE SET status = excluded.status";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, claim.getId());
            ps.setString(2, claim.getStatus().name());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Cannot save claim " + claim.getId(), e);
        }
    }

    public Optional<Claim> findById(String id) {
        String sql = "SELECT status FROM claims WHERE id = ?";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                ClaimStatus status = ClaimStatus.valueOf(rs.getString("status"));
                return Optional.of(Claim.restore(id, status));
            }
        } catch (SQLException e) {
            throw new RepositoryException("Cannot load claim " + id, e);
        }
    }
}
