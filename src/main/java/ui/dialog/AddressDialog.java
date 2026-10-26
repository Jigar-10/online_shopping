package ui.dialog;

import dao.AddressDao;
import dao.CustomerDao;
import model.Address;
import model.ComboItem;
import service.ValidationService;
import ui.util.NotificationUtils;
import ui.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class AddressDialog extends JDialog {
    private final AddressDao addressDao = new AddressDao();
    private final CustomerDao customerDao = new CustomerDao();
    private final Address existingAddress;
    private boolean saved = false;

    private JComboBox<ComboItem<Integer>> customerCombo;
    private JTextField streetField;
    private JTextField cityField;
    private JTextField stateField;
    private JTextField countryField;
    private JTextField pincodeField;

    public AddressDialog(Window owner, Address address) {
        super(owner, address == null ? "Add Delivery Address" : "Edit Address #" + address.getAddressId(), ModalityType.APPLICATION_MODAL);
        this.existingAddress = address;
        initUI();
    }

    private void initUI() {
        setSize(480, 420);
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

        streetField = new JTextField(20);
        cityField = new JTextField(20);
        stateField = new JTextField(20);
        countryField = new JTextField("India", 20);
        pincodeField = new JTextField(20);

        if (existingAddress != null) {
            streetField.setText(existingAddress.getStreet());
            cityField.setText(existingAddress.getCity());
            stateField.setText(existingAddress.getState());
            countryField.setText(existingAddress.getCountry());
            pincodeField.setText(existingAddress.getPincode());

            // Select customer
            for (int i = 0; i < customerCombo.getItemCount(); i++) {
                ComboItem<Integer> item = customerCombo.getItemAt(i);
                if (item.getId().equals(existingAddress.getCustomerId())) {
                    customerCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        int row = 0;
        addFormField(form, gbc, row++, "Customer *:", customerCombo);
        addFormField(form, gbc, row++, "Street Address *:", streetField);
        addFormField(form, gbc, row++, "City *:", cityField);
        addFormField(form, gbc, row++, "State *:", stateField);
        addFormField(form, gbc, row++, "Country *:", countryField);
        addFormField(form, gbc, row++, "Pincode (6 digits) *:", pincodeField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Separator.foreground")));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = new JButton(existingAddress == null ? "Add Address" : "Save Changes");
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
            for (ComboItem<Integer> it : items) {
                customerCombo.addItem(it);
            }
        } catch (Exception e) {
            NotificationUtils.showError(this, "Could not load customers for dropdown", e);
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
            ComboItem<Integer> selectedCust = (ComboItem<Integer>) customerCombo.getSelectedItem();
            if (selectedCust == null) {
                NotificationUtils.showWarning(this, "Please select a customer.");
                return;
            }

            Address a = existingAddress != null ? existingAddress : new Address();
            a.setCustomerId(selectedCust.getId());
            a.setStreet(streetField.getText().trim());
            a.setCity(cityField.getText().trim());
            a.setState(stateField.getText().trim());
            a.setCountry(countryField.getText().trim());
            a.setPincode(pincodeField.getText().trim());

            ValidationService.validateAddress(a);

            if (existingAddress == null) {
                addressDao.insert(a);
                NotificationUtils.showSuccess(this, "Address added successfully with ID #" + a.getAddressId() + "!");
            } else {
                addressDao.update(a);
                NotificationUtils.showSuccess(this, "Address #" + a.getAddressId() + " updated successfully!");
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
