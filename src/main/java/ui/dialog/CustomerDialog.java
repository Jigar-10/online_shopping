package ui.dialog;

import dao.CustomerDao;
import exception.ValidationException;
import model.Customer;
import service.ValidationService;
import ui.util.NotificationUtils;
import ui.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class CustomerDialog extends JDialog {
    private final CustomerDao customerDao = new CustomerDao();
    private final Customer existingCustomer;
    private boolean saved = false;

    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField registeredOnField;
    private JComboBox<String> genderCombo;

    public CustomerDialog(Window owner, Customer customer) {
        super(owner, customer == null ? "Register New Customer" : "Edit Customer #" + customer.getCustomerId(), ModalityType.APPLICATION_MODAL);
        this.existingCustomer = customer;
        initUI();
    }

    private void initUI() {
        setSize(460, 380);
        setLocationRelativeTo(getOwner());
        setResizable(false);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        nameField = new JTextField(20);
        emailField = new JTextField(20);
        phoneField = new JTextField(20);
        registeredOnField = new JTextField(LocalDate.now().toString(), 20);
        genderCombo = new JComboBox<>(new String[]{"Male", "Female", "Other", "Prefer not to say"});

        if (existingCustomer != null) {
            nameField.setText(existingCustomer.getName());
            emailField.setText(existingCustomer.getEmail());
            phoneField.setText(existingCustomer.getPhone());
            if (existingCustomer.getRegisteredOn() != null) {
                registeredOnField.setText(existingCustomer.getRegisteredOn().toString());
            }
            if (existingCustomer.getGender() != null) {
                genderCombo.setSelectedItem(existingCustomer.getGender());
            }
        }

        int row = 0;
        addFormField(form, gbc, row++, "Full Name *:", nameField);
        addFormField(form, gbc, row++, "Email Address *:", emailField);
        addFormField(form, gbc, row++, "Phone (10 digits):", phoneField);
        addFormField(form, gbc, row++, "Registration Date (YYYY-MM-DD):", registeredOnField);
        addFormField(form, gbc, row++, "Gender:", genderCombo);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Separator.foreground")));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = new JButton(existingCustomer == null ? "Register Customer" : "Save Changes");
        saveBtn.setBackground(UITheme.PRIMARY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(UITheme.FONT_BOLD);
        saveBtn.addActionListener(e -> onSave());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        add(form, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
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
            Customer c = existingCustomer != null ? existingCustomer : new Customer();
            c.setName(nameField.getText().trim());
            c.setEmail(emailField.getText().trim());
            c.setPhone(phoneField.getText().trim());
            c.setGender((String) genderCombo.getSelectedItem());

            String dateStr = registeredOnField.getText().trim();
            if (!dateStr.isEmpty()) {
                try {
                    c.setRegisteredOn(LocalDate.parse(dateStr));
                } catch (DateTimeParseException ex) {
                    throw new ValidationException("Invalid date format. Use YYYY-MM-DD (e.g. 2026-10-26).");
                }
            }

            ValidationService.validateCustomer(c);

            if (existingCustomer == null) {
                customerDao.insert(c);
                NotificationUtils.showSuccess(this, "Customer registered successfully with ID #" + c.getCustomerId() + "!");
            } else {
                customerDao.update(c);
                NotificationUtils.showSuccess(this, "Customer #" + c.getCustomerId() + " updated successfully!");
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
