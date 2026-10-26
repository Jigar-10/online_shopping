package ui.customer;

import dao.AddressDao;
import model.Address;
import model.Cart;
import model.CartItem;
import model.ComboItem;
import model.Customer;
import model.Order;
import service.CustomerPortalService;
import ui.dialog.AddressDialog;
import ui.util.NotificationUtils;
import ui.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CartPanel extends JPanel {
    private final CustomerPortalService portalService;
    private final java.util.function.Supplier<Customer> customerSupplier;
    private final Runnable onOrderPlaced;

    private final AddressDao addressDao = new AddressDao();

    private DefaultTableModel tableModel;
    private JTable cartTable;
    private JLabel totalAmountLabel;
    private JComboBox<ComboItem<Integer>> addressCombo;
    private JComboBox<String> paymentTypeCombo;

    // Payment fields
    private JPanel cardFieldsPanel;
    private JPanel walletFieldsPanel;
    private JTextField cardNumberField;
    private JTextField cvvField;
    private JTextField expiryDateField;
    private JComboBox<String> walletProviderCombo;
    private JTextField walletRefField;

    private JButton checkoutBtn;
    private List<CartItem> currentCartItems = new ArrayList<>();
    private Cart currentCart;

    public CartPanel(CustomerPortalService portalService,
                     java.util.function.Supplier<Customer> customerSupplier,
                     Runnable onOrderPlaced) {
        this.portalService = portalService;
        this.customerSupplier = customerSupplier;
        this.onOrderPlaced = onOrderPlaced;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(16, 0));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        // Left Panel: Items Table
        JPanel leftPanel = new JPanel(new BorderLayout(0, 10));
        leftPanel.setOpaque(false);

        JPanel leftHeader = new JPanel(new BorderLayout());
        leftHeader.setOpaque(false);
        JLabel title = new JLabel("Shopping Cart Items");
        title.setFont(UITheme.FONT_TITLE);
        leftHeader.add(title, BorderLayout.WEST);

        JButton clearBtn = new JButton("Clear Cart");
        clearBtn.setFont(UITheme.FONT_SMALL);
        clearBtn.addActionListener(e -> {
            if (currentCart != null && NotificationUtils.confirm(this, "Clear Cart", "Remove all items from your cart?")) {
                portalService.clearCart(currentCart.getCartId());
                reload();
            }
        });
        leftHeader.add(clearBtn, BorderLayout.EAST);
        leftPanel.add(leftHeader, BorderLayout.NORTH);

        String[] cols = {"Product Name", "Unit Price", "Quantity", "Subtotal (\u20B9)", "Action"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        cartTable = new JTable(tableModel);
        cartTable.setRowHeight(36);
        cartTable.getTableHeader().setFont(UITheme.FONT_BOLD);
        cartTable.setFont(UITheme.FONT_REGULAR);

        JScrollPane tableScroll = new JScrollPane(cartTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_GRAY, 1));
        leftPanel.add(tableScroll, BorderLayout.CENTER);

        // Cart Actions below table
        JPanel itemActionsBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        itemActionsBar.setOpaque(false);

        JButton incBtn = new JButton("+ Increase Qty");
        incBtn.addActionListener(e -> adjustSelectedQuantity(1));

        JButton decBtn = new JButton("- Decrease Qty");
        decBtn.addActionListener(e -> adjustSelectedQuantity(-1));

        JButton removeBtn = new JButton("🗑 Remove Item");
        removeBtn.addActionListener(e -> removeSelectedItem());

        itemActionsBar.add(incBtn);
        itemActionsBar.add(decBtn);
        itemActionsBar.add(removeBtn);
        leftPanel.add(itemActionsBar, BorderLayout.SOUTH);

        // Right Panel: Checkout Box
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setPreferredSize(new Dimension(360, 600));
        rightPanel.setBackground(UITheme.CARD_BG);
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_GRAY, 1),
                new EmptyBorder(16, 18, 16, 18)
        ));

        JLabel summaryTitle = new JLabel("Order Summary & Checkout");
        summaryTitle.setFont(UITheme.FONT_HEADER);
        rightPanel.add(summaryTitle);
        rightPanel.add(Box.createVerticalStrut(14));

        // Delivery Address selector
        JLabel addrLabel = new JLabel("Delivery Address *:");
        addrLabel.setFont(UITheme.FONT_BOLD);
        addressCombo = new JComboBox<>();

        JPanel addrBar = new JPanel(new BorderLayout(6, 0));
        addrBar.setOpaque(false);
        addrBar.add(addressCombo, BorderLayout.CENTER);

        JButton newAddrBtn = new JButton("+");
        newAddrBtn.setToolTipText("Add a new address");
        newAddrBtn.addActionListener(e -> {
            Customer cust = customerSupplier.get();
            if (cust == null) return;
            Address newAddr = new Address();
            newAddr.setCustomerId(cust.getCustomerId());
            AddressDialog d = new AddressDialog(SwingUtilities.getWindowAncestor(this), newAddr);
            d.setVisible(true);
            if (d.isSaved()) loadAddresses();
        });
        addrBar.add(newAddrBtn, BorderLayout.EAST);

        rightPanel.add(addrLabel);
        rightPanel.add(Box.createVerticalStrut(4));
        rightPanel.add(addrBar);
        rightPanel.add(Box.createVerticalStrut(14));

        // Payment Method Selector
        JLabel payLabel = new JLabel("Payment Method *:");
        payLabel.setFont(UITheme.FONT_BOLD);
        paymentTypeCombo = new JComboBox<>(new String[]{"Credit Card", "Digital Wallet"});
        paymentTypeCombo.addActionListener(e -> togglePaymentFields());

        rightPanel.add(payLabel);
        rightPanel.add(Box.createVerticalStrut(4));
        rightPanel.add(paymentTypeCombo);
        rightPanel.add(Box.createVerticalStrut(10));

        // Credit Card Sub-panel
        cardFieldsPanel = new JPanel(new GridLayout(3, 2, 6, 6));
        cardFieldsPanel.setOpaque(false);
        cardNumberField = new JTextField("4111 1111 1111 1001", 16);
        cvvField = new JTextField("123", 4);
        expiryDateField = new JTextField(LocalDate.now().plusYears(3).toString(), 10);

        cardFieldsPanel.add(new JLabel("Card Number:"));
        cardFieldsPanel.add(cardNumberField);
        cardFieldsPanel.add(new JLabel("CVV:"));
        cardFieldsPanel.add(cvvField);
        cardFieldsPanel.add(new JLabel("Expiry Date:"));
        cardFieldsPanel.add(expiryDateField);

        // Digital Wallet Sub-panel
        walletFieldsPanel = new JPanel(new GridLayout(2, 2, 6, 6));
        walletFieldsPanel.setOpaque(false);
        walletProviderCombo = new JComboBox<>(new String[]{"Paytm", "PhonePe", "Google Pay", "Amazon Pay"});
        walletRefField = new JTextField("WALLET" + (System.currentTimeMillis() % 10000), 14);

        walletFieldsPanel.add(new JLabel("Provider:"));
        walletFieldsPanel.add(walletProviderCombo);
        walletFieldsPanel.add(new JLabel("Wallet Ref:"));
        walletFieldsPanel.add(walletRefField);
        walletFieldsPanel.setVisible(false);

        rightPanel.add(cardFieldsPanel);
        rightPanel.add(walletFieldsPanel);
        rightPanel.add(Box.createVerticalStrut(16));
        rightPanel.add(new JSeparator());
        rightPanel.add(Box.createVerticalStrut(10));

        // Pricing Rows
        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);
        JLabel totLabel = new JLabel("Total to Pay:");
        totLabel.setFont(UITheme.FONT_HEADER);
        totalAmountLabel = new JLabel("\u20B9 0.00");
        totalAmountLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        totalAmountLabel.setForeground(UITheme.PRIMARY);

        totalRow.add(totLabel, BorderLayout.WEST);
        totalRow.add(totalAmountLabel, BorderLayout.EAST);
        rightPanel.add(totalRow);
        rightPanel.add(Box.createVerticalStrut(16));

        // Checkout Button
        checkoutBtn = new JButton("💳 Place Order (ACID Transaction)");
        checkoutBtn.setFont(UITheme.FONT_BOLD);
        checkoutBtn.setBackground(UITheme.SUCCESS);
        checkoutBtn.setForeground(Color.WHITE);
        checkoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        checkoutBtn.setPreferredSize(new Dimension(320, 44));
        checkoutBtn.addActionListener(e -> onPlaceOrder());

        rightPanel.add(checkoutBtn);
        rightPanel.add(Box.createVerticalGlue());

        add(leftPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }

    private void togglePaymentFields() {
        boolean isCard = "Credit Card".equals(paymentTypeCombo.getSelectedItem());
        cardFieldsPanel.setVisible(isCard);
        walletFieldsPanel.setVisible(!isCard);
        revalidate();
        repaint();
    }

    public void reload() {
        Customer cust = customerSupplier.get();
        if (cust == null) {
            tableModel.setRowCount(0);
            totalAmountLabel.setText("\u20B9 0.00");
            checkoutBtn.setEnabled(false);
            return;
        }

        currentCart = portalService.getOrCreateCart(cust.getCustomerId());
        currentCartItems = portalService.getCartItems(currentCart.getCartId());

        tableModel.setRowCount(0);
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem it : currentCartItems) {
            tableModel.addRow(new Object[]{
                    it.getProductName() != null ? it.getProductName() : "Product #" + it.getProductId(),
                    "\u20B9 " + it.getUnitPrice(),
                    it.getQuantity(),
                    "\u20B9 " + it.getSubtotal(),
                    "In Cart"
            });
            total = total.add(it.getSubtotal());
        }

        totalAmountLabel.setText("\u20B9 " + total);
        checkoutBtn.setEnabled(!currentCartItems.isEmpty());

        loadAddresses();
    }

    private void loadAddresses() {
        addressCombo.removeAllItems();
        Customer cust = customerSupplier.get();
        if (cust == null) return;

        List<Address> addresses = portalService.getCustomerAddresses(cust.getCustomerId());
        for (Address a : addresses) {
            addressCombo.addItem(new ComboItem<>(a.getAddressId(), a.getStreet() + ", " + a.getCity() + " - " + a.getPincode()));
        }
    }

    private void adjustSelectedQuantity(int delta) {
        int row = cartTable.getSelectedRow();
        if (row == -1 || row >= currentCartItems.size() || currentCart == null) return;
        CartItem item = currentCartItems.get(row);
        int newQty = item.getQuantity() + delta;
        portalService.updateCartItemQuantity(currentCart.getCartId(), item.getProductId(), newQty);
        reload();
    }

    private void removeSelectedItem() {
        int row = cartTable.getSelectedRow();
        if (row == -1 || row >= currentCartItems.size() || currentCart == null) return;
        CartItem item = currentCartItems.get(row);
        portalService.removeCartItem(currentCart.getCartId(), item.getProductId());
        reload();
    }

    private void onPlaceOrder() {
        Customer cust = customerSupplier.get();
        if (cust == null) {
            NotificationUtils.showWarning(this, "Please select an active customer.");
            return;
        }

        ComboItem<Integer> addrItem = (ComboItem<Integer>) addressCombo.getSelectedItem();
        if (addrItem == null) {
            NotificationUtils.showWarning(this, "Please select or add a delivery address.");
            return;
        }

        String payType = (String) paymentTypeCombo.getSelectedItem();
        String cardNo = cardNumberField.getText().trim();
        String cvv = cvvField.getText().trim();
        LocalDate parsedExp = LocalDate.now().plusYears(2);
        try {
            parsedExp = LocalDate.parse(expiryDateField.getText().trim());
        } catch (Exception ignored) {}
        final LocalDate exp = parsedExp;

        String walletProv = (String) walletProviderCombo.getSelectedItem();
        String walletRef = walletRefField.getText().trim();

        checkoutBtn.setEnabled(false);
        checkoutBtn.setText("Executing MySQL Transaction...");

        new SwingWorker<Order, Void>() {
            @Override
            protected Order doInBackground() {
                return portalService.checkout(
                        cust.getCustomerId(),
                        currentCart.getCartId(),
                        addrItem.getId(),
                        payType,
                        cardNo,
                        cvv,
                        exp,
                        walletProv,
                        walletRef
                );
            }

            @Override
            protected void done() {
                checkoutBtn.setEnabled(true);
                checkoutBtn.setText("💳 Place Order (ACID Transaction)");
                try {
                    Order placed = get();
                    NotificationUtils.showSuccess(CartPanel.this,
                            "🎉 Order #" + placed.getOrderId() + " placed successfully!\n\n" +
                            "Total Amount: \u20B9" + placed.getTotalAmount() + "\n" +
                            "Payment Status: Completed (" + payType + ")\n" +
                            "Database stock has been updated automatically via triggers.");
                    reload();
                    if (onOrderPlaced != null) onOrderPlaced.run();
                } catch (Exception e) {
                    NotificationUtils.showError(CartPanel.this, "Checkout Failed", e);
                }
            }
        }.execute();
    }
}
