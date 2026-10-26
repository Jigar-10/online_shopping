package dao;

import config.DatabaseConnection;
import exception.DatabaseException;
import model.ComboItem;
import model.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SupplierDao implements GenericDao<Supplier, Integer> {
    private static final Logger logger = LoggerFactory.getLogger(SupplierDao.class);

    @Override
    public List<Supplier> findAll() {
        String sql = "SELECT supplier_id, name, contact_email, phone FROM SUPPLIER ORDER BY supplier_id ASC";
        List<Supplier> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding all suppliers", e);
            throw new DatabaseException("Failed to load suppliers: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
        return list;
    }

    @Override
    public Optional<Supplier> findById(Integer id) {
        String sql = "SELECT supplier_id, name, contact_email, phone FROM SUPPLIER WHERE supplier_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding supplier ID {}", id, e);
            throw new DatabaseException("Failed to find supplier: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
        return Optional.empty();
    }

    @Override
    public Supplier insert(Supplier s) {
        String sql = "INSERT INTO SUPPLIER (name, contact_email, phone) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getContactEmail());
            ps.setString(3, s.getPhone());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    s.setSupplierId(keys.getInt(1));
                }
            }
            return s;
        } catch (SQLException e) {
            logger.error("Error inserting supplier", e);
            if (e.getErrorCode() == 1062) {
                throw new DatabaseException("A supplier with contact email '" + s.getContactEmail() + "' already exists.", e.getSQLState(), e.getErrorCode(), e);
            }
            throw new DatabaseException("Failed to insert supplier: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public boolean update(Supplier s) {
        String sql = "UPDATE SUPPLIER SET name = ?, contact_email = ?, phone = ? WHERE supplier_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getContactEmail());
            ps.setString(3, s.getPhone());
            ps.setInt(4, s.getSupplierId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating supplier ID {}", s.getSupplierId(), e);
            if (e.getErrorCode() == 1062) {
                throw new DatabaseException("Another supplier with contact email '" + s.getContactEmail() + "' already exists.", e.getSQLState(), e.getErrorCode(), e);
            }
            throw new DatabaseException("Failed to update supplier: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM SUPPLIER WHERE supplier_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting supplier ID {}", id, e);
            if (e.getErrorCode() == 1451) {
                throw new DatabaseException("Cannot delete supplier (ID " + id + ") because products are supplied by them.", e.getSQLState(), e.getErrorCode(), e);
            }
            throw new DatabaseException("Failed to delete supplier: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public List<Supplier> search(String query) {
        if (query == null || query.isBlank()) return findAll();
        String sql = "SELECT supplier_id, name, contact_email, phone FROM SUPPLIER " +
                     "WHERE name LIKE ? OR contact_email LIKE ? OR phone LIKE ? ORDER BY supplier_id ASC";
        String pattern = "%" + query.trim() + "%";
        List<Supplier> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error searching suppliers", e);
            throw new DatabaseException("Failed to search suppliers: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
        return list;
    }

    public List<ComboItem<Integer>> findAllAsComboItems() {
        List<ComboItem<Integer>> items = new ArrayList<>();
        for (Supplier s : findAll()) {
            items.add(new ComboItem<>(s.getSupplierId(), s.getName()));
        }
        return items;
    }

    private Supplier mapRow(ResultSet rs) throws SQLException {
        return new Supplier(
                rs.getInt("supplier_id"),
                rs.getString("name"),
                rs.getString("contact_email"),
                rs.getString("phone")
        );
    }
}
