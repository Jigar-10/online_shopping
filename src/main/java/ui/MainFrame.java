package ui;

import config.DatabaseConnection;
import dao.*;
import model.*;
import ui.dialog.*;
import ui.util.NotificationUtils;
import ui.util.UITheme;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class MainFrame extends JFrame {
    private final SidebarPanel sidebar;
    private final TopBarPanel topBar;
    private final JPanel contentArea;
    private final CardLayout cardLayout;
    private final Map<String, JComponent> screens = new HashMap<>();

    // DAOs
    private final CustomerDao customerDao = new CustomerDao();
    private final AddressDao addressDao = new AddressDao();
    private final ShippingDao shippingDao = new ShippingDao();
    private final CategoryDao categoryDao = new CategoryDao();
    private final SupplierDao supplierDao = new SupplierDao();
    private final ProductDao productDao = new ProductDao();
    private final CartDao cartDao = new CartDao();
    private final CartItemDao cartItemDao = new CartItemDao();
    private final OrderDao orderDao = new OrderDao();
    private final OrderItemDao orderItemDao = new OrderItemDao();
    private final PaymentDao paymentDao = new PaymentDao();
    private final CreditCardDao creditCardDao = new CreditCardDao();
    private final DigitalWalletDao digitalWalletDao = new DigitalWalletDao();
    private final ReviewDao reviewDao = new ReviewDao();

    private String currentScreen = "dashboard";

    public MainFrame() {
        super("Online Shopping DBMS Desktop Studio - MySQL 8.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 820);
        setMinimumSize(new Dimension(1024, 700));
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        contentArea = new JPanel(cardLayout);

        topBar = new TopBarPanel(
                this::openDatabaseConfig,
                UITheme::toggleTheme,
                this::refreshCurrentScreen
        );

        sidebar = new SidebarPanel(this::navigateTo);

        initScreens();

        setLayout(new BorderLayout());
        add(sidebar, BorderLayout.WEST);

        JPanel mainCenter = new JPanel(new BorderLayout());
        mainCenter.add(topBar, BorderLayout.NORTH);
        mainCenter.add(contentArea, BorderLayout.CENTER);
        add(mainCenter, BorderLayout.CENTER);

        navigateTo("dashboard");

        // Async test connection on startup
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                DatabaseConnection.testConnection();
                return null;
            }
        }.execute();
    }

    private void initScreens() {
        // 1. Dashboard
        DashboardPanel dashboard = new DashboardPanel(
                () -> navigateTo("customers"),
                () -> navigateTo("products"),
                () -> navigateTo("orders"),
                this::openDatabaseConfig
        );
        addScreen("dashboard", dashboard);

        // 2. Customers
        GenericTablePanel<Customer> customersPanel = new GenericTablePanel<>(
                "Customer Records",
                new GenericTablePanel.DataLoader<>() {
                    @Override public java.util.List<Customer> loadData(String query) { return customerDao.search(query); }
                    @Override public Vector<String> getColumnNames() {
                        return new Vector<>(java.util.List.of("ID", "Full Name", "Email Address", "Phone", "Registered On", "Gender"));
                    }
                    @Override public Vector<Object> toRowData(Customer c) {
                        return new Vector<>(java.util.List.of(c.getCustomerId(), c.getName(), c.getEmail(),
                                c.getPhone() != null ? c.getPhone() : "-", c.getRegisteredOn() != null ? c.getRegisteredOn() : "-", c.getGender() != null ? c.getGender() : "-"));
                    }
                },
                (item, panel) -> {
                    CustomerDialog d = new CustomerDialog(this, null);
                    d.setVisible(true);
                    if (d.isSaved()) panel.reload();
                },
                (item, panel) -> {
                    CustomerDialog d = new CustomerDialog(this, item);
                    d.setVisible(true);
                    if (d.isSaved()) panel.reload();
                },
                (item, panel) -> {
                    if (NotificationUtils.confirm(this, "Delete Customer", "Delete customer '" + item.getName() + "' (ID " + item.getCustomerId() + ")?")) {
                        try {
                            customerDao.delete(item.getCustomerId());
                            NotificationUtils.showSuccess(this, "Customer deleted successfully.");
                            panel.reload();
                        } catch (Exception ex) {
                            NotificationUtils.showError(this, ex.getMessage(), ex);
                        }
                    }
                }
        );
        addScreen("customers", customersPanel);

        // 3. Addresses
        GenericTablePanel<Address> addressPanel = new GenericTablePanel<>(
                "Customer Delivery Addresses",
                new GenericTablePanel.DataLoader<>() {
                    @Override public java.util.List<Address> loadData(String q) { return addressDao.search(q); }
                    @Override public Vector<String> getColumnNames() {
                        return new Vector<>(java.util.List.of("Address ID", "Cust ID", "Customer Name", "Street", "City", "State", "Country", "Pincode"));
                    }
                    @Override public Vector<Object> toRowData(Address a) {
                        return new Vector<>(java.util.List.of(a.getAddressId(), a.getCustomerId(),
                                a.getCustomerName() != null ? a.getCustomerName() : "-",
                                a.getStreet(), a.getCity(), a.getState(), a.getCountry(), a.getPincode()));
                    }
                },
                (item, panel) -> {
                    AddressDialog d = new AddressDialog(this, null);
                    d.setVisible(true);
                    if (d.isSaved()) panel.reload();
                },
                (item, panel) -> {
                    AddressDialog d = new AddressDialog(this, item);
                    d.setVisible(true);
                    if (d.isSaved()) panel.reload();
                },
                (item, panel) -> {
                    if (NotificationUtils.confirm(this, "Delete Address", "Delete Address #" + item.getAddressId() + "?")) {
                        try {
                            addressDao.delete(item.getAddressId());
                            NotificationUtils.showSuccess(this, "Address deleted.");
                            panel.reload();
                        } catch (Exception ex) {
                            NotificationUtils.showError(this, ex.getMessage(), ex);
                        }
                    }
                }
        );
        addScreen("addresses", addressPanel);

        // 4. Shipping
        GenericTablePanel<Shipping> shippingPanel = new GenericTablePanel<>(
                "Shipments & Tracking",
                new GenericTablePanel.DataLoader<>() {
                    @Override public java.util.List<Shipping> loadData(String q) { return shippingDao.search(q); }
                    @Override public Vector<String> getColumnNames() {
                        return new Vector<>(java.util.List.of("Shipment ID", "Address ID", "Delivery Details", "Carrier", "Tracking No", "Status"));
                    }
                    @Override public Vector<Object> toRowData(Shipping s) {
                        return new Vector<>(java.util.List.of(s.getShipmentId(), s.getAddressId(),
                                s.getAddressSummary() != null ? s.getAddressSummary() : "-",
                                s.getCarrier() != null ? s.getCarrier() : "-",
                                s.getTrackingNo() != null ? s.getTrackingNo() : "-", s.getStatus()));
                    }
                },
                (item, panel) -> {
                    ShippingDialog d = new ShippingDialog(this, null);
                    d.setVisible(true);
                    if (d.isSaved()) panel.reload();
                },
                (item, panel) -> {
                    ShippingDialog d = new ShippingDialog(this, item);
                    d.setVisible(true);
                    if (d.isSaved()) panel.reload();
                },
                (item, panel) -> {
                    if (NotificationUtils.confirm(this, "Delete Shipment", "Delete shipment #" + item.getShipmentId() + "?")) {
                        try {
                            shippingDao.delete(item.getShipmentId());
                            NotificationUtils.showSuccess(this, "Shipment deleted.");
                            panel.reload();
                        } catch (Exception ex) {
                            NotificationUtils.showError(this, ex.getMessage(), ex);
                        }
                    }
                }
        );
        addScreen("shipping", shippingPanel);

        // 5. Categories
        GenericTablePanel<Category> categoryPanel = new GenericTablePanel<>(
                "Product Categories",
                new GenericTablePanel.DataLoader<>() {
                    @Override public java.util.List<Category> loadData(String q) { return categoryDao.search(q); }
                    @Override public Vector<String> getColumnNames() {
                        return new Vector<>(java.util.List.of("Category ID", "Category Name"));
                    }
                    @Override public Vector<Object> toRowData(Category c) {
                        return new Vector<>(java.util.List.of(c.getCategoryId(), c.getName()));
                    }
                },
                (item, panel) -> {
                    CategoryDialog d = new CategoryDialog(this, null);
                    d.setVisible(true);
                    if (d.isSaved()) panel.reload();
                },
                (item, panel) -> {
                    CategoryDialog d = new CategoryDialog(this, item);
                    d.setVisible(true);
                    if (d.isSaved()) panel.reload();
                },
                (item, panel) -> {
                    if (NotificationUtils.confirm(this, "Delete Category", "Delete category '" + item.getName() + "'?")) {
                        try {
                            categoryDao.delete(item.getCategoryId());
                            NotificationUtils.showSuccess(this, "Category deleted.");
                            panel.reload();
                        } catch (Exception ex) {
                            NotificationUtils.showError(this, ex.getMessage(), ex);
                        }
                    }
                }
        );
        addScreen("categories", categoryPanel);

        // 6. Suppliers
        GenericTablePanel<Supplier> supplierPanel = new GenericTablePanel<>(
                "Product Suppliers",
                new GenericTablePanel.DataLoader<>() {
                    @Override public java.util.List<Supplier> loadData(String q) { return supplierDao.search(q); }
                    @Override public Vector<String> getColumnNames() {
                        return new Vector<>(java.util.List.of("Supplier ID", "Supplier Name", "Contact Email", "Phone"));
                    }
                    @Override public Vector<Object> toRowData(Supplier s) {
                        return new Vector<>(java.util.List.of(s.getSupplierId(), s.getName(),
                                s.getContactEmail() != null ? s.getContactEmail() : "-",
                                s.getPhone() != null ? s.getPhone() : "-"));
                    }
                },
                (item, panel) -> {
                    SupplierDialog d = new SupplierDialog(this, null);
                    d.setVisible(true);
                    if (d.isSaved()) panel.reload();
                },
                (item, panel) -> {
                    SupplierDialog d = new SupplierDialog(this, item);
                    d.setVisible(true);
                    if (d.isSaved()) panel.reload();
                },
                (item, panel) -> {
                    if (NotificationUtils.confirm(this, "Delete Supplier", "Delete supplier '" + item.getName() + "'?")) {
                        try {
                            supplierDao.delete(item.getSupplierId());
                            NotificationUtils.showSuccess(this, "Supplier deleted.");
                            panel.reload();
                        } catch (Exception ex) {
                            NotificationUtils.showError(this, ex.getMessage(), ex);
                        }
                    }
                }
        );
        addScreen("suppliers", supplierPanel);

        // 7. Products
        GenericTablePanel<Product> productPanel = new GenericTablePanel<>(
                "Product Catalog & Inventory",
                new GenericTablePanel.DataLoader<>() {
                    @Override public java.util.List<Product> loadData(String q) { return productDao.search(q); }
                    @Override public Vector<String> getColumnNames() {
                        return new Vector<>(java.util.List.of("Product ID", "Product Name", "Category", "Supplier", "Stock Qty", "Price (\u20B9)", "Description"));
                    }
                    @Override public Vector<Object> toRowData(Product p) {
                        return new Vector<>(java.util.List.of(p.getProductId(), p.getName(),
                                p.getCategoryName() != null ? p.getCategoryName() : "ID " + p.getCategoryId(),
                                p.getSupplierName() != null ? p.getSupplierName() : "ID " + p.getSupplierId(),
                                p.getStockQty(), p.getPrice(), p.getDescription() != null ? p.getDescription() : "-"));
                    }
                },
                (item, panel) -> {
                    ProductDialog d = new ProductDialog(this, null);
                    d.setVisible(true);
                    if (d.isSaved()) panel.reload();
                },
                (item, panel) -> {
                    ProductDialog d = new ProductDialog(this, item);
                    d.setVisible(true);
                    if (d.isSaved()) panel.reload();
                },
                (item, panel) -> {
                    if (NotificationUtils.confirm(this, "Delete Product", "Delete product '" + item.getName() + "'?")) {
                        try {
                            productDao.delete(item.getProductId());
                            NotificationUtils.showSuccess(this, "Product deleted.");
                            panel.reload();
                        } catch (Exception ex) {
                            NotificationUtils.showError(this, ex.getMessage(), ex);
                        }
                    }
                }
        );
        addScreen("products", productPanel);

        // 8. Shopping Carts
        GenericTablePanel<Cart> cartPanel = new GenericTablePanel<>(
                "Customer Shopping Carts",
                new GenericTablePanel.DataLoader<>() {
                    @Override public java.util.List<Cart> loadData(String q) { return cartDao.search(q); }
                    @Override public Vector<String> getColumnNames() {
                        return new Vector<>(java.util.List.of("Cart ID", "Customer ID", "Customer Name", "Created On"));
                    }
                    @Override public Vector<Object> toRowData(Cart c) {
                        return new Vector<>(java.util.List.of(c.getCartId(), c.getCustomerId(),
                                c.getCustomerName() != null ? c.getCustomerName() : "-",
                                c.getCreatedOn() != null ? c.getCreatedOn().toString().replace("T", " ") : "-"));
                    }
                },
                (item, panel) -> {
                    CartDialog d = new CartDialog(this, null);
                    d.setVisible(true);
                    if (d.isSaved()) panel.reload();
                },
                (item, panel) -> {
                    CartDialog d = new CartDialog(this, item);
                    d.setVisible(true);
                    if (d.isSaved()) panel.reload();
                },
                (item, panel) -> {
                    if (NotificationUtils.confirm(this, "Delete Cart", "Delete shopping cart #" + item.getCartId() + "?")) {
                        try {
                            cartDao.delete(item.getCartId());
                            NotificationUtils.showSuccess(this, "Cart deleted.");
                            panel.reload();
                        } catch (Exception ex) {
                            NotificationUtils.showError(this, ex.getMessage(), ex);
                        }
                    }
                }
        );
        addScreen("carts", cartPanel);

        // 9. Cart Items
        GenericTablePanel<CartItem> cartItemPanel = new GenericTablePanel<>(
                "Cart Line Items (Composite Key)",
                new GenericTablePanel.DataLoader<>() {
                    @Override public java.util.List<CartItem> loadData(String q) { return cartItemDao.search(q); }
                    @Override public Vector<String> getColumnNames() {
                        return new Vector<>(java.util.List.of("Cart ID", "Product ID", "Product Name", "Quantity", "Unit Price (\u20B9)", "Subtotal (\u20B9)"));
                    }
                    @Override public Vector<Object> toRowData(CartItem ci) {
                        return new Vector<>(java.util.List.of(ci.getCartId(), ci.getProductId(),
                                ci.getProductName() != null ? ci.getProductName() : "-",
                                ci.getQuantity(), ci.getUnitPrice() != null ? ci.getUnitPrice() : "-",
                                ci.getSubtotal()));
                    }
                },
                (item, panel) -> {
                    CartItemDialog d = new CartItemDialog(this, null);
                    d.setVisible(true);
                    if (d.isSaved()) panel.reload();
                },
                (item, panel) -> {
                    CartItemDialog d = new CartItemDialog(this, item);
                    d.setVisible(true);
                    if (d.isSaved()) panel.reload();
                },
                (item, panel) -> {
                    if (NotificationUtils.confirm(this, "Delete Cart Item", "Remove item from Cart #" + item.getCartId() + "?")) {
                        try {
                            cartItemDao.delete(item.getCartId(), item.getProductId());
                            NotificationUtils.showSuccess(this, "Cart item removed.");
                            panel.reload();
                        } catch (Exception ex) {
                            NotificationUtils.showError(this, ex.getMessage(), ex);
                        }
                    }
                }
        );
        addScreen("cart_items", cartItemPanel);

        // 10. Orders
        GenericTablePanel<Order> orderPanel = new GenericTablePanel<>(
                "Customer Orders",
                new GenericTablePanel.DataLoader<>() {
                    @Override public java.util.List<Order> loadData(String q) { return orderDao.search(q); }
                    @Override public Vector<String> getColumnNames() {
                        return new Vector<>(java.util.List.of("Order ID", "Customer ID", "Customer Name", "Shipment", "Order Date", "Total (\u20B9)", "Status"));
                    }
                    @Override public Vector<Object> toRowData(Order o) {
                        return new Vector<>(java.util.List.of(o.getOrderId(), o.getCustomerId(),
                                o.getCustomerName() != null ? o.getCustomerName() : "-",
                                o.getShipmentId() != null ? "#" + o.getShipmentId() + " (" + o.getTrackingNo() + ")" : "Pending",
                                o.getOrderDate() != null ? o.getOrderDate().toString().replace("T", " ") : "-",
                                o.getTotalAmount(), o.getStatus()));
                    }
                },
                (item, panel) -> {
                    OrderDialog d = new OrderDialog(this, null);
                    d.setVisible(true);
                    if (d.isSaved()) panel.reload();
                },
                (item, panel) -> {
                    OrderDialog d = new OrderDialog(this, item);
                    d.setVisible(true);
                    if (d.isSaved()) panel.reload();
                },
                (item, panel) -> {
                    if (NotificationUtils.confirm(this, "Delete Order", "Delete Order #" + item.getOrderId() + "?")) {
                        try {
                            orderDao.delete(item.getOrderId());
                            NotificationUtils.showSuccess(this, "Order deleted.");
                            panel.reload();
                        } catch (Exception ex) {
                            NotificationUtils.showError(this, ex.getMessage(), ex);
                        }
                    }
                }
        );
        addScreen("orders", orderPanel);

        // 11. Order Items
        GenericTablePanel<OrderItem> orderItemPanel = new GenericTablePanel<>(
                "Order Line Items (Composite Key)",
                new GenericTablePanel.DataLoader<>() {
                    @Override public java.util.List<OrderItem> loadData(String q) { return orderItemDao.search(q); }
                    @Override public Vector<String> getColumnNames() {
                        return new Vector<>(java.util.List.of("Order ID", "Product ID", "Product Name", "Quantity", "Unit Price (\u20B9)", "Subtotal (\u20B9)"));
                    }
                    @Override public Vector<Object> toRowData(OrderItem oi) {
                        return new Vector<>(java.util.List.of(oi.getOrderId(), oi.getProductId(),
                                oi.getProductName() != null ? oi.getProductName() : "-",
                                oi.getQuantity(), oi.getUnitPrice(), oi.getSubtotal()));
                    }
                },
                (item, panel) -> {
                    OrderItemDialog d = new OrderItemDialog(this, null);
                    d.setVisible(true);
                    if (d.isSaved()) panel.reload();
                },
                (item, panel) -> {
                    OrderItemDialog d = new OrderItemDialog(this, item);
                    d.setVisible(true);
                    if (d.isSaved()) panel.reload();
                },
                (item, panel) -> {
                    if (NotificationUtils.confirm(this, "Delete Order Item", "Remove item from Order #" + item.getOrderId() + "?")) {
                        try {
                            orderItemDao.delete(item.getOrderId(), item.getProductId());
                            NotificationUtils.showSuccess(this, "Order item removed.");
                            panel.reload();
                        } catch (Exception ex) {
                            NotificationUtils.showError(this, ex.getMessage(), ex);
                        }
                    }
                }
        );
        addScreen("order_items", orderItemPanel);

        // 12. Payments
        GenericTablePanel<Payment> paymentPanel = new GenericTablePanel<>(
                "Order Payments",
                new GenericTablePanel.DataLoader<>() {
                    @Override public java.util.List<Payment> loadData(String q) { return paymentDao.search(q); }
                    @Override public Vector<String> getColumnNames() {
                        return new Vector<>(java.util.List.of("Payment ID", "Order ID", "Amount (\u20B9)", "Payment Date", "Payment Method", "Status"));
                    }
                    @Override public Vector<Object> toRowData(Payment p) {
                        return new Vector<>(java.util.List.of(p.getPaymentId(), p.getOrderId(), p.getAmount(),
                                p.getPaymentDate() != null ? p.getPaymentDate().toString().replace("T", " ") : "-",
                                p.getPaymentType(), p.getStatus()));
                    }
                },
                (item, panel) -> {
                    PaymentDialog d = new PaymentDialog(this, null);
                    d.setVisible(true);
                    if (d.isSaved()) panel.reload();
                },
                (item, panel) -> {
                    PaymentDialog d = new PaymentDialog(this, item);
                    d.setVisible(true);
                    if (d.isSaved()) panel.reload();
                },
                (item, panel) -> {
                    if (NotificationUtils.confirm(this, "Delete Payment", "Delete Payment #" + item.getPaymentId() + "?")) {
                        try {
                            paymentDao.delete(item.getPaymentId());
                            NotificationUtils.showSuccess(this, "Payment deleted.");
                            panel.reload();
                        } catch (Exception ex) {
                            NotificationUtils.showError(this, ex.getMessage(), ex);
                        }
                    }
                }
        );
        addScreen("payments", paymentPanel);

        // 13. Credit Cards
        GenericTablePanel<CreditCard> cardPanel = new GenericTablePanel<>(
                "Credit Card Details (Payment Subtype)",
                new GenericTablePanel.DataLoader<>() {
                    @Override public java.util.List<CreditCard> loadData(String q) { return creditCardDao.search(q); }
                    @Override public Vector<String> getColumnNames() {
                        return new Vector<>(java.util.List.of("Payment ID", "Card Number", "CVV", "Expiry Date"));
                    }
                    @Override public Vector<Object> toRowData(CreditCard c) {
                        return new Vector<>(java.util.List.of(c.getPaymentId(), c.getMaskedCardNumber(), "***", c.getExpiryDate() != null ? c.getExpiryDate() : "-"));
                    }
                },
                (item, panel) -> {
                    CreditCardDialog d = new CreditCardDialog(this, null);
                    d.setVisible(true);
                    if (d.isSaved()) panel.reload();
                },
                (item, panel) -> {
                    CreditCardDialog d = new CreditCardDialog(this, item);
                    d.setVisible(true);
                    if (d.isSaved()) panel.reload();
                },
                (item, panel) -> {
                    if (NotificationUtils.confirm(this, "Delete Credit Card", "Delete card details for Payment #" + item.getPaymentId() + "?")) {
                        try {
                            creditCardDao.delete(item.getPaymentId());
                            NotificationUtils.showSuccess(this, "Credit card record deleted.");
                            panel.reload();
                        } catch (Exception ex) {
                            NotificationUtils.showError(this, ex.getMessage(), ex);
                        }
                    }
                }
        );
        addScreen("credit_cards", cardPanel);

        // 14. Digital Wallets
        GenericTablePanel<DigitalWallet> walletPanel = new GenericTablePanel<>(
                "Digital Wallet Details (Payment Subtype)",
                new GenericTablePanel.DataLoader<>() {
                    @Override public java.util.List<DigitalWallet> loadData(String q) { return digitalWalletDao.search(q); }
                    @Override public Vector<String> getColumnNames() {
                        return new Vector<>(java.util.List.of("Payment ID", "Provider", "Wallet Reference ID"));
                    }
                    @Override public Vector<Object> toRowData(DigitalWallet w) {
                        return new Vector<>(java.util.List.of(w.getPaymentId(), w.getProvider(), w.getWalletRef()));
                    }
                },
                (item, panel) -> {
                    DigitalWalletDialog d = new DigitalWalletDialog(this, null);
                    d.setVisible(true);
                    if (d.isSaved()) panel.reload();
                },
                (item, panel) -> {
                    DigitalWalletDialog d = new DigitalWalletDialog(this, item);
                    d.setVisible(true);
                    if (d.isSaved()) panel.reload();
                },
                (item, panel) -> {
                    if (NotificationUtils.confirm(this, "Delete Wallet", "Delete wallet details for Payment #" + item.getPaymentId() + "?")) {
                        try {
                            digitalWalletDao.delete(item.getPaymentId());
                            NotificationUtils.showSuccess(this, "Digital wallet record deleted.");
                            panel.reload();
                        } catch (Exception ex) {
                            NotificationUtils.showError(this, ex.getMessage(), ex);
                        }
                    }
                }
        );
        addScreen("digital_wallets", walletPanel);

        // 15. Reviews
        GenericTablePanel<Review> reviewPanel = new GenericTablePanel<>(
                "Customer Product Reviews",
                new GenericTablePanel.DataLoader<>() {
                    @Override public java.util.List<Review> loadData(String q) { return reviewDao.search(q); }
                    @Override public Vector<String> getColumnNames() {
                        return new Vector<>(java.util.List.of("Review ID", "Customer", "Product", "Rating", "Comment"));
                    }
                    @Override public Vector<Object> toRowData(Review r) {
                        return new Vector<>(java.util.List.of(r.getReviewId(),
                                r.getCustomerName() != null ? r.getCustomerName() : "ID " + r.getCustomerId(),
                                r.getProductName() != null ? r.getProductName() : "ID " + r.getProductId(),
                                r.getStars(), r.getComment() != null ? r.getComment() : "-"));
                    }
                },
                (item, panel) -> {
                    ReviewDialog d = new ReviewDialog(this, null);
                    d.setVisible(true);
                    if (d.isSaved()) panel.reload();
                },
                (item, panel) -> {
                    ReviewDialog d = new ReviewDialog(this, item);
                    d.setVisible(true);
                    if (d.isSaved()) panel.reload();
                },
                (item, panel) -> {
                    if (NotificationUtils.confirm(this, "Delete Review", "Delete Review #" + item.getReviewId() + "?")) {
                        try {
                            reviewDao.delete(item.getReviewId());
                            NotificationUtils.showSuccess(this, "Review deleted.");
                            panel.reload();
                        } catch (Exception ex) {
                            NotificationUtils.showError(this, ex.getMessage(), ex);
                        }
                    }
                }
        );
        addScreen("reviews", reviewPanel);

        // 16. Views Panel
        ViewsPanel viewsPanel = new ViewsPanel();
        addScreen("views", viewsPanel);
    }

    private void addScreen(String id, JComponent component) {
        screens.put(id, component);
        contentArea.add(component, id);
    }

    public void navigateTo(String id) {
        if ("connection".equals(id)) {
            openDatabaseConfig();
            sidebar.setActive(currentScreen);
            return;
        } else if ("about".equals(id)) {
            AboutDialog d = new AboutDialog(this);
            d.setVisible(true);
            sidebar.setActive(currentScreen);
            return;
        }

        if (screens.containsKey(id)) {
            currentScreen = id;
            cardLayout.show(contentArea, id);
            sidebar.setActive(id);

            String title = switch (id) {
                case "dashboard" -> "Dashboard Overview";
                case "customers" -> "Customers Management";
                case "addresses" -> "Addresses Management";
                case "shipping" -> "Shipments & Tracking";
                case "categories" -> "Product Categories";
                case "suppliers" -> "Suppliers Management";
                case "products" -> "Products Inventory";
                case "carts" -> "Shopping Carts";
                case "cart_items" -> "Cart Items";
                case "orders" -> "Orders Management";
                case "order_items" -> "Order Line Items";
                case "payments" -> "Payments Management";
                case "credit_cards" -> "Credit Card Records";
                case "digital_wallets" -> "Digital Wallet Records";
                case "reviews" -> "Product Reviews";
                case "views" -> "Database Views (Experiment 7)";
                default -> id.toUpperCase();
            };
            topBar.setTitle(title);
        }
    }

    private void refreshCurrentScreen() {
        JComponent comp = screens.get(currentScreen);
        if (comp instanceof DashboardPanel d) {
            d.reload();
        } else if (comp instanceof GenericTablePanel<?> p) {
            p.reload();
        } else if (comp instanceof ViewsPanel v) {
            v.reload();
        }
    }

    private void openDatabaseConfig() {
        DatabaseConnectionDialog dialog = new DatabaseConnectionDialog(this);
        dialog.setVisible(true);
        refreshCurrentScreen();
    }
}
