package dao;

import config.DatabaseConnection;
import exception.DatabaseException;
import model.ComboItem;
import model.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PaymentDao implements GenericDao<Payment, Integer> {
    private static final Logger logger = LoggerFactory.getLogger(PaymentDao.class);

    private static final String BASE_SELECT =
            "SELECT p.payment_id, p.order_id, p.amount, p.payment_date, p.payment_type, p.status " +
            "FROM PAYMENT p ";

    @Override
    public List<Payment> findAll() {
        String sql = BASE_SELECT + "ORDER BY p.payment_id ASC";
        List<Payment> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding all payments", e);
            throw new DatabaseException("Failed to load payments: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
        return list;
    }

    @Override
    public Optional<Payment> findById(Integer id) {
        String sql = BASE_SELECT + "WHERE p.payment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding payment ID {}", id, e);
            throw new DatabaseException("Failed to find payment: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
        return Optional.empty();
    }

    @Override
    public Payment insert(Payment p) {
        String sql = "INSERT INTO PAYMENT (order_id, amount, payment_date, payment_type, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getOrderId());
            ps.setBigDecimal(2, p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO);
            ps.setTimestamp(3, p.getPaymentDate() != null ? Timestamp.valueOf(p.getPaymentDate()) : new Timestamp(System.currentTimeMillis()));
            ps.setString(4, p.getPaymentType());
            ps.setString(5, p.getStatus() != null ? p.getStatus() : "Pending");
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    p.setPaymentId(keys.getInt(1));
                }
            }
            return p;
        } catch (SQLException e) {
            logger.error("Error inserting payment", e);
            if (e.getErrorCode() == 1452) {
                throw new DatabaseException("Selected Order does not exist.", e.getSQLState(), e.getErrorCode(), e);
            } else if (e.getErrorCode() == 3819 || e.getMessage().contains("chk_payment_amount")) {
                throw new DatabaseException("Payment amount must be greater than or equal to zero.", e.getSQLState(), e.getErrorCode(), e);
            }
            throw new DatabaseException("Failed to insert payment: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public boolean update(Payment p) {
        String sql = "UPDATE PAYMENT SET order_id = ?, amount = ?, payment_date = ?, payment_type = ?, status = ? WHERE payment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, p.getOrderId());
            ps.setBigDecimal(2, p.getAmount());
            ps.setTimestamp(3, p.getPaymentDate() != null ? Timestamp.valueOf(p.getPaymentDate()) : new Timestamp(System.currentTimeMillis()));
            ps.setString(4, p.getPaymentType());
            ps.setString(5, p.getStatus());
            ps.setInt(6, p.getPaymentId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating payment ID {}", p.getPaymentId(), e);
            if (e.getErrorCode() == 1452) {
                throw new DatabaseException("Selected Order does not exist.", e.getSQLState(), e.getErrorCode(), e);
            }
            throw new DatabaseException("Failed to update payment: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM PAYMENT WHERE payment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting payment ID {}", id, e);
            if (e.getErrorCode() == 1451) {
                throw new DatabaseException("Cannot delete payment (ID " + id + ") because credit card or digital wallet records link to it.", e.getSQLState(), e.getErrorCode(), e);
            }
            throw new DatabaseException("Failed to delete payment: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public List<Payment> search(String query) {
        if (query == null || query.isBlank()) return findAll();
        String sql = BASE_SELECT +
                     "WHERE p.payment_type LIKE ? OR p.status LIKE ? " +
                     "ORDER BY p.payment_id ASC";
        String pattern = "%" + query.trim() + "%";
        List<Payment> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error searching payments", e);
            throw new DatabaseException("Failed to search payments: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
        return list;
    }

    public List<ComboItem<Integer>> findAllAsComboItems(String filterType) {
        List<ComboItem<Integer>> items = new ArrayList<>();
        for (Payment p : findAll()) {
            if (filterType == null || filterType.equalsIgnoreCase(p.getPaymentType())) {
                items.add(new ComboItem<>(p.getPaymentId(), "Payment #" + p.getPaymentId() + " (Order #" + p.getOrderId() + " - \u20B9" + p.getAmount() + " [" + p.getPaymentType() + "])"));
            }
        }
        return items;
    }

    private Payment mapRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("payment_date");
        return new Payment(
                rs.getInt("payment_id"),
                rs.getInt("order_id"),
                rs.getBigDecimal("amount"),
                ts != null ? ts.toLocalDateTime() : null,
                rs.getString("payment_type"),
                rs.getString("status")
        );
    }
}
