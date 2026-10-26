package service;

import config.DatabaseConnection;
import exception.DatabaseException;
import model.Order;
import model.OrderItem;
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
import java.sql.Types;
import java.util.List;

public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    /**
     * Executes a complete transactional order placement:
     * 1. Validates input
     * 2. Begins database transaction
     * 3. Inserts ORDERS record
     * 4. Inserts each ORDER_ITEM (MySQL triggers verify stock and reduce inventory)
     * 5. Recalculates total amount based on items
     * 6. Inserts PAYMENT record if amount > 0
     * 7. Commits transaction or rolls back on error
     */
    public Order placeOrderTransaction(Order order, List<OrderItem> items, Payment payment) {
        ValidationService.validateOrder(order);
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("An order must contain at least one item.");
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Begin transaction

            // 1. Calculate total
            BigDecimal total = BigDecimal.ZERO;
            for (OrderItem it : items) {
                total = total.add(it.getSubtotal());
            }
            order.setTotalAmount(total);

            // 2. Insert Order
            String orderSql = "INSERT INTO ORDERS (customer_id, shipment_id, order_date, total_amount, status) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, order.getCustomerId());
                if (order.getShipmentId() != null) {
                    ps.setInt(2, order.getShipmentId());
                } else {
                    ps.setNull(2, Types.INTEGER);
                }
                ps.setTimestamp(3, Timestamp.valueOf(order.getOrderDate()));
                ps.setBigDecimal(4, total);
                ps.setString(5, order.getStatus());
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        order.setOrderId(keys.getInt(1));
                    }
                }
            }

            // 3. Insert Order Items (Database trigger will reduce product stock)
            String itemSql = "INSERT INTO ORDER_ITEM (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(itemSql)) {
                for (OrderItem it : items) {
                    it.setOrderId(order.getOrderId());
                    ps.setInt(1, order.getOrderId());
                    ps.setInt(2, it.getProductId());
                    ps.setInt(3, it.getQuantity());
                    ps.setBigDecimal(4, it.getUnitPrice());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // 4. Insert Payment if requested
            if (payment != null && payment.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                payment.setOrderId(order.getOrderId());
                String paySql = "INSERT INTO PAYMENT (order_id, amount, payment_date, payment_type, status) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(paySql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, order.getOrderId());
                    ps.setBigDecimal(2, payment.getAmount());
                    ps.setTimestamp(3, Timestamp.valueOf(payment.getPaymentDate()));
                    ps.setString(4, payment.getPaymentType());
                    ps.setString(5, payment.getStatus());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            payment.setPaymentId(keys.getInt(1));
                        }
                    }
                }
            }

            conn.commit(); // Commit transaction
            logger.info("Successfully committed order #{} with {} items", order.getOrderId(), items.size());
            return order;

        } catch (SQLException e) {
            DatabaseConnection.rollback(conn);
            logger.error("Transaction failed, rolled back order", e);
            throw new DatabaseException("Failed to place order (Transaction rolled back): " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }
}
