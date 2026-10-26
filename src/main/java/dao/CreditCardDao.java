package dao;

import config.DatabaseConnection;
import exception.DatabaseException;
import model.CreditCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CreditCardDao implements GenericDao<CreditCard, Integer> {
    private static final Logger logger = LoggerFactory.getLogger(CreditCardDao.class);

    @Override
    public List<CreditCard> findAll() {
        String sql = "SELECT payment_id, card_number, cvv, expiry_date FROM CREDIT_CARD ORDER BY payment_id ASC";
        List<CreditCard> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding credit cards", e);
            throw new DatabaseException("Failed to load credit cards: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
        return list;
    }

    @Override
    public Optional<CreditCard> findById(Integer paymentId) {
        String sql = "SELECT payment_id, card_number, cvv, expiry_date FROM CREDIT_CARD WHERE payment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, paymentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding credit card for payment {}", paymentId, e);
            throw new DatabaseException("Failed to find credit card: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
        return Optional.empty();
    }

    @Override
    public CreditCard insert(CreditCard card) {
        String sql = "INSERT INTO CREDIT_CARD (payment_id, card_number, cvv, expiry_date) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, card.getPaymentId());
            ps.setString(2, card.getCardNumber());
            ps.setString(3, card.getCvv());
            ps.setDate(4, card.getExpiryDate() != null ? Date.valueOf(card.getExpiryDate()) : null);
            ps.executeUpdate();
            return card;
        } catch (SQLException e) {
            logger.error("Error inserting credit card", e);
            if (e.getErrorCode() == 1062) {
                throw new DatabaseException("Credit card details already exist for Payment ID " + card.getPaymentId(), e.getSQLState(), e.getErrorCode(), e);
            } else if (e.getErrorCode() == 1452) {
                throw new DatabaseException("Payment ID " + card.getPaymentId() + " does not exist.", e.getSQLState(), e.getErrorCode(), e);
            }
            throw new DatabaseException("Failed to insert credit card: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public boolean update(CreditCard card) {
        String sql = "UPDATE CREDIT_CARD SET card_number = ?, cvv = ?, expiry_date = ? WHERE payment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, card.getCardNumber());
            ps.setString(2, card.getCvv());
            ps.setDate(3, card.getExpiryDate() != null ? Date.valueOf(card.getExpiryDate()) : null);
            ps.setInt(4, card.getPaymentId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating credit card for payment {}", card.getPaymentId(), e);
            throw new DatabaseException("Failed to update credit card: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public boolean delete(Integer paymentId) {
        String sql = "DELETE FROM CREDIT_CARD WHERE payment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, paymentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting credit card for payment {}", paymentId, e);
            throw new DatabaseException("Failed to delete credit card: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public List<CreditCard> search(String query) {
        if (query == null || query.isBlank()) return findAll();
        String sql = "SELECT payment_id, card_number, cvv, expiry_date FROM CREDIT_CARD WHERE card_number LIKE ? ORDER BY payment_id ASC";
        String pattern = "%" + query.trim() + "%";
        List<CreditCard> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error searching credit cards", e);
            throw new DatabaseException("Failed to search credit cards: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
        return list;
    }

    private CreditCard mapRow(ResultSet rs) throws SQLException {
        Date exp = rs.getDate("expiry_date");
        return new CreditCard(
                rs.getInt("payment_id"),
                rs.getString("card_number"),
                rs.getString("cvv"),
                exp != null ? exp.toLocalDate() : null
        );
    }
}
