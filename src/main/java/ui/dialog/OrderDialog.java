package ui.dialog;

import dao.CustomerDao;
import dao.OrderDao;
import dao.ShippingDao;
import model.ComboItem;
import model.Order;
import service.ValidationService;
import ui.util.NotificationUtils;
import ui.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDialog extends JDialog {
    private final OrderDao orderDao = new OrderDao();
    private final CustomerDao customerDao = new CustomerDao();
    private final ShippingDao shippingDao = new ShippingDao();
    private final Order existingOrder;
    private boolean saved = false;

    private JComboBox<ComboItem<Integer>> customerCombo;
    private JComboBox<ComboItem<Integer>> shipmentCombo;
    private JTextField totalAmountField;
    private JComboBox<String> statusCombo;

    public OrderDialog(Window owner, Order order) {
        super(owner, order == null ? "Create New Order" : "Edit Order #" + order.getOrderId(), ModalityType.APPLICATION_MODAL);
        this.existingOrder = order;
        initUI();
    }

    private void initUI() {
        setSize(480, 360);
        setLocationRelativeTo(getOwner());
        setResizable(false);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        customerCombo = new JComboBox<>();
        loadCustomers();

        shipmentCombo = new JComboBox<>();
        loadShipments();

        totalAmountField = new JTextField("0.00", 20);
        statusCombo = new JComboBox<>(new String[]{"Pending", "Processing", "Shipped", "Delivered", "Cancelled"});

        if (existingOrder != null) {
            totalAmountField.setText(existingOrder.getTotalAmount().toString());
            statusCombo.setSelectedItem(existingOrder.getStatus());

            for (int i = 0; i < customerCombo.getItemCount(); i++) {
                if (customerCombo.getItemAt(i).getId().equals(existingOrder.getCustomerId())) {
                    customerCombo.setSelectedIndex(i);
                    break;
                }
            }

            if (existingOrder.getShipmentId() != null) {
                for (int i = 0; i < shipmentCombo.getItemCount(); i++) {
                    if (shipmentCombo.getItemAt(i).getId() != null &&
                        shipmentCombo.getItemAt(i).getId().equals(existingOrder.getShipmentId())) {
                        shipmentCombo.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }

        int row = 0;
        addFormField(form, gbc, row++, "Customer *:", customerCombo);
        addFormField(form, gbc, row++, "Assigned Shipment:", shipmentCombo);
        addFormField(form, gbc, row++, "Total Amount (\u20B9) *:", totalAmountField);
        addFormField(form, gbc, row++, "Order Status:", statusCombo);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Separator.foreground")));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = new JButton(existingOrder == null ? "Create Order" : "Save Changes");
        saveBtn.setBackground(UITheme.PRIMARY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(UITheme.FONT_BOLD);
        saveBtn.addActionListener(e -> onSave());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        add(form, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadCustomers() {
        try {
            List<ComboItem<Integer>> items = customerDao.findAllAsComboItems();
            for (ComboItem<Integer> it : items) customerCombo.addItem(it);
        } catch (Exception e) {
            NotificationUtils.showError(this, "Could not load customers", e);
        }
    }

    private void loadShipments() {
        try {
            shipmentCombo.addItem(new ComboItem<>(null, "-- None (Pending Shipment) --"));
            List<ComboItem<Integer>> items = shippingDao.findAllAsComboItems();
            for (ComboItem<Integer> it : items) shipmentCombo.addItem(it);
        } catch (Exception e) {
            NotificationUtils.showError(this, "Could not load shipments", e);
        }
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        JLabel label = new JLabel(labelText);
        label.setFont(UITheme.FONT_BOLD);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        panel.add(field, gbc);
    }

    private void onSave() {
        try {
            ComboItem<Integer> cust = (ComboItem<Integer>) customerCombo.getSelectedItem();
            if (cust == null) {
                NotificationUtils.showWarning(this, "Please select a customer.");
                return;
            }

            ComboItem<Integer> ship = (ComboItem<Integer>) shipmentCombo.getSelectedItem();
            Integer shipId = ship != null ? ship.getId() : null;

            BigDecimal total;
            try {
                total = new BigDecimal(totalAmountField.getText().trim());
            } catch (NumberFormatException e) {
                NotificationUtils.showWarning(this, "Please enter a valid total amount.");
                return;
            }

            Order o = existingOrder != null ? existingOrder : new Order();
            o.setCustomerId(cust.getId());
            o.setShipmentId(shipId);
            o.setTotalAmount(total);
            o.setStatus((String) statusCombo.getSelectedItem());
            if (existingOrder == null) {
                o.setOrderDate(LocalDateTime.now());
            }

            ValidationService.validateOrder(o);

            if (existingOrder == null) {
                orderDao.insert(o);
                NotificationUtils.showSuccess(this, "Order created with ID #" + o.getOrderId() + "!");
            } else {
                orderDao.update(o);
                NotificationUtils.showSuccess(this, "Order #" + o.getOrderId() + " updated successfully!");
            }

            saved = true;
            dispose();
        } catch (Exception ex) {
            NotificationUtils.showError(this, ex.getMessage(), ex);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
