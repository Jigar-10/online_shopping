package ui.dialog;

import dao.OrderDao;
import dao.PaymentDao;
import model.ComboItem;
import model.Payment;
import service.ValidationService;
import ui.util.NotificationUtils;
import ui.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PaymentDialog extends JDialog {
    private final PaymentDao paymentDao = new PaymentDao();
    private final OrderDao orderDao = new OrderDao();
    private final Payment existingPayment;
    private boolean saved = false;

    private JComboBox<ComboItem<Integer>> orderCombo;
    private JTextField amountField;
    private JComboBox<String> typeCombo;
    private JComboBox<String> statusCombo;

    public PaymentDialog(Window owner, Payment payment) {
        super(owner, payment == null ? "Record New Payment" : "Edit Payment #" + payment.getPaymentId(), ModalityType.APPLICATION_MODAL);
        this.existingPayment = payment;
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

        orderCombo = new JComboBox<>();
        loadOrders();

        amountField = new JTextField("0.00", 20);
        typeCombo = new JComboBox<>(new String[]{"Credit Card", "Digital Wallet", "Net Banking", "UPI", "Cash on Delivery"});
        statusCombo = new JComboBox<>(new String[]{"Completed", "Pending", "Failed", "Refunded"});

        if (existingPayment != null) {
            amountField.setText(existingPayment.getAmount().toString());
            typeCombo.setSelectedItem(existingPayment.getPaymentType());
            statusCombo.setSelectedItem(existingPayment.getStatus());

            for (int i = 0; i < orderCombo.getItemCount(); i++) {
                if (orderCombo.getItemAt(i).getId().equals(existingPayment.getOrderId())) {
                    orderCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        int row = 0;
        addFormField(form, gbc, row++, "Order *:", orderCombo);
        addFormField(form, gbc, row++, "Amount (\u20B9) *:", amountField);
        addFormField(form, gbc, row++, "Payment Method *:", typeCombo);
        addFormField(form, gbc, row++, "Payment Status:", statusCombo);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Separator.foreground")));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = new JButton(existingPayment == null ? "Record Payment" : "Save Changes");
        saveBtn.setBackground(UITheme.PRIMARY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(UITheme.FONT_BOLD);
        saveBtn.addActionListener(e -> onSave());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        add(form, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadOrders() {
        try {
            List<ComboItem<Integer>> orders = orderDao.findAllAsComboItems();
            for (ComboItem<Integer> o : orders) orderCombo.addItem(o);
        } catch (Exception e) {
            NotificationUtils.showError(this, "Could not load orders", e);
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
            ComboItem<Integer> ord = (ComboItem<Integer>) orderCombo.getSelectedItem();
            if (ord == null) {
                NotificationUtils.showWarning(this, "Please select an order.");
                return;
            }

            BigDecimal amt;
            try {
                amt = new BigDecimal(amountField.getText().trim());
            } catch (NumberFormatException e) {
                NotificationUtils.showWarning(this, "Please enter a valid amount.");
                return;
            }

            Payment p = existingPayment != null ? existingPayment : new Payment();
            p.setOrderId(ord.getId());
            p.setAmount(amt);
            p.setPaymentType((String) typeCombo.getSelectedItem());
            p.setStatus((String) statusCombo.getSelectedItem());
            if (existingPayment == null) {
                p.setPaymentDate(LocalDateTime.now());
            }

            ValidationService.validatePayment(p);

            if (existingPayment == null) {
                paymentDao.insert(p);
                NotificationUtils.showSuccess(this, "Payment recorded with ID #" + p.getPaymentId() + "!");
            } else {
                paymentDao.update(p);
                NotificationUtils.showSuccess(this, "Payment #" + p.getPaymentId() + " updated successfully!");
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
