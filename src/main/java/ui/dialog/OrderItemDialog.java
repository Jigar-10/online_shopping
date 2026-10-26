package ui.dialog;

import dao.OrderDao;
import dao.OrderItemDao;
import dao.ProductDao;
import model.ComboItem;
import model.OrderItem;
import service.ValidationService;
import ui.util.NotificationUtils;
import ui.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class OrderItemDialog extends JDialog {
    private final OrderItemDao orderItemDao = new OrderItemDao();
    private final OrderDao orderDao = new OrderDao();
    private final ProductDao productDao = new ProductDao();
    private final OrderItem existingItem;
    private boolean saved = false;

    private JComboBox<ComboItem<Integer>> orderCombo;
    private JComboBox<ComboItem<Integer>> productCombo;
    private JSpinner quantitySpinner;
    private JTextField unitPriceField;

    public OrderItemDialog(Window owner, OrderItem item) {
        super(owner, item == null ? "Add Line Item to Order" : "Edit Order Line Item", ModalityType.APPLICATION_MODAL);
        this.existingItem = item;
        initUI();
    }

    private void initUI() {
        setSize(480, 320);
        setLocationRelativeTo(getOwner());
        setResizable(false);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        orderCombo = new JComboBox<>();
        productCombo = new JComboBox<>();
        loadCombos();

        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        unitPriceField = new JTextField("0.00", 20);

        productCombo.addActionListener(e -> {
            ComboItem<Integer> selected = (ComboItem<Integer>) productCombo.getSelectedItem();
            if (selected != null && selected.getExtra() instanceof BigDecimal price) {
                unitPriceField.setText(price.toString());
            }
        });

        if (existingItem != null) {
            orderCombo.setEnabled(false); // Composite primary key cannot be altered
            productCombo.setEnabled(false);
            quantitySpinner.setValue(existingItem.getQuantity());
            unitPriceField.setText(existingItem.getUnitPrice().toString());

            for (int i = 0; i < orderCombo.getItemCount(); i++) {
                if (orderCombo.getItemAt(i).getId().equals(existingItem.getOrderId())) {
                    orderCombo.setSelectedIndex(i);
                    break;
                }
            }

            for (int i = 0; i < productCombo.getItemCount(); i++) {
                if (productCombo.getItemAt(i).getId().equals(existingItem.getProductId())) {
                    productCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        int row = 0;
        addFormField(form, gbc, row++, "Order *:", orderCombo);
        addFormField(form, gbc, row++, "Product *:", productCombo);
        addFormField(form, gbc, row++, "Quantity *:", quantitySpinner);
        addFormField(form, gbc, row++, "Unit Price (\u20B9) *:", unitPriceField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Separator.foreground")));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = new JButton(existingItem == null ? "Add Line Item" : "Update Line Item");
        saveBtn.setBackground(UITheme.PRIMARY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(UITheme.FONT_BOLD);
        saveBtn.addActionListener(e -> onSave());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        add(form, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadCombos() {
        try {
            List<ComboItem<Integer>> orders = orderDao.findAllAsComboItems();
            for (ComboItem<Integer> o : orders) orderCombo.addItem(o);

            List<ComboItem<Integer>> prods = productDao.findAllAsComboItems();
            for (ComboItem<Integer> p : prods) productCombo.addItem(p);

            if (!prods.isEmpty() && existingItem == null) {
                ComboItem<Integer> first = prods.get(0);
                if (first.getExtra() instanceof BigDecimal price) {
                    unitPriceField.setText(price.toString());
                }
            }
        } catch (Exception e) {
            NotificationUtils.showError(this, "Could not load dropdown data", e);
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
            ComboItem<Integer> o = (ComboItem<Integer>) orderCombo.getSelectedItem();
            ComboItem<Integer> p = (ComboItem<Integer>) productCombo.getSelectedItem();

            if (o == null || p == null) {
                NotificationUtils.showWarning(this, "Please select both an order and a product.");
                return;
            }

            BigDecimal unitPrice;
            try {
                unitPrice = new BigDecimal(unitPriceField.getText().trim());
            } catch (NumberFormatException e) {
                NotificationUtils.showWarning(this, "Please enter a valid unit price.");
                return;
            }

            OrderItem item = existingItem != null ? existingItem : new OrderItem();
            item.setOrderId(o.getId());
            item.setProductId(p.getId());
            item.setQuantity((Integer) quantitySpinner.getValue());
            item.setUnitPrice(unitPrice);

            ValidationService.validateOrderItem(item);

            if (existingItem == null) {
                orderItemDao.insert(item);
                NotificationUtils.showSuccess(this, "Item added to Order #" + item.getOrderId() + " successfully!");
            } else {
                orderItemDao.update(item);
                NotificationUtils.showSuccess(this, "Order item updated successfully!");
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
