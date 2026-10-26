package ui.dialog;

import dao.CustomerDao;
import dao.ProductDao;
import dao.ReviewDao;
import model.ComboItem;
import model.Review;
import service.ValidationService;
import ui.util.NotificationUtils;
import ui.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class ReviewDialog extends JDialog {
    private final ReviewDao reviewDao = new ReviewDao();
    private final CustomerDao customerDao = new CustomerDao();
    private final ProductDao productDao = new ProductDao();
    private final Review existingReview;
    private boolean saved = false;

    private JComboBox<ComboItem<Integer>> customerCombo;
    private JComboBox<ComboItem<Integer>> productCombo;
    private JComboBox<String> ratingCombo;
    private JTextArea commentArea;

    public ReviewDialog(Window owner, Review review) {
        super(owner, review == null ? "Write Product Review" : "Edit Review #" + review.getReviewId(), ModalityType.APPLICATION_MODAL);
        this.existingReview = review;
        initUI();
    }

    private void initUI() {
        setSize(480, 400);
        setLocationRelativeTo(getOwner());
        setResizable(false);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        customerCombo = new JComboBox<>();
        productCombo = new JComboBox<>();
        loadCombos();

        ratingCombo = new JComboBox<>(new String[]{
                "5 Stars - Excellent (\u2605\u2605\u2605\u2605\u2605)",
                "4 Stars - Good (\u2605\u2605\u2605\u2605\u2606)",
                "3 Stars - Average (\u2605\u2605\u2605\u2606\u2606)",
                "2 Stars - Poor (\u2605\u2605\u2606\u2606\u2606)",
                "1 Star - Terrible (\u2605\u2606\u2606\u2606\u2606)"
        });

        commentArea = new JTextArea(4, 20);
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        JScrollPane commentScroll = new JScrollPane(commentArea);

        if (existingReview != null) {
            commentArea.setText(existingReview.getComment());
            int idx = 5 - existingReview.getRating();
            if (idx >= 0 && idx < ratingCombo.getItemCount()) {
                ratingCombo.setSelectedIndex(idx);
            }

            for (int i = 0; i < customerCombo.getItemCount(); i++) {
                if (customerCombo.getItemAt(i).getId().equals(existingReview.getCustomerId())) {
                    customerCombo.setSelectedIndex(i);
                    break;
                }
            }

            for (int i = 0; i < productCombo.getItemCount(); i++) {
                if (productCombo.getItemAt(i).getId().equals(existingReview.getProductId())) {
                    productCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        int row = 0;
        addFormField(form, gbc, row++, "Customer *:", customerCombo);
        addFormField(form, gbc, row++, "Product *:", productCombo);
        addFormField(form, gbc, row++, "Rating *:", ratingCombo);
        addFormField(form, gbc, row++, "Comment:", commentScroll);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Separator.foreground")));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = new JButton(existingReview == null ? "Submit Review" : "Save Changes");
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
            List<ComboItem<Integer>> custs = customerDao.findAllAsComboItems();
            for (ComboItem<Integer> c : custs) customerCombo.addItem(c);

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
            ComboItem<Integer> cust = (ComboItem<Integer>) customerCombo.getSelectedItem();
            ComboItem<Integer> prod = (ComboItem<Integer>) productCombo.getSelectedItem();

            if (cust == null || prod == null) {
                NotificationUtils.showWarning(this, "Please select both a customer and a product.");
                return;
            }

            int rating = 5 - ratingCombo.getSelectedIndex();

            Review r = existingReview != null ? existingReview : new Review();
            r.setCustomerId(cust.getId());
            r.setProductId(prod.getId());
            r.setRating(rating);
            r.setComment(commentArea.getText().trim());

            ValidationService.validateReview(r);

            if (existingReview == null) {
                reviewDao.insert(r);
                NotificationUtils.showSuccess(this, "Review submitted successfully with ID #" + r.getReviewId() + "!");
            } else {
                reviewDao.update(r);
                NotificationUtils.showSuccess(this, "Review #" + r.getReviewId() + " updated successfully!");
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
