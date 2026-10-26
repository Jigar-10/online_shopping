package ui.customer;

import dao.CustomerDao;
import model.Customer;
import service.CustomerPortalService;
import ui.dialog.CustomerDialog;
import ui.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerPortalPanel extends JPanel {
    private final CustomerDao customerDao = new CustomerDao();
    private final CustomerPortalService portalService = new CustomerPortalService();
    private final Runnable onSwitchToAdmin;

    private JComboBox<Customer> customerSelector;
    private JButton cartBadgeBtn;
    private JTabbedPane tabbedPane;

    private ProductCatalogPanel catalogPanel;
    private CartPanel cartPanel;
    private MyOrdersPanel ordersPanel;
    private MyAddressesPanel addressesPanel;

    private Customer activeCustomer;
    private List<Customer> customerList = new ArrayList<>();

    public CustomerPortalPanel(Runnable onSwitchToAdmin) {
        this.onSwitchToAdmin = onSwitchToAdmin;
        initUI();
        loadCustomers();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_MAIN);

        // Top Storefront Header Bar
        JPanel headerBar = new JPanel(new BorderLayout(16, 0));
        headerBar.setBackground(UITheme.CARD_BG);
        headerBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_GRAY),
                new EmptyBorder(12, 24, 12, 24)
        ));

        // Left brand
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandPanel.setOpaque(false);

        JLabel logo = new JLabel("🛍 Online Shopping Store");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logo.setForeground(UITheme.PRIMARY);

        JLabel badge = new JLabel("Customer Portal");
        badge.setFont(UITheme.FONT_SMALL);
        badge.setForeground(UITheme.TEXT_MUTED);
        badge.setOpaque(true);
        badge.setBackground(UITheme.PRIMARY_LIGHT);
        badge.setBorder(new EmptyBorder(2, 6, 2, 6));

        brandPanel.add(logo);
        brandPanel.add(badge);
        headerBar.add(brandPanel, BorderLayout.WEST);

        // Right controls (Active customer selector, Cart button, Admin switch button)
        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightControls.setOpaque(false);

        JLabel custLbl = new JLabel("Shopping as:");
        custLbl.setFont(UITheme.FONT_BOLD);
        custLbl.setForeground(UITheme.TEXT_MUTED);

        customerSelector = new JComboBox<>();
        customerSelector.setPreferredSize(new Dimension(190, 32));
        customerSelector.addActionListener(e -> onCustomerChanged());

        JButton newCustBtn = new JButton("+ New");
        newCustBtn.setToolTipText("Register new customer profile");
        newCustBtn.setPreferredSize(new Dimension(70, 32));
        newCustBtn.addActionListener(e -> {
            CustomerDialog d = new CustomerDialog(SwingUtilities.getWindowAncestor(this), null);
            d.setVisible(true);
            if (d.isSaved()) loadCustomers();
        });

        cartBadgeBtn = new JButton("🛒 My Cart (0)");
        cartBadgeBtn.setFont(UITheme.FONT_BOLD);
        cartBadgeBtn.setBackground(UITheme.PRIMARY);
        cartBadgeBtn.setForeground(Color.WHITE);
        cartBadgeBtn.setPreferredSize(new Dimension(130, 32));
        cartBadgeBtn.addActionListener(e -> tabbedPane.setSelectedComponent(cartPanel));

        JButton switchAdminBtn = new JButton("🛡 Switch to Admin Studio");
        switchAdminBtn.setFont(UITheme.FONT_SMALL);
        switchAdminBtn.setPreferredSize(new Dimension(170, 32));
        switchAdminBtn.addActionListener(e -> {
            if (onSwitchToAdmin != null) onSwitchToAdmin.run();
        });

        rightControls.add(custLbl);
        rightControls.add(customerSelector);
        rightControls.add(newCustBtn);
        rightControls.add(cartBadgeBtn);
        rightControls.add(switchAdminBtn);
        headerBar.add(rightControls, BorderLayout.EAST);

        add(headerBar, BorderLayout.NORTH);

        // Navigation Tabs (Store Catalog, Cart & Checkout, My Orders, Saved Addresses)
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BOLD);
        tabbedPane.setBackground(UITheme.CARD_BG);

        catalogPanel = new ProductCatalogPanel(portalService, () -> activeCustomer, this::updateCartBadge);
        cartPanel = new CartPanel(portalService, () -> activeCustomer, () -> {
            updateCartBadge();
            ordersPanel.reload();
            catalogPanel.reload();
            tabbedPane.setSelectedComponent(ordersPanel);
        });
        ordersPanel = new MyOrdersPanel(portalService, () -> activeCustomer);
        addressesPanel = new MyAddressesPanel(portalService, () -> activeCustomer);

        tabbedPane.addTab("🏪 Store Catalog", catalogPanel);
        tabbedPane.addTab("🛒 My Cart & Checkout", cartPanel);
        tabbedPane.addTab("📦 My Orders & Tracking", ordersPanel);
        tabbedPane.addTab("📍 Saved Addresses", addressesPanel);

        tabbedPane.addChangeListener(e -> {
            Component selected = tabbedPane.getSelectedComponent();
            if (selected == cartPanel) cartPanel.reload();
            else if (selected == ordersPanel) ordersPanel.reload();
            else if (selected == addressesPanel) addressesPanel.reload();
            else if (selected == catalogPanel) catalogPanel.reload();
        });

        add(tabbedPane, BorderLayout.CENTER);
    }

    public void loadCustomers() {
        customerSelector.removeAllItems();
        try {
            customerList = customerDao.findAll();
            for (Customer c : customerList) {
                customerSelector.addItem(c);
            }
            if (!customerList.isEmpty()) {
                customerSelector.setSelectedIndex(0);
                activeCustomer = customerList.get(0);
            }
        } catch (Exception ignored) {}
        updateCartBadge();
    }

    private void onCustomerChanged() {
        activeCustomer = (Customer) customerSelector.getSelectedItem();
        updateCartBadge();
        Component selected = tabbedPane.getSelectedComponent();
        if (selected == cartPanel) cartPanel.reload();
        else if (selected == ordersPanel) ordersPanel.reload();
        else if (selected == addressesPanel) addressesPanel.reload();
    }

    public void updateCartBadge() {
        if (activeCustomer == null) {
            cartBadgeBtn.setText("🛒 My Cart (0)");
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                var cart = portalService.getOrCreateCart(activeCustomer.getCustomerId());
                var items = portalService.getCartItems(cart.getCartId());
                int count = 0;
                for (var it : items) count += it.getQuantity();
                cartBadgeBtn.setText("🛒 My Cart (" + count + ")");
            } catch (Exception e) {
                cartBadgeBtn.setText("🛒 My Cart (0)");
            }
        });
    }

    public void reload() {
        loadCustomers();
        catalogPanel.reload();
    }
}
