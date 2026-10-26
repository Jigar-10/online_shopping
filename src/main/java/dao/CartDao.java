package dao;

import config.DatabaseConnection;
import exception.DatabaseException;
import model.Cart;
import model.ComboItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CartDao implements GenericDao<Cart, Integer> {
    private static final Logger logger = LoggerFactory.getLogger(CartDao.class);

    private static final String BASE_SELECT =
            "SELECT c.cart_id, c.customer_id, cust.name AS customer_name, c.created_on " +
            "FROM CART c LEFT JOIN CUSTOMER cust ON c.customer_id = cust.customer_id ";

    @Override
    public List<Cart> findAll() {
        String sql = BASE_SELECT + "ORDER BY c.cart_id ASC";
        List<Cart> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding all carts", e);
            throw new DatabaseException("Failed to load carts: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
        return list;
    }

    @Override
    public Optional<Cart> findById(Integer id) {
        String sql = BASE_SELECT + "WHERE c.cart_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding cart ID {}", id, e);
            throw new DatabaseException("Failed to find cart: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
        return Optional.empty();
    }

    @Override
    public Cart insert(Cart c) {
        String sql = "INSERT INTO CART (customer_id, created_on) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, c.getCustomerId());
            ps.setTimestamp(2, c.getCreatedOn() != null ? Timestamp.valueOf(c.getCreatedOn()) : new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    c.setCartId(keys.getInt(1));
                }
            }
            return c;
        } catch (SQLException e) {
            logger.error("Error inserting cart", e);
            if (e.getErrorCode() == 1452) {
                throw new DatabaseException("Selected customer (ID " + c.getCustomerId() + ") does not exist.", e.getSQLState(), e.getErrorCode(), e);
            }
            throw new DatabaseException("Failed to insert cart: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public boolean update(Cart c) {
        String sql = "UPDATE CART SET customer_id = ?, created_on = ? WHERE cart_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, c.getCustomerId());
            ps.setTimestamp(2, c.getCreatedOn() != null ? Timestamp.valueOf(c.getCreatedOn()) : new Timestamp(System.currentTimeMillis()));
            ps.setInt(3, c.getCartId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating cart ID {}", c.getCartId(), e);
            if (e.getErrorCode() == 1452) {
                throw new DatabaseException("Selected customer does not exist.", e.getSQLState(), e.getErrorCode(), e);
            }
            throw new DatabaseException("Failed to update cart: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM CART WHERE cart_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting cart ID {}", id, e);
            if (e.getErrorCode() == 1451) {
                throw new DatabaseException("Cannot delete cart (ID " + id + ") because it contains items.", e.getSQLState(), e.getErrorCode(), e);
            }
            throw new DatabaseException("Failed to delete cart: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public List<Cart> search(String query) {
        if (query == null || query.isBlank()) return findAll();
        String sql = BASE_SELECT + "WHERE cust.name LIKE ? OR cust.email LIKE ? ORDER BY c.cart_id ASC";
        String pattern = "%" + query.trim() + "%";
        List<Cart> list = new ArrayList<>();
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
            logger.error("Error searching carts", e);
            throw new DatabaseException("Failed to search carts: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
        return list;
    }

    public List<ComboItem<Integer>> findAllAsComboItems() {
        List<ComboItem<Integer>> items = new ArrayList<>();
        for (Cart c : findAll()) {
            items.add(new ComboItem<>(c.getCartId(), "Cart #" + c.getCartId() + " (" + (c.getCustomerName() != null ? c.getCustomerName() : "Cust " + c.getCustomerId()) + ")"));
        }
        return items;
    }

    private Cart mapRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("created_on");
        Cart c = new Cart(
                rs.getInt("cart_id"),
                rs.getInt("customer_id"),
                ts != null ? ts.toLocalDateTime() : null
        );
        c.setCustomerName(rs.getString("customer_name"));
        return c;
    }
}
