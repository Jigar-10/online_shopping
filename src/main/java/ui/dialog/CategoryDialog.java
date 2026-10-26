package ui.dialog;

import dao.CategoryDao;
import model.Category;
import service.ValidationService;
import ui.util.NotificationUtils;
import ui.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CategoryDialog extends JDialog {
    private final CategoryDao categoryDao = new CategoryDao();
    private final Category existingCategory;
    private boolean saved = false;

    private JTextField nameField;

    public CategoryDialog(Window owner, Category category) {
        super(owner, category == null ? "Create New Category" : "Edit Category #" + category.getCategoryId(), ModalityType.APPLICATION_MODAL);
        this.existingCategory = category;
        initUI();
    }

    private void initUI() {
        setSize(400, 220);
        setLocationRelativeTo(getOwner());
        setResizable(false);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        nameField = new JTextField(20);
        if (existingCategory != null) {
            nameField.setText(existingCategory.getName());
        }

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.35;
        JLabel label = new JLabel("Category Name *:");
        label.setFont(UITheme.FONT_BOLD);
        form.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        form.add(nameField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Separator.foreground")));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = new JButton(existingCategory == null ? "Save Category" : "Save Changes");
        saveBtn.setBackground(UITheme.PRIMARY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(UITheme.FONT_BOLD);
        saveBtn.addActionListener(e -> onSave());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        add(form, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void onSave() {
        try {
            Category c = existingCategory != null ? existingCategory : new Category();
            c.setName(nameField.getText().trim());

            ValidationService.validateCategory(c);

            if (existingCategory == null) {
                categoryDao.insert(c);
                NotificationUtils.showSuccess(this, "Category created with ID #" + c.getCategoryId() + "!");
            } else {
                categoryDao.update(c);
                NotificationUtils.showSuccess(this, "Category #" + c.getCategoryId() + " updated successfully!");
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
