package ui.dialog;

import dao.DigitalWalletDao;
import dao.PaymentDao;
import model.ComboItem;
import model.DigitalWallet;
import service.ValidationService;
import ui.util.NotificationUtils;
import ui.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class DigitalWalletDialog extends JDialog {
    private final DigitalWalletDao digitalWalletDao = new DigitalWalletDao();
    private final PaymentDao paymentDao = new PaymentDao();
    private final DigitalWallet existingWallet;
    private boolean saved = false;

    private JComboBox<ComboItem<Integer>> paymentCombo;
    private JComboBox<String> providerCombo;
    private JTextField walletRefField;

    public DigitalWalletDialog(Window owner, DigitalWallet wallet) {
        super(owner, wallet == null ? "Register Digital Wallet Payment" : "Edit Digital Wallet Details", ModalityType.APPLICATION_MODAL);
        this.existingWallet = wallet;
        initUI();
    }

    private void initUI() {
        setSize(480, 280);
        setLocationRelativeTo(getOwner());
        setResizable(false);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        paymentCombo = new JComboBox<>();
        loadPayments();

        providerCombo = new JComboBox<>(new String[]{"Paytm", "PhonePe", "Google Pay", "Amazon Pay", "Apple Pay", "PayPal"});
        providerCombo.setEditable(true);
        walletRefField = new JTextField(20);

        if (existingWallet != null) {
            paymentCombo.setEnabled(false); // Primary key cannot be altered
            providerCombo.setSelectedItem(existingWallet.getProvider());
            walletRefField.setText(existingWallet.getWalletRef());

            for (int i = 0; i < paymentCombo.getItemCount(); i++) {
                if (paymentCombo.getItemAt(i).getId().equals(existingWallet.getPaymentId())) {
                    paymentCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        int row = 0;
        addFormField(form, gbc, row++, "Payment Record *:", paymentCombo);
        addFormField(form, gbc, row++, "Wallet Provider *:", providerCombo);
        addFormField(form, gbc, row++, "Wallet Reference ID *:", walletRefField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Separator.foreground")));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = new JButton(existingWallet == null ? "Save Wallet Details" : "Save Changes");
        saveBtn.setBackground(UITheme.PRIMARY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(UITheme.FONT_BOLD);
        saveBtn.addActionListener(e -> onSave());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        add(form, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadPayments() {
        try {
            List<ComboItem<Integer>> items = paymentDao.findAllAsComboItems("Digital Wallet");
            for (ComboItem<Integer> p : items) paymentCombo.addItem(p);
        } catch (Exception e) {
            NotificationUtils.showError(this, "Could not load wallet payments", e);
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
            ComboItem<Integer> p = (ComboItem<Integer>) paymentCombo.getSelectedItem();
            if (p == null) {
                NotificationUtils.showWarning(this, "Please select a payment record.");
                return;
            }

            DigitalWallet w = existingWallet != null ? existingWallet : new DigitalWallet();
            w.setPaymentId(p.getId());
            w.setProvider((String) providerCombo.getSelectedItem());
            w.setWalletRef(walletRefField.getText().trim());

            ValidationService.validateDigitalWallet(w);

            if (existingWallet == null) {
                digitalWalletDao.insert(w);
                NotificationUtils.showSuccess(this, "Digital wallet details saved for Payment #" + w.getPaymentId() + "!");
            } else {
                digitalWalletDao.update(w);
                NotificationUtils.showSuccess(this, "Digital wallet details updated successfully!");
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
