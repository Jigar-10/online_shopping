package dao;

import config.DatabaseConnection;
import exception.DatabaseException;
import model.CartItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CartItemDao {
    private static final Logger logger = LoggerFactory.getLogger(CartItemDao.class);

    private static final String BASE_SELECT =
            "SELECT ci.cart_id, ci.product_id, p.name AS product_name, ci.quantity, p.price AS unit_price, " +
            "(ci.quantity * p.price) AS subtotal " +
            "FROM CART_ITEM ci " +
            "LEFT JOIN PRODUCT p ON ci.product_id = p.product_id ";

    public List<CartItem> findAll() {
        String sql = BASE_SELECT + "ORDER BY ci.cart_id ASC, ci.product_id ASC";
        List<CartItem> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding cart items", e);
            throw new DatabaseException("Failed to load cart items: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
        return list;
    }

    public List<CartItem> findByCartId(int cartId) {
        String sql = BASE_SELECT + "WHERE ci.cart_id = ? ORDER BY ci.product_id ASC";
        List<CartItem> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cartId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding cart items for cart {}", cartId, e);
            throw new DatabaseException("Failed to load cart items: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
        return list;
    }

    public Optional<CartItem> findById(int cartId, int productId) {
        String sql = BASE_SELECT + "WHERE ci.cart_id = ? AND ci.product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cartId);
            ps.setInt(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding cart item ({}, {})", cartId, productId, e);
            throw new DatabaseException("Failed to find cart item: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
        return Optional.empty();
    }

    public CartItem insert(CartItem item) {
        String sql = "INSERT INTO CART_ITEM (cart_id, product_id, quantity) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, item.getCartId());
            ps.setInt(2, item.getProductId());
            ps.setInt(3, item.getQuantity() != null ? item.getQuantity() : 1);
            ps.executeUpdate();
            return item;
        } catch (SQLException e) {
            logger.error("Error inserting cart item", e);
            if (e.getErrorCode() == 1062) {
                throw new DatabaseException("This product is already in the specified cart. Update its quantity instead.", e.getSQLState(), e.getErrorCode(), e);
            } else if (e.getErrorCode() == 1452) {
                throw new DatabaseException("Selected Cart or Product does not exist.", e.getSQLState(), e.getErrorCode(), e);
            } else if (e.getErrorCode() == 3819 || e.getMessage().contains("chk_cart_item_quantity")) {
                throw new DatabaseException("Cart item quantity must be greater than zero.", e.getSQLState(), e.getErrorCode(), e);
            }
            throw new DatabaseException("Failed to insert cart item: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    public boolean update(CartItem item) {
        String sql = "UPDATE CART_ITEM SET quantity = ? WHERE cart_id = ? AND product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, item.getQuantity());
            ps.setInt(2, item.getCartId());
            ps.setInt(3, item.getProductId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating cart item ({}, {})", item.getCartId(), item.getProductId(), e);
            if (e.getErrorCode() == 3819 || e.getMessage().contains("chk_cart_item_quantity")) {
                throw new DatabaseException("Cart item quantity must be greater than zero.", e.getSQLState(), e.getErrorCode(), e);
            }
            throw new DatabaseException("Failed to update cart item: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    public boolean delete(int cartId, int productId) {
        String sql = "DELETE FROM CART_ITEM WHERE cart_id = ? AND product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cartId);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting cart item ({}, {})", cartId, productId, e);
            throw new DatabaseException("Failed to delete cart item: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    public List<CartItem> search(String query) {
        if (query == null || query.isBlank()) return findAll();
        String sql = BASE_SELECT + "WHERE p.name LIKE ? ORDER BY ci.cart_id ASC";
        String pattern = "%" + query.trim() + "%";
        List<CartItem> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error searching cart items", e);
            throw new DatabaseException("Failed to search cart items: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
        return list;
    }

    private CartItem mapRow(ResultSet rs) throws SQLException {
        CartItem item = new CartItem(
                rs.getInt("cart_id"),
                rs.getInt("product_id"),
                rs.getInt("quantity")
        );
        item.setProductName(rs.getString("product_name"));
        item.setUnitPrice(rs.getBigDecimal("unit_price"));
        item.setSubtotal(rs.getBigDecimal("subtotal"));
        return item;
    }
}
