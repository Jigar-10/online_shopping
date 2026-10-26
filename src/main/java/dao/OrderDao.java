package dao;

import config.DatabaseConnection;
import exception.DatabaseException;
import model.ComboItem;
import model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderDao implements GenericDao<Order, Integer> {
    private static final Logger logger = LoggerFactory.getLogger(OrderDao.class);

    private static final String BASE_SELECT =
            "SELECT o.order_id, o.customer_id, c.name AS customer_name, " +
            "o.shipment_id, s.tracking_no, o.order_date, o.total_amount, o.status " +
            "FROM ORDERS o " +
            "LEFT JOIN CUSTOMER c ON o.customer_id = c.customer_id " +
            "LEFT JOIN SHIPPING s ON o.shipment_id = s.shipment_id ";

    @Override
    public List<Order> findAll() {
        String sql = BASE_SELECT + "ORDER BY o.order_id ASC";
        List<Order> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding all orders", e);
            throw new DatabaseException("Failed to load orders: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
        return list;
    }

    @Override
    public Optional<Order> findById(Integer id) {
        String sql = BASE_SELECT + "WHERE o.order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding order ID {}", id, e);
            throw new DatabaseException("Failed to find order: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
        return Optional.empty();
    }

    @Override
    public Order insert(Order o) {
        String sql = "INSERT INTO ORDERS (customer_id, shipment_id, order_date, total_amount, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, o.getCustomerId());
            if (o.getShipmentId() != null) {
                ps.setInt(2, o.getShipmentId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setTimestamp(3, o.getOrderDate() != null ? Timestamp.valueOf(o.getOrderDate()) : new Timestamp(System.currentTimeMillis()));
            ps.setBigDecimal(4, o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO);
            ps.setString(5, o.getStatus() != null ? o.getStatus() : "Pending");
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    o.setOrderId(keys.getInt(1));
                }
            }
            return o;
        } catch (SQLException e) {
            logger.error("Error inserting order", e);
            if (e.getErrorCode() == 1452) {
                throw new DatabaseException("Selected Customer or Shipment does not exist.", e.getSQLState(), e.getErrorCode(), e);
            } else if (e.getErrorCode() == 3819 || e.getMessage().contains("chk_order_amount")) {
                throw new DatabaseException("Total amount must be greater than or equal to zero.", e.getSQLState(), e.getErrorCode(), e);
            }
            throw new DatabaseException("Failed to insert order: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public boolean update(Order o) {
        String sql = "UPDATE ORDERS SET customer_id = ?, shipment_id = ?, order_date = ?, total_amount = ?, status = ? WHERE order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, o.getCustomerId());
            if (o.getShipmentId() != null) {
                ps.setInt(2, o.getShipmentId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setTimestamp(3, o.getOrderDate() != null ? Timestamp.valueOf(o.getOrderDate()) : new Timestamp(System.currentTimeMillis()));
            ps.setBigDecimal(4, o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO);
            ps.setString(5, o.getStatus());
            ps.setInt(6, o.getOrderId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating order ID {}", o.getOrderId(), e);
            if (e.getErrorCode() == 1452) {
                throw new DatabaseException("Selected Customer or Shipment does not exist.", e.getSQLState(), e.getErrorCode(), e);
            }
            throw new DatabaseException("Failed to update order: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM ORDERS WHERE order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting order ID {}", id, e);
            if (e.getErrorCode() == 1451) {
                throw new DatabaseException("Cannot delete order (ID " + id + ") because related order items or payments exist.", e.getSQLState(), e.getErrorCode(), e);
            }
            throw new DatabaseException("Failed to delete order: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public List<Order> search(String query) {
        if (query == null || query.isBlank()) return findAll();
        String sql = BASE_SELECT +
                     "WHERE c.name LIKE ? OR o.status LIKE ? OR s.tracking_no LIKE ? " +
                     "ORDER BY o.order_id ASC";
        String pattern = "%" + query.trim() + "%";
        List<Order> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 1; i <= 3; i++) ps.setString(i, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error searching orders", e);
            throw new DatabaseException("Failed to search orders: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
        return list;
    }

    public List<ComboItem<Integer>> findAllAsComboItems() {
        List<ComboItem<Integer>> items = new ArrayList<>();
        for (Order o : findAll()) {
            items.add(new ComboItem<>(o.getOrderId(), "Order #" + o.getOrderId() + " (" + (o.getCustomerName() != null ? o.getCustomerName() : "Cust " + o.getCustomerId()) + ") - \u20B9" + o.getTotalAmount()));
        }
        return items;
    }

    private Order mapRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("order_date");
        int shipmentId = rs.getInt("shipment_id");
        Integer shipObj = rs.wasNull() ? null : shipmentId;

        Order o = new Order(
                rs.getInt("order_id"),
                rs.getInt("customer_id"),
                shipObj,
                ts != null ? ts.toLocalDateTime() : null,
                rs.getBigDecimal("total_amount"),
                rs.getString("status")
        );
        o.setCustomerName(rs.getString("customer_name"));
        o.setTrackingNo(rs.getString("tracking_no"));
        return o;
    }
}
