package service;

import config.DatabaseConnection;
import dao.*;
import exception.DatabaseException;
import model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CustomerPortalService {
    private static final Logger logger = LoggerFactory.getLogger(CustomerPortalService.class);

    private final ProductDao productDao = new ProductDao();
    private final CartDao cartDao = new CartDao();
    private final CartItemDao cartItemDao = new CartItemDao();
    private final OrderDao orderDao = new OrderDao();
    private final OrderItemDao orderItemDao = new OrderItemDao();
    private final AddressDao addressDao = new AddressDao();
    private final ReviewDao reviewDao = new ReviewDao();

    /**
     * Gets active cart for customer, or creates one if not present.
     */
    public Cart getOrCreateCart(int customerId) {
        String sql = "SELECT cart_id, customer_id, created_on FROM CART WHERE customer_id = ? ORDER BY cart_id DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp ts = rs.getTimestamp("created_on");
                    return new Cart(rs.getInt("cart_id"), customerId, ts != null ? ts.toLocalDateTime() : LocalDateTime.now());
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching cart for customer {}", customerId, e);
        }

        // Create new cart
        Cart newCart = new Cart(null, customerId, LocalDateTime.now());
        return cartDao.insert(newCart);
    }

    /**
     * Add product to customer cart (or increment if already in cart).
     */
    public void addItemToCart(int cartId, int productId, int quantity) {
        var existing = cartItemDao.findById(cartId, productId);
        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemDao.update(item);
        } else {
            CartItem item = new CartItem(cartId, productId, quantity);
            cartItemDao.insert(item);
        }
    }

    /**
     * Get all items in a cart with full product info and subtotal.
     */
    public List<CartItem> getCartItems(int cartId) {
        return cartItemDao.findByCartId(cartId);
    }

    /**
     * Update quantity of an item in cart.
     */
    public void updateCartItemQuantity(int cartId, int productId, int newQuantity) {
        if (newQuantity <= 0) {
            cartItemDao.delete(cartId, productId);
        } else {
            CartItem item = new CartItem(cartId, productId, newQuantity);
            cartItemDao.update(item);
        }
    }

    /**
     * Remove item from cart.
     */
    public void removeCartItem(int cartId, int productId) {
        cartItemDao.delete(cartId, productId);
    }

    /**
     * Clear all items in cart.
     */
    public void clearCart(int cartId) {
        String sql = "DELETE FROM CART_ITEM WHERE cart_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cartId);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error clearing cart {}", cartId, e);
        }
    }

    /**
     * Executes atomic multi-table checkout transaction:
     * 1. Creates SHIPPING record for selected address
     * 2. Creates ORDERS record
     * 3. Inserts each ORDER_ITEM (MySQL trigger verifies stock & reduces inventory)
     * 4. Creates PAYMENT record
     * 5. Inserts CREDIT_CARD or DIGITAL_WALLET record
     * 6. Clears customer's CART_ITEMs
     * 7. Commits transaction atomically (Rolls back if any step fails)
     */
    public Order checkout(int customerId, int cartId, int addressId,
                         String paymentType, String cardNumber, String cvv, LocalDate expiry,
                         String walletProvider, String walletRef) {

        List<CartItem> cartItems = getCartItems(cartId);
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Your shopping cart is empty.");
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Begin ACID Transaction

            // 1. Calculate total order amount
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (CartItem it : cartItems) {
                totalAmount = totalAmount.add(it.getSubtotal());
            }

            // 2. Create Shipping record
            int shipmentId;
            String shipSql = "INSERT INTO SHIPPING (address_id, carrier, tracking_no, status) VALUES (?, ?, ?, 'Pending')";
            try (PreparedStatement ps = conn.prepareStatement(shipSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, addressId);
                ps.setString(2, "Express Logistics");
                ps.setString(3, "TRK" + System.currentTimeMillis() % 1000000);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        shipmentId = keys.getInt(1);
                    } else {
                        throw new SQLException("Failed to generate shipment ID");
                    }
                }
            }

            // 3. Create Order record
            int orderId;
            String orderSql = "INSERT INTO ORDERS (customer_id, shipment_id, order_date, total_amount, status) VALUES (?, ?, ?, ?, 'Pending')";
            try (PreparedStatement ps = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, customerId);
                ps.setInt(2, shipmentId);
                ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                ps.setBigDecimal(4, totalAmount);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        orderId = keys.getInt(1);
                    } else {
                        throw new SQLException("Failed to generate order ID");
                    }
                }
            }

            // 4. Insert each order item (database trigger reduces stock)
            String itemSql = "INSERT INTO ORDER_ITEM (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(itemSql)) {
                for (CartItem it : cartItems) {
                    ps.setInt(1, orderId);
                    ps.setInt(2, it.getProductId());
                    ps.setInt(3, it.getQuantity());
                    ps.setBigDecimal(4, it.getUnitPrice());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // 5. Create Payment record
            int paymentId;
            String paySql = "INSERT INTO PAYMENT (order_id, amount, payment_date, payment_type, status) VALUES (?, ?, ?, ?, 'Completed')";
            try (PreparedStatement ps = conn.prepareStatement(paySql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, orderId);
                ps.setBigDecimal(2, totalAmount);
                ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                ps.setString(4, paymentType != null ? paymentType : "Credit Card");
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        paymentId = keys.getInt(1);
                    } else {
                        throw new SQLException("Failed to generate payment ID");
                    }
                }
            }

            // 6. Record payment subtype details
            if ("Credit Card".equalsIgnoreCase(paymentType)) {
                String ccSql = "INSERT INTO CREDIT_CARD (payment_id, card_number, cvv, expiry_date) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(ccSql)) {
                    ps.setInt(1, paymentId);
                    ps.setString(2, cardNumber != null ? cardNumber.replaceAll("\\s+", "") : "4111111111111001");
                    ps.setString(3, cvv != null ? cvv : "123");
                    ps.setDate(4, expiry != null ? Date.valueOf(expiry) : Date.valueOf(LocalDate.now().plusYears(3)));
                    ps.executeUpdate();
                }
            } else {
                String dwSql = "INSERT INTO DIGITAL_WALLET (payment_id, provider, wallet_ref) VALUES (?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(dwSql)) {
                    ps.setInt(1, paymentId);
                    ps.setString(2, walletProvider != null ? walletProvider : "UPI/Paytm");
                    ps.setString(3, walletRef != null ? walletRef : "REF" + System.currentTimeMillis() % 100000);
                    ps.executeUpdate();
                }
            }

            // 7. Clear items from cart
            String clearSql = "DELETE FROM CART_ITEM WHERE cart_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(clearSql)) {
                ps.setInt(1, cartId);
                ps.executeUpdate();
            }

            conn.commit(); // Atomic commit!
            logger.info("Order #{} placed successfully for customer #{}", orderId, customerId);

            Order placedOrder = new Order(orderId, customerId, shipmentId, LocalDateTime.now(), totalAmount, "Pending");
            return placedOrder;

        } catch (SQLException e) {
            DatabaseConnection.rollback(conn);
            logger.error("Checkout transaction failed", e);
            throw new DatabaseException("Checkout failed (Transaction rolled back): " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Get order history for a specific customer.
     */
    public List<Order> getCustomerOrders(int customerId) {
        String sql = "SELECT o.order_id, o.customer_id, c.name AS customer_name, " +
                     "o.shipment_id, s.tracking_no, o.order_date, o.total_amount, o.status " +
                     "FROM ORDERS o " +
                     "LEFT JOIN CUSTOMER c ON o.customer_id = c.customer_id " +
                     "LEFT JOIN SHIPPING s ON o.shipment_id = s.shipment_id " +
                     "WHERE o.customer_id = ? " +
                     "ORDER BY o.order_date DESC";
        List<Order> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
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
            logger.error("Error loading orders for customer {}", customerId, e);
        }
        return list;
    }

    /**
     * Get delivery addresses for a customer.
     */
    public List<Address> getCustomerAddresses(int customerId) {
        String sql = "SELECT address_id, customer_id, street, city, state, country, pincode FROM ADDRESS WHERE customer_id = ?";
        List<Address> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Address(
                            rs.getInt("address_id"),
                            rs.getInt("customer_id"),
                            rs.getString("street"),
                            rs.getString("city"),
                            rs.getString("state"),
                            rs.getString("country"),
                            rs.getString("pincode")
                    ));
                }
            }
        } catch (SQLException e) {
            logger.error("Error loading addresses for customer {}", customerId, e);
        }
        return list;
    }

    /**
     * Get reviews for a product.
     */
    public List<Review> getProductReviews(int productId) {
        String sql = "SELECT r.review_id, r.customer_id, c.name AS customer_name, " +
                     "r.product_id, p.name AS product_name, r.rating, r.comment " +
                     "FROM REVIEW r " +
                     "LEFT JOIN CUSTOMER c ON r.customer_id = c.customer_id " +
                     "LEFT JOIN PRODUCT p ON r.product_id = p.product_id " +
                     "WHERE r.product_id = ? ORDER BY r.review_id DESC";
        List<Review> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Review r = new Review(
                            rs.getInt("review_id"),
                            rs.getInt("customer_id"),
                            rs.getInt("product_id"),
                            rs.getInt("rating"),
                            rs.getString("comment")
                    );
                    r.setCustomerName(rs.getString("customer_name"));
                    r.setProductName(rs.getString("product_name"));
                    list.add(r);
                }
            }
        } catch (SQLException e) {
            logger.error("Error loading reviews for product {}", productId, e);
        }
        return list;
    }
}
