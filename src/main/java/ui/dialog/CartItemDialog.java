package ui.dialog;

import dao.CartDao;
import dao.CartItemDao;
import dao.ProductDao;
import model.CartItem;
import model.ComboItem;
import service.ValidationService;
import ui.util.NotificationUtils;
import ui.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class CartItemDialog extends JDialog {
    private final CartItemDao cartItemDao = new CartItemDao();
    private final CartDao cartDao = new CartDao();
    private final ProductDao productDao = new ProductDao();
    private final CartItem existingItem;
    private boolean saved = false;

    private JComboBox<ComboItem<Integer>> cartCombo;
    private JComboBox<ComboItem<Integer>> productCombo;
    private JSpinner quantitySpinner;

    public CartItemDialog(Window owner, CartItem item) {
        super(owner, item == null ? "Add Item to Cart" : "Edit Cart Item", ModalityType.APPLICATION_MODAL);
        this.existingItem = item;
        initUI();
    }

    private void initUI() {
        setSize(460, 290);
        setLocationRelativeTo(getOwner());
        setResizable(false);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        cartCombo = new JComboBox<>();
        productCombo = new JComboBox<>();
        loadCombos();

        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));

        if (existingItem != null) {
            quantitySpinner.setValue(existingItem.getQuantity());
            cartCombo.setEnabled(false); // Composite primary key cannot be altered
            productCombo.setEnabled(false);

            for (int i = 0; i < cartCombo.getItemCount(); i++) {
                if (cartCombo.getItemAt(i).getId().equals(existingItem.getCartId())) {
                    cartCombo.setSelectedIndex(i);
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
        addFormField(form, gbc, row++, "Shopping Cart *:", cartCombo);
        addFormField(form, gbc, row++, "Product *:", productCombo);
        addFormField(form, gbc, row++, "Quantity *:", quantitySpinner);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Separator.foreground")));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = new JButton(existingItem == null ? "Add to Cart" : "Update Quantity");
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
            List<ComboItem<Integer>> carts = cartDao.findAllAsComboItems();
            for (ComboItem<Integer> c : carts) cartCombo.addItem(c);

            List<ComboItem<Integer>> prods = productDao.findAllAsComboItems();
            for (ComboItem<Integer> p : prods) productCombo.addItem(p);
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
            ComboItem<Integer> c = (ComboItem<Integer>) cartCombo.getSelectedItem();
            ComboItem<Integer> p = (ComboItem<Integer>) productCombo.getSelectedItem();

            if (c == null || p == null) {
                NotificationUtils.showWarning(this, "Please select both a cart and a product.");
                return;
            }

            CartItem item = existingItem != null ? existingItem : new CartItem();
            item.setCartId(c.getId());
            item.setProductId(p.getId());
            item.setQuantity((Integer) quantitySpinner.getValue());

            ValidationService.validateCartItem(item);

            if (existingItem == null) {
                cartItemDao.insert(item);
                NotificationUtils.showSuccess(this, "Item added to cart #" + item.getCartId() + " successfully!");
            } else {
                cartItemDao.update(item);
                NotificationUtils.showSuccess(this, "Cart item updated successfully!");
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
