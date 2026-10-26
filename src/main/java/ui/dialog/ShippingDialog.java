package ui.dialog;

import dao.AddressDao;
import dao.ShippingDao;
import model.ComboItem;
import model.Shipping;
import service.ValidationService;
import ui.util.NotificationUtils;
import ui.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class ShippingDialog extends JDialog {
    private final ShippingDao shippingDao = new ShippingDao();
    private final AddressDao addressDao = new AddressDao();
    private final Shipping existingShipping;
    private boolean saved = false;

    private JComboBox<ComboItem<Integer>> addressCombo;
    private JTextField carrierField;
    private JTextField trackingNoField;
    private JComboBox<String> statusCombo;

    public ShippingDialog(Window owner, Shipping shipping) {
        super(owner, shipping == null ? "Create Shipment" : "Edit Shipment #" + shipping.getShipmentId(), ModalityType.APPLICATION_MODAL);
        this.existingShipping = shipping;
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

        addressCombo = new JComboBox<>();
        loadAddresses();

        carrierField = new JTextField(20);
        trackingNoField = new JTextField(20);
        statusCombo = new JComboBox<>(new String[]{"Pending", "Shipped", "In Transit", "Delivered", "Cancelled"});

        if (existingShipping != null) {
            carrierField.setText(existingShipping.getCarrier());
            trackingNoField.setText(existingShipping.getTrackingNo());
            statusCombo.setSelectedItem(existingShipping.getStatus());

            for (int i = 0; i < addressCombo.getItemCount(); i++) {
                if (addressCombo.getItemAt(i).getId().equals(existingShipping.getAddressId())) {
                    addressCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        int row = 0;
        addFormField(form, gbc, row++, "Delivery Address *:", addressCombo);
        addFormField(form, gbc, row++, "Carrier (e.g. BlueDart) *:", carrierField);
        addFormField(form, gbc, row++, "Tracking Number *:", trackingNoField);
        addFormField(form, gbc, row++, "Status:", statusCombo);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Separator.foreground")));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = new JButton(existingShipping == null ? "Create Shipment" : "Save Changes");
        saveBtn.setBackground(UITheme.PRIMARY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(UITheme.FONT_BOLD);
        saveBtn.addActionListener(e -> onSave());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        add(form, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadAddresses() {
        try {
            List<ComboItem<Integer>> items = addressDao.findAllAsComboItems();
            for (ComboItem<Integer> it : items) {
                addressCombo.addItem(it);
            }
        } catch (Exception e) {
            NotificationUtils.showError(this, "Could not load addresses", e);
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
            ComboItem<Integer> selectedAddr = (ComboItem<Integer>) addressCombo.getSelectedItem();
            if (selectedAddr == null) {
                NotificationUtils.showWarning(this, "Please select an address.");
                return;
            }

            Shipping s = existingShipping != null ? existingShipping : new Shipping();
            s.setAddressId(selectedAddr.getId());
            s.setCarrier(carrierField.getText().trim());
            s.setTrackingNo(trackingNoField.getText().trim());
            s.setStatus((String) statusCombo.getSelectedItem());

            ValidationService.validateShipping(s);

            if (existingShipping == null) {
                shippingDao.insert(s);
                NotificationUtils.showSuccess(this, "Shipment created successfully with ID #" + s.getShipmentId() + "!");
            } else {
                shippingDao.update(s);
                NotificationUtils.showSuccess(this, "Shipment #" + s.getShipmentId() + " updated successfully!");
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
