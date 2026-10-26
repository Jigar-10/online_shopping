package ui.dialog;

import dao.CategoryDao;
import dao.ProductDao;
import dao.SupplierDao;
import model.ComboItem;
import model.Product;
import service.ValidationService;
import ui.util.NotificationUtils;
import ui.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class ProductDialog extends JDialog {
    private final ProductDao productDao = new ProductDao();
    private final CategoryDao categoryDao = new CategoryDao();
    private final SupplierDao supplierDao = new SupplierDao();
    private final Product existingProduct;
    private boolean saved = false;

    private JComboBox<ComboItem<Integer>> categoryCombo;
    private JComboBox<ComboItem<Integer>> supplierCombo;
    private JTextField nameField;
    private JTextArea descriptionArea;
    private JSpinner stockSpinner;
    private JTextField priceField;

    public ProductDialog(Window owner, Product product) {
        super(owner, product == null ? "Add New Product" : "Edit Product #" + product.getProductId(), ModalityType.APPLICATION_MODAL);
        this.existingProduct = product;
        initUI();
    }

    private void initUI() {
        setSize(500, 480);
        setLocationRelativeTo(getOwner());
        setResizable(false);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        categoryCombo = new JComboBox<>();
        loadCategories();

        supplierCombo = new JComboBox<>();
        loadSuppliers();

        nameField = new JTextField(20);
        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descriptionArea);

        stockSpinner = new JSpinner(new SpinnerNumberModel(10, 0, 100000, 1));
        priceField = new JTextField("999.00", 20);

        if (existingProduct != null) {
            nameField.setText(existingProduct.getName());
            descriptionArea.setText(existingProduct.getDescription());
            stockSpinner.setValue(existingProduct.getStockQty());
            priceField.setText(existingProduct.getPrice().toString());

            for (int i = 0; i < categoryCombo.getItemCount(); i++) {
                if (categoryCombo.getItemAt(i).getId().equals(existingProduct.getCategoryId())) {
                    categoryCombo.setSelectedIndex(i);
                    break;
                }
            }

            for (int i = 0; i < supplierCombo.getItemCount(); i++) {
                if (supplierCombo.getItemAt(i).getId().equals(existingProduct.getSupplierId())) {
                    supplierCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        int row = 0;
        addFormField(form, gbc, row++, "Product Name *:", nameField);
        addFormField(form, gbc, row++, "Category *:", categoryCombo);
        addFormField(form, gbc, row++, "Supplier *:", supplierCombo);
        addFormField(form, gbc, row++, "Price (\u20B9) *:", priceField);
        addFormField(form, gbc, row++, "Stock Quantity *:", stockSpinner);
        addFormField(form, gbc, row++, "Description:", descScroll);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Separator.foreground")));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = new JButton(existingProduct == null ? "Add Product" : "Save Changes");
        saveBtn.setBackground(UITheme.PRIMARY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(UITheme.FONT_BOLD);
        saveBtn.addActionListener(e -> onSave());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        add(form, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadCategories() {
        try {
            List<ComboItem<Integer>> items = categoryDao.findAllAsComboItems();
            for (ComboItem<Integer> it : items) {
                categoryCombo.addItem(it);
            }
        } catch (Exception e) {
            NotificationUtils.showError(this, "Could not load categories", e);
        }
    }

    private void loadSuppliers() {
        try {
            List<ComboItem<Integer>> items = supplierDao.findAllAsComboItems();
            for (ComboItem<Integer> it : items) {
                supplierCombo.addItem(it);
            }
        } catch (Exception e) {
            NotificationUtils.showError(this, "Could not load suppliers", e);
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
            ComboItem<Integer> cat = (ComboItem<Integer>) categoryCombo.getSelectedItem();
            ComboItem<Integer> sup = (ComboItem<Integer>) supplierCombo.getSelectedItem();

            if (cat == null) {
                NotificationUtils.showWarning(this, "Please select a category.");
                return;
            }
            if (sup == null) {
                NotificationUtils.showWarning(this, "Please select a supplier.");
                return;
            }

            BigDecimal price;
            try {
                price = new BigDecimal(priceField.getText().trim());
            } catch (NumberFormatException e) {
                NotificationUtils.showWarning(this, "Please enter a valid price (e.g. 799.00).");
                return;
            }

            Product p = existingProduct != null ? existingProduct : new Product();
            p.setName(nameField.getText().trim());
            p.setCategoryId(cat.getId());
            p.setSupplierId(sup.getId());
            p.setPrice(price);
            p.setStockQty((Integer) stockSpinner.getValue());
            p.setDescription(descriptionArea.getText().trim());

            ValidationService.validateProduct(p);

            if (existingProduct == null) {
                productDao.insert(p);
                NotificationUtils.showSuccess(this, "Product created with ID #" + p.getProductId() + "!");
            } else {
                productDao.update(p);
                NotificationUtils.showSuccess(this, "Product #" + p.getProductId() + " updated successfully!");
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
