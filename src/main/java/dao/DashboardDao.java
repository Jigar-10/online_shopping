package dao;

import config.DatabaseConnection;
import exception.DatabaseException;
import model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardDao {
    private static final Logger logger = LoggerFactory.getLogger(DashboardDao.class);

    public record DashboardStats(
            int totalCustomers,
            int totalProducts,
            int totalCategories,
            int totalSuppliers,
            int totalOrders,
            BigDecimal totalRevenue,
            int pendingOrders,
            int pendingShipments,
            int lowStockCount,
            List<Order> recentOrders,
            Map<String, Integer> orderStatusCounts,
            Map<String, BigDecimal> paymentTypeRevenue
    ) {}

    public DashboardStats getStats() {
        int totalCustomers = getCount("SELECT COUNT(*) FROM CUSTOMER");
        int totalProducts = getCount("SELECT COUNT(*) FROM PRODUCT");
        int totalCategories = getCount("SELECT COUNT(*) FROM CATEGORY");
        int totalSuppliers = getCount("SELECT COUNT(*) FROM SUPPLIER");
        int totalOrders = getCount("SELECT COUNT(*) FROM ORDERS");
        BigDecimal totalRevenue = getDecimal("SELECT COALESCE(SUM(amount), 0) FROM PAYMENT WHERE status = 'Completed'");
        int pendingOrders = getCount("SELECT COUNT(*) FROM ORDERS WHERE status = 'Pending'");
        int pendingShipments = getCount("SELECT COUNT(*) FROM SHIPPING WHERE status = 'Pending'");
        int lowStockCount = getCount("SELECT COUNT(*) FROM PRODUCT WHERE stock_qty < 15");

        List<Order> recent = getRecentOrders(6);
        Map<String, Integer> statusCounts = getGroupCounts("SELECT status, COUNT(*) AS cnt FROM ORDERS GROUP BY status");
        Map<String, BigDecimal> paymentRevenue = getGroupDecimals("SELECT payment_type, COALESCE(SUM(amount), 0) AS rev FROM PAYMENT GROUP BY payment_type");

        return new DashboardStats(
                totalCustomers,
                totalProducts,
                totalCategories,
                totalSuppliers,
                totalOrders,
                totalRevenue,
                pendingOrders,
                pendingShipments,
                lowStockCount,
                recent,
                statusCounts,
                paymentRevenue
        );
    }

    private int getCount(String sql) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.warn("Could not execute count query: {}", e.getMessage());
        }
        return 0;
    }

    private BigDecimal getDecimal(String sql) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                BigDecimal val = rs.getBigDecimal(1);
                return val != null ? val : BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            logger.warn("Could not execute decimal query: {}", e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    private List<Order> getRecentOrders(int limit) {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.order_id, o.customer_id, c.name AS customer_name, " +
                     "o.shipment_id, s.tracking_no, o.order_date, o.total_amount, o.status " +
                     "FROM ORDERS o " +
                     "LEFT JOIN CUSTOMER c ON o.customer_id = c.customer_id " +
                     "LEFT JOIN SHIPPING s ON o.shipment_id = s.shipment_id " +
                     "ORDER BY o.order_date DESC, o.order_id DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("order_date");
                    Order o = new Order(
                            rs.getInt("order_id"),
                            rs.getInt("customer_id"),
                            rs.getObject("shipment_id") != null ? rs.getInt("shipment_id") : null,
                            ts != null ? ts.toLocalDateTime() : null,
                            rs.getBigDecimal("total_amount"),
                            rs.getString("status")
                    );
                    o.setCustomerName(rs.getString("customer_name"));
                    o.setTrackingNo(rs.getString("tracking_no"));
                    list.add(o);
                }
            }
        } catch (SQLException e) {
            logger.warn("Could not fetch recent orders: {}", e.getMessage());
        }
        return list;
    }

    private Map<String, Integer> getGroupCounts(String sql) {
        Map<String, Integer> map = new HashMap<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String key = rs.getString(1);
                int val = rs.getInt(2);
                if (key != null) map.put(key, val);
            }
        } catch (SQLException e) {
            logger.warn("Could not execute group count query: {}", e.getMessage());
        }
        return map;
    }

    private Map<String, BigDecimal> getGroupDecimals(String sql) {
        Map<String, BigDecimal> map = new HashMap<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String key = rs.getString(1);
                BigDecimal val = rs.getBigDecimal(2);
                if (key != null) map.put(key, val != null ? val : BigDecimal.ZERO);
            }
        } catch (SQLException e) {
            logger.warn("Could not execute group decimal query: {}", e.getMessage());
        }
        return map;
    }
}
