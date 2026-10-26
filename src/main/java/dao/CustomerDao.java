package dao;

import config.DatabaseConnection;
import exception.DatabaseException;
import model.ComboItem;
import model.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerDao implements GenericDao<Customer, Integer> {
    private static final Logger logger = LoggerFactory.getLogger(CustomerDao.class);

    @Override
    public List<Customer> findAll() {
        String sql = "SELECT customer_id, name, email, phone, registered_on, gender FROM CUSTOMER ORDER BY customer_id ASC";
        List<Customer> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding all customers", e);
            throw new DatabaseException("Failed to load customers: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
        return list;
    }

    @Override
    public Optional<Customer> findById(Integer id) {
        String sql = "SELECT customer_id, name, email, phone, registered_on, gender FROM CUSTOMER WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding customer by ID {}", id, e);
            throw new DatabaseException("Failed to find customer: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
        return Optional.empty();
    }

    @Override
    public Customer insert(Customer c) {
        String sql = "INSERT INTO CUSTOMER (name, email, phone, registered_on, gender) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getEmail());
            ps.setString(3, c.getPhone());
            ps.setDate(4, c.getRegisteredOn() != null ? Date.valueOf(c.getRegisteredOn()) : new Date(System.currentTimeMillis()));
            ps.setString(5, c.getGender());

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    c.setCustomerId(keys.getInt(1));
                }
            }
            return c;
        } catch (SQLException e) {
            logger.error("Error inserting customer: {}", c.getName(), e);
            if (e.getErrorCode() == 1062) {
                throw new DatabaseException("A customer with email '" + c.getEmail() + "' already exists.", e.getSQLState(), e.getErrorCode(), e);
            }
            throw new DatabaseException("Failed to insert customer: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public boolean update(Customer c) {
        String sql = "UPDATE CUSTOMER SET name = ?, email = ?, phone = ?, registered_on = ?, gender = ? WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getEmail());
            ps.setString(3, c.getPhone());
            ps.setDate(4, c.getRegisteredOn() != null ? Date.valueOf(c.getRegisteredOn()) : new Date(System.currentTimeMillis()));
            ps.setString(5, c.getGender());
            ps.setInt(6, c.getCustomerId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating customer ID {}", c.getCustomerId(), e);
            if (e.getErrorCode() == 1062) {
                throw new DatabaseException("Another customer with email '" + c.getEmail() + "' already exists.", e.getSQLState(), e.getErrorCode(), e);
            }
            throw new DatabaseException("Failed to update customer: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM CUSTOMER WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting customer ID {}", id, e);
            if (e.getErrorCode() == 1451) {
                throw new DatabaseException("Cannot delete customer (ID " + id + ") because related addresses, orders, or cart items exist.", e.getSQLState(), e.getErrorCode(), e);
            }
            throw new DatabaseException("Failed to delete customer: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public List<Customer> search(String query) {
        if (query == null || query.isBlank()) return findAll();
        String sql = "SELECT customer_id, name, email, phone, registered_on, gender FROM CUSTOMER " +
                     "WHERE name LIKE ? OR email LIKE ? OR phone LIKE ? ORDER BY customer_id ASC";
        String pattern = "%" + query.trim() + "%";
        List<Customer> list = new ArrayList<>();
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
            logger.error("Error searching customers with query {}", query, e);
            throw new DatabaseException("Failed to search customers: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
        return list;
    }

    public List<ComboItem<Integer>> findAllAsComboItems() {
        List<ComboItem<Integer>> items = new ArrayList<>();
        for (Customer c : findAll()) {
            items.add(new ComboItem<>(c.getCustomerId(), c.getName() + " (" + c.getEmail() + ")"));
        }
        return items;
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        Date regDate = rs.getDate("registered_on");
        return new Customer(
                rs.getInt("customer_id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("phone"),
                regDate != null ? regDate.toLocalDate() : null,
                rs.getString("gender")
        );
    }
}
