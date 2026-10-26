package ui.dialog;

import dao.CartDao;
import dao.CustomerDao;
import model.Cart;
import model.ComboItem;
import service.ValidationService;
import ui.util.NotificationUtils;
import ui.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;

public class CartDialog extends JDialog {
    private final CartDao cartDao = new CartDao();
    private final CustomerDao customerDao = new CustomerDao();
    private final Cart existingCart;
    private boolean saved = false;

    private JComboBox<ComboItem<Integer>> customerCombo;

    public CartDialog(Window owner, Cart cart) {
        super(owner, cart == null ? "Create Shopping Cart" : "Edit Cart #" + cart.getCartId(), ModalityType.APPLICATION_MODAL);
        this.existingCart = cart;
        initUI();
    }

    private void initUI() {
        setSize(440, 240);
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

        if (existingCart != null) {
            for (int i = 0; i < customerCombo.getItemCount(); i++) {
                if (customerCombo.getItemAt(i).getId().equals(existingCart.getCustomerId())) {
                    customerCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.35;
        JLabel label = new JLabel("Customer *:");
        label.setFont(UITheme.FONT_BOLD);
        form.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        form.add(customerCombo, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Separator.foreground")));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = new JButton(existingCart == null ? "Create Cart" : "Save Changes");
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
            NotificationUtils.showError(this, "Could not load customers", e);
        }
    }

    private void onSave() {
        try {
            ComboItem<Integer> cust = (ComboItem<Integer>) customerCombo.getSelectedItem();
            if (cust == null) {
                NotificationUtils.showWarning(this, "Please select a customer.");
                return;
            }

            Cart c = existingCart != null ? existingCart : new Cart();
            c.setCustomerId(cust.getId());
            if (existingCart == null) {
                c.setCreatedOn(LocalDateTime.now());
            }

            ValidationService.validateCart(c);

            if (existingCart == null) {
                cartDao.insert(c);
                NotificationUtils.showSuccess(this, "Shopping cart created with ID #" + c.getCartId() + "!");
            } else {
                cartDao.update(c);
                NotificationUtils.showSuccess(this, "Cart #" + c.getCartId() + " updated successfully!");
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
