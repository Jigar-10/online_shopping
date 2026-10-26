package dao;

import config.DatabaseConnection;
import exception.DatabaseException;
import model.DigitalWallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DigitalWalletDao implements GenericDao<DigitalWallet, Integer> {
    private static final Logger logger = LoggerFactory.getLogger(DigitalWalletDao.class);

    @Override
    public List<DigitalWallet> findAll() {
        String sql = "SELECT payment_id, provider, wallet_ref FROM DIGITAL_WALLET ORDER BY payment_id ASC";
        List<DigitalWallet> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding digital wallets", e);
            throw new DatabaseException("Failed to load digital wallets: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
        return list;
    }

    @Override
    public Optional<DigitalWallet> findById(Integer paymentId) {
        String sql = "SELECT payment_id, provider, wallet_ref FROM DIGITAL_WALLET WHERE payment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, paymentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding digital wallet for payment {}", paymentId, e);
            throw new DatabaseException("Failed to find digital wallet: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
        return Optional.empty();
    }

    @Override
    public DigitalWallet insert(DigitalWallet wallet) {
        String sql = "INSERT INTO DIGITAL_WALLET (payment_id, provider, wallet_ref) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, wallet.getPaymentId());
            ps.setString(2, wallet.getProvider());
            ps.setString(3, wallet.getWalletRef());
            ps.executeUpdate();
            return wallet;
        } catch (SQLException e) {
            logger.error("Error inserting digital wallet", e);
            if (e.getErrorCode() == 1062) {
                throw new DatabaseException("Digital wallet details already exist for Payment ID " + wallet.getPaymentId(), e.getSQLState(), e.getErrorCode(), e);
            } else if (e.getErrorCode() == 1452) {
                throw new DatabaseException("Payment ID " + wallet.getPaymentId() + " does not exist.", e.getSQLState(), e.getErrorCode(), e);
            }
            throw new DatabaseException("Failed to insert digital wallet: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public boolean update(DigitalWallet wallet) {
        String sql = "UPDATE DIGITAL_WALLET SET provider = ?, wallet_ref = ? WHERE payment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, wallet.getProvider());
            ps.setString(2, wallet.getWalletRef());
            ps.setInt(3, wallet.getPaymentId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating digital wallet for payment {}", wallet.getPaymentId(), e);
            throw new DatabaseException("Failed to update digital wallet: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public boolean delete(Integer paymentId) {
        String sql = "DELETE FROM DIGITAL_WALLET WHERE payment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, paymentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting digital wallet for payment {}", paymentId, e);
            throw new DatabaseException("Failed to delete digital wallet: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public List<DigitalWallet> search(String query) {
        if (query == null || query.isBlank()) return findAll();
        String sql = "SELECT payment_id, provider, wallet_ref FROM DIGITAL_WALLET " +
                     "WHERE provider LIKE ? OR wallet_ref LIKE ? ORDER BY payment_id ASC";
        String pattern = "%" + query.trim() + "%";
        List<DigitalWallet> list = new ArrayList<>();
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
            logger.error("Error searching digital wallets", e);
            throw new DatabaseException("Failed to search digital wallets: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
        return list;
    }

    private DigitalWallet mapRow(ResultSet rs) throws SQLException {
        return new DigitalWallet(
                rs.getInt("payment_id"),
                rs.getString("provider"),
                rs.getString("wallet_ref")
        );
    }
}
