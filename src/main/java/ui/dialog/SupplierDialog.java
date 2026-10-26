package ui.dialog;

import dao.SupplierDao;
import model.Supplier;
import service.ValidationService;
import ui.util.NotificationUtils;
import ui.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SupplierDialog extends JDialog {
    private final SupplierDao supplierDao = new SupplierDao();
    private final Supplier existingSupplier;
    private boolean saved = false;

    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;

    public SupplierDialog(Window owner, Supplier supplier) {
        super(owner, supplier == null ? "Register New Supplier" : "Edit Supplier #" + supplier.getSupplierId(), ModalityType.APPLICATION_MODAL);
        this.existingSupplier = supplier;
        initUI();
    }

    private void initUI() {
        setSize(440, 290);
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

        if (existingSupplier != null) {
            nameField.setText(existingSupplier.getName());
            emailField.setText(existingSupplier.getContactEmail());
            phoneField.setText(existingSupplier.getPhone());
        }

        int row = 0;
        addFormField(form, gbc, row++, "Supplier Name *:", nameField);
        addFormField(form, gbc, row++, "Contact Email:", emailField);
        addFormField(form, gbc, row++, "Phone:", phoneField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Separator.foreground")));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = new JButton(existingSupplier == null ? "Save Supplier" : "Save Changes");
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
            Supplier s = existingSupplier != null ? existingSupplier : new Supplier();
            s.setName(nameField.getText().trim());
            s.setContactEmail(emailField.getText().trim());
            s.setPhone(phoneField.getText().trim());

            ValidationService.validateSupplier(s);

            if (existingSupplier == null) {
                supplierDao.insert(s);
                NotificationUtils.showSuccess(this, "Supplier registered with ID #" + s.getSupplierId() + "!");
            } else {
                supplierDao.update(s);
                NotificationUtils.showSuccess(this, "Supplier #" + s.getSupplierId() + " updated successfully!");
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
