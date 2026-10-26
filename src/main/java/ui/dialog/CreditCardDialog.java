package ui.dialog;

import dao.CreditCardDao;
import dao.PaymentDao;
import exception.ValidationException;
import model.ComboItem;
import model.CreditCard;
import service.ValidationService;
import ui.util.NotificationUtils;
import ui.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class CreditCardDialog extends JDialog {
    private final CreditCardDao creditCardDao = new CreditCardDao();
    private final PaymentDao paymentDao = new PaymentDao();
    private final CreditCard existingCard;
    private boolean saved = false;

    private JComboBox<ComboItem<Integer>> paymentCombo;
    private JTextField cardNumberField;
    private JTextField cvvField;
    private JTextField expiryDateField;

    public CreditCardDialog(Window owner, CreditCard card) {
        super(owner, card == null ? "Register Credit Card Payment" : "Edit Credit Card Details", ModalityType.APPLICATION_MODAL);
        this.existingCard = card;
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

        paymentCombo = new JComboBox<>();
        loadPayments();

        cardNumberField = new JTextField(20);
        cvvField = new JTextField(6);
        expiryDateField = new JTextField(LocalDate.now().plusYears(3).toString(), 20);

        if (existingCard != null) {
            paymentCombo.setEnabled(false); // Primary key cannot be altered
            cardNumberField.setText(existingCard.getCardNumber());
            cvvField.setText(existingCard.getCvv());
            if (existingCard.getExpiryDate() != null) {
                expiryDateField.setText(existingCard.getExpiryDate().toString());
            }

            for (int i = 0; i < paymentCombo.getItemCount(); i++) {
                if (paymentCombo.getItemAt(i).getId().equals(existingCard.getPaymentId())) {
                    paymentCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        int row = 0;
        addFormField(form, gbc, row++, "Payment Record *:", paymentCombo);
        addFormField(form, gbc, row++, "Card Number (16 digits) *:", cardNumberField);
        addFormField(form, gbc, row++, "CVV (3 digits) *:", cvvField);
        addFormField(form, gbc, row++, "Expiry Date (YYYY-MM-DD) *:", expiryDateField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Separator.foreground")));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = new JButton(existingCard == null ? "Save Card Details" : "Save Changes");
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
            List<ComboItem<Integer>> items = paymentDao.findAllAsComboItems("Credit Card");
            for (ComboItem<Integer> p : items) paymentCombo.addItem(p);
        } catch (Exception e) {
            NotificationUtils.showError(this, "Could not load credit card payments", e);
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

            LocalDate exp;
            try {
                exp = LocalDate.parse(expiryDateField.getText().trim());
            } catch (DateTimeParseException ex) {
                throw new ValidationException("Invalid expiry date format. Use YYYY-MM-DD (e.g. 2028-12-31).");
            }

            CreditCard card = existingCard != null ? existingCard : new CreditCard();
            card.setPaymentId(p.getId());
            card.setCardNumber(cardNumberField.getText().replaceAll("\\s+", ""));
            card.setCvv(cvvField.getText().trim());
            card.setExpiryDate(exp);

            ValidationService.validateCreditCard(card);

            if (existingCard == null) {
                creditCardDao.insert(card);
                NotificationUtils.showSuccess(this, "Credit card details saved for Payment #" + card.getPaymentId() + "!");
            } else {
                creditCardDao.update(card);
                NotificationUtils.showSuccess(this, "Credit card details updated successfully!");
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
