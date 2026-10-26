package dao;

import config.DatabaseConnection;
import exception.DatabaseException;
import model.OrderItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderItemDao {
    private static final Logger logger = LoggerFactory.getLogger(OrderItemDao.class);

    private static final String BASE_SELECT =
            "SELECT oi.order_id, oi.product_id, p.name AS product_name, " +
            "oi.quantity, oi.unit_price, (oi.quantity * oi.unit_price) AS subtotal " +
            "FROM ORDER_ITEM oi " +
            "LEFT JOIN PRODUCT p ON oi.product_id = p.product_id ";

    public List<OrderItem> findAll() {
        String sql = BASE_SELECT + "ORDER BY oi.order_id ASC, oi.product_id ASC";
        List<OrderItem> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding all order items", e);
            throw new DatabaseException("Failed to load order items: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
        return list;
    }

    public List<OrderItem> findByOrderId(int orderId) {
        String sql = BASE_SELECT + "WHERE oi.order_id = ? ORDER BY oi.product_id ASC";
        List<OrderItem> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding items for order {}", orderId, e);
            throw new DatabaseException("Failed to load order items: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
        return list;
    }

    public Optional<OrderItem> findById(int orderId, int productId) {
        String sql = BASE_SELECT + "WHERE oi.order_id = ? AND oi.product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding order item ({}, {})", orderId, productId, e);
            throw new DatabaseException("Failed to find order item: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
        return Optional.empty();
    }

    public OrderItem insert(OrderItem item) {
        String sql = "INSERT INTO ORDER_ITEM (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, item.getOrderId());
            ps.setInt(2, item.getProductId());
            ps.setInt(3, item.getQuantity() != null ? item.getQuantity() : 1);
            ps.setBigDecimal(4, item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO);
            ps.executeUpdate();
            return item;
        } catch (SQLException e) {
            logger.error("Error inserting order item", e);
            if (e.getErrorCode() == 1062) {
                throw new DatabaseException("This product is already part of this order. Update its quantity instead.", e.getSQLState(), e.getErrorCode(), e);
            } else if (e.getErrorCode() == 1452) {
                throw new DatabaseException("Selected Order or Product does not exist.", e.getSQLState(), e.getErrorCode(), e);
            } else if (e.getErrorCode() == 3819 || e.getMessage().contains("chk_order_item")) {
                throw new DatabaseException("Order item quantity and unit price must be positive numbers.", e.getSQLState(), e.getErrorCode(), e);
            }
            throw new DatabaseException("Failed to insert order item: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    public boolean update(OrderItem item) {
        String sql = "UPDATE ORDER_ITEM SET quantity = ?, unit_price = ? WHERE order_id = ? AND product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, item.getQuantity());
            ps.setBigDecimal(2, item.getUnitPrice());
            ps.setInt(3, item.getOrderId());
            ps.setInt(4, item.getProductId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating order item ({}, {})", item.getOrderId(), item.getProductId(), e);
            if (e.getErrorCode() == 3819 || e.getMessage().contains("chk_order_item")) {
                throw new DatabaseException("Order item quantity and unit price must be positive numbers.", e.getSQLState(), e.getErrorCode(), e);
            }
            throw new DatabaseException("Failed to update order item: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    public boolean delete(int orderId, int productId) {
        String sql = "DELETE FROM ORDER_ITEM WHERE order_id = ? AND product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting order item ({}, {})", orderId, productId, e);
            throw new DatabaseException("Failed to delete order item: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    public List<OrderItem> search(String query) {
        if (query == null || query.isBlank()) return findAll();
        String sql = BASE_SELECT + "WHERE p.name LIKE ? ORDER BY oi.order_id ASC";
        String pattern = "%" + query.trim() + "%";
        List<OrderItem> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error searching order items", e);
            throw new DatabaseException("Failed to search order items: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
        return list;
    }

    private OrderItem mapRow(ResultSet rs) throws SQLException {
        OrderItem item = new OrderItem(
                rs.getInt("order_id"),
                rs.getInt("product_id"),
                rs.getInt("quantity"),
                rs.getBigDecimal("unit_price")
        );
        item.setProductName(rs.getString("product_name"));
        item.setSubtotal(rs.getBigDecimal("subtotal"));
        return item;
    }
}
