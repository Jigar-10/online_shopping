package dao;

import config.DatabaseConnection;
import exception.DatabaseException;
import model.DdlDemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DdlDemoDao implements GenericDao<DdlDemo, Integer> {
    private static final Logger logger = LoggerFactory.getLogger(DdlDemoDao.class);

    @Override
    public List<DdlDemo> findAll() {
        String sql = "SELECT demo_id, demo_name, demo_value FROM DDL_DEMO ORDER BY demo_id ASC";
        List<DdlDemo> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.warn("DDL_DEMO table not found or error: {}", e.getMessage());
        }
        return list;
    }

    @Override
    public Optional<DdlDemo> findById(Integer id) {
        String sql = "SELECT demo_id, demo_name, demo_value FROM DDL_DEMO WHERE demo_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding DDL_DEMO ID {}", id, e);
        }
        return Optional.empty();
    }

    @Override
    public DdlDemo insert(DdlDemo demo) {
        String sql = "INSERT INTO DDL_DEMO (demo_id, demo_name, demo_value) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, demo.getDemoId());
            ps.setString(2, demo.getDemoName());
            ps.setInt(3, demo.getDemoValue());
            ps.executeUpdate();
            return demo;
        } catch (SQLException e) {
            logger.error("Error inserting DDL_DEMO", e);
            throw new DatabaseException("Failed to insert DDL demo: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public boolean update(DdlDemo demo) {
        String sql = "UPDATE DDL_DEMO SET demo_name = ?, demo_value = ? WHERE demo_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, demo.getDemoName());
            ps.setInt(2, demo.getDemoValue());
            ps.setInt(3, demo.getDemoId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating DDL_DEMO ID {}", demo.getDemoId(), e);
            throw new DatabaseException("Failed to update DDL demo: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM DDL_DEMO WHERE demo_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting DDL_DEMO ID {}", id, e);
            throw new DatabaseException("Failed to delete DDL demo: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public List<DdlDemo> search(String query) {
        if (query == null || query.isBlank()) return findAll();
        String sql = "SELECT demo_id, demo_name, demo_value FROM DDL_DEMO WHERE demo_name LIKE ? ORDER BY demo_id ASC";
        String pattern = "%" + query.trim() + "%";
        List<DdlDemo> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error searching DDL_DEMO", e);
        }
        return list;
    }

    private DdlDemo mapRow(ResultSet rs) throws SQLException {
        return new DdlDemo(
                rs.getInt("demo_id"),
                rs.getString("demo_name"),
                rs.getInt("demo_value")
        );
    }
}
