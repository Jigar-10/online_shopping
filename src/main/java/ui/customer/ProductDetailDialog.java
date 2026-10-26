package ui.customer;

import dao.ReviewDao;
import model.Customer;
import model.Product;
import model.Review;
import service.CustomerPortalService;
import ui.util.ImageUtils;
import ui.util.NotificationUtils;
import ui.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class ProductDetailDialog extends JDialog {
    private final Product product;
    private final Customer currentCustomer;
    private final CustomerPortalService portalService;
    private final Runnable onCartUpdated;

    private final ReviewDao reviewDao = new ReviewDao();
    private JPanel reviewsListPanel;
    private JComboBox<Integer> ratingCombo;
    private JTextArea commentField;

    public ProductDetailDialog(Window owner, Product product, Customer currentCustomer,
                               CustomerPortalService portalService, Runnable onCartUpdated) {
        super(owner, product.getName() + " - Product Details", ModalityType.APPLICATION_MODAL);
        this.product = product;
        this.currentCustomer = currentCustomer;
        this.portalService = portalService;
        this.onCartUpdated = onCartUpdated;
        initUI();
    }

    private void initUI() {
        setSize(700, 700);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(new EmptyBorder(20, 24, 20, 24));
        root.setBackground(UITheme.CARD_BG);

        // Product Header Card with Product Image
        JPanel topHeroPanel = new JPanel(new BorderLayout(16, 0));
        topHeroPanel.setOpaque(false);

        JLabel imgLbl = new JLabel(ImageUtils.getProductImage(product, 160, 130));
        imgLbl.setHorizontalAlignment(SwingConstants.CENTER);
        imgLbl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_GRAY, 1),
                new EmptyBorder(4, 4, 4, 4)
        ));
        topHeroPanel.add(imgLbl, BorderLayout.WEST);

        JPanel headerCard = new JPanel(new BorderLayout(12, 10));
        headerCard.setOpaque(false);

        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setOpaque(false);

        JLabel nameLbl = new JLabel(product.getName());
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        nameLbl.setForeground(UITheme.TEXT_PRIMARY);

        JLabel metaLbl = new JLabel("Category: " + (product.getCategoryName() != null ? product.getCategoryName() : "General") +
                                  "  |  Supplier: " + (product.getSupplierName() != null ? product.getSupplierName() : "Verified Supplier"));
        metaLbl.setFont(UITheme.FONT_REGULAR);
        metaLbl.setForeground(UITheme.TEXT_MUTED);

        titles.add(nameLbl);
        titles.add(Box.createVerticalStrut(4));
        titles.add(metaLbl);
        headerCard.add(titles, BorderLayout.WEST);

        // Price & Stock
        JPanel priceStockPanel = new JPanel();
        priceStockPanel.setLayout(new BoxLayout(priceStockPanel, BoxLayout.Y_AXIS));
        priceStockPanel.setOpaque(false);

        JLabel priceLbl = new JLabel("\u20B9 " + product.getPrice());
        priceLbl.setFont(new Font("Segoe UI", Font.BOLD, 24));
        priceLbl.setForeground(UITheme.PRIMARY);

        boolean inStock = product.getStockQty() != null && product.getStockQty() > 0;
        JLabel stockLbl = new JLabel(inStock ? "\u25CF In Stock (" + product.getStockQty() + ")" : "\u25CF Out of Stock");
        stockLbl.setFont(UITheme.FONT_BOLD);
        stockLbl.setForeground(inStock ? UITheme.SUCCESS : UITheme.DANGER);

        priceStockPanel.add(priceLbl);
        priceStockPanel.add(Box.createVerticalStrut(4));
        priceStockPanel.add(stockLbl);
        headerCard.add(priceStockPanel, BorderLayout.EAST);

        topHeroPanel.add(headerCard, BorderLayout.CENTER);
        root.add(topHeroPanel);
        root.add(Box.createVerticalStrut(14));

        // Description
        if (product.getDescription() != null && !product.getDescription().isBlank()) {
            JPanel descPanel = new JPanel(new BorderLayout(4, 4));
            descPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UITheme.BORDER_GRAY, 1),
                    new EmptyBorder(10, 12, 10, 12)
            ));
            descPanel.setBackground(UITheme.BG_MAIN);

            JLabel descTitle = new JLabel("Description & Features");
            descTitle.setFont(UITheme.FONT_BOLD);

            JTextArea descText = new JTextArea(product.getDescription());
            descText.setFont(UITheme.FONT_REGULAR);
            descText.setLineWrap(true);
            descText.setWrapStyleWord(true);
            descText.setEditable(false);
            descText.setOpaque(false);

            descPanel.add(descTitle, BorderLayout.NORTH);
            descPanel.add(descText, BorderLayout.CENTER);
            root.add(descPanel);
            root.add(Box.createVerticalStrut(16));
        }

        // Add to Cart Action Bar
        JPanel buyBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buyBar.setOpaque(false);

        JLabel qtyLabel = new JLabel("Quantity:");
        qtyLabel.setFont(UITheme.FONT_BOLD);
        JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, Math.max(1, product.getStockQty()), 1));
        qtySpinner.setPreferredSize(new Dimension(65, 32));

        JButton addCartBtn = new JButton("🛒 Add to Shopping Cart");
        addCartBtn.setBackground(UITheme.PRIMARY);
        addCartBtn.setForeground(Color.WHITE);
        addCartBtn.setFont(UITheme.FONT_BOLD);
        addCartBtn.setPreferredSize(new Dimension(190, 34));
        addCartBtn.setEnabled(inStock);

        addCartBtn.addActionListener(e -> {
            if (currentCustomer == null) {
                NotificationUtils.showWarning(this, "Please select an active customer profile to shop.");
                return;
            }
            int qty = (Integer) qtySpinner.getValue();
            var cart = portalService.getOrCreateCart(currentCustomer.getCustomerId());
            portalService.addItemToCart(cart.getCartId(), product.getProductId(), qty);
            NotificationUtils.showSuccess(this, "Added " + qty + " \u00D7 " + product.getName() + " to your cart!");
            if (onCartUpdated != null) onCartUpdated.run();
        });

        buyBar.add(qtyLabel);
        buyBar.add(qtySpinner);
        buyBar.add(addCartBtn);
        root.add(buyBar);
        root.add(Box.createVerticalStrut(18));

        // Reviews Section
        JLabel revTitle = new JLabel("Customer Reviews & Feedback");
        revTitle.setFont(UITheme.FONT_HEADER);
        root.add(revTitle);
        root.add(Box.createVerticalStrut(8));

        reviewsListPanel = new JPanel();
        reviewsListPanel.setLayout(new BoxLayout(reviewsListPanel, BoxLayout.Y_AXIS));
        reviewsListPanel.setOpaque(false);
        loadReviews();

        JScrollPane reviewsScroll = new JScrollPane(reviewsListPanel);
        reviewsScroll.setPreferredSize(new Dimension(580, 160));
        reviewsScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_GRAY, 1));
        reviewsScroll.getVerticalScrollBar().setUnitIncrement(12);
        root.add(reviewsScroll);
        root.add(Box.createVerticalStrut(14));

        // Write a review box
        JPanel writeRevPanel = new JPanel(new BorderLayout(8, 6));
        writeRevPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_GRAY, 1),
                new EmptyBorder(10, 12, 10, 12)
        ));
        writeRevPanel.setBackground(UITheme.BG_MAIN);

        JPanel writeTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        writeTop.setOpaque(false);
        writeTop.add(new JLabel("Your Rating:"));
        ratingCombo = new JComboBox<>(new Integer[]{5, 4, 3, 2, 1});
        writeTop.add(ratingCombo);
        writeTop.add(new JLabel("Stars"));

        commentField = new JTextArea(2, 25);
        commentField.setFont(UITheme.FONT_REGULAR);
        commentField.setLineWrap(true);

        JButton postReviewBtn = new JButton("Post Review");
        postReviewBtn.setFont(UITheme.FONT_BOLD);
        postReviewBtn.addActionListener(e -> {
            if (currentCustomer == null) {
                NotificationUtils.showWarning(this, "Please select an active customer first.");
                return;
            }
            String comment = commentField.getText().trim();
            int rating = (Integer) ratingCombo.getSelectedItem();
            Review r = new Review(null, currentCustomer.getCustomerId(), product.getProductId(), rating, comment);
            reviewDao.insert(r);
            commentField.setText("");
            NotificationUtils.showSuccess(this, "Thank you! Your review has been saved.");
            loadReviews();
        });

        writeRevPanel.add(writeTop, BorderLayout.NORTH);
        writeRevPanel.add(new JScrollPane(commentField), BorderLayout.CENTER);
        writeRevPanel.add(postReviewBtn, BorderLayout.EAST);
        root.add(writeRevPanel);

        JScrollPane mainScroll = new JScrollPane(root);
        mainScroll.setBorder(BorderFactory.createEmptyBorder());
        add(mainScroll, BorderLayout.CENTER);

        JPanel closeBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 10));
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());
        closeBar.add(closeBtn);
        add(closeBar, BorderLayout.SOUTH);
    }

    private void loadReviews() {
        reviewsListPanel.removeAll();
        List<Review> reviews = portalService.getProductReviews(product.getProductId());
        if (reviews.isEmpty()) {
            JLabel noRev = new JLabel("No reviews yet for this product. Be the first to review!");
            noRev.setFont(UITheme.FONT_SMALL);
            noRev.setForeground(UITheme.TEXT_MUTED);
            noRev.setBorder(new EmptyBorder(12, 12, 12, 12));
            reviewsListPanel.add(noRev);
        } else {
            for (Review r : reviews) {
                JPanel card = new JPanel(new BorderLayout(4, 2));
                card.setBorder(new EmptyBorder(8, 10, 8, 10));
                card.setBackground(Color.WHITE);

                JLabel header = new JLabel((r.getCustomerName() != null ? r.getCustomerName() : "Customer") + "  \u2022  " + r.getStars());
                header.setFont(UITheme.FONT_BOLD);
                header.setForeground(UITheme.WARNING);

                JLabel comment = new JLabel("<html>" + (r.getComment() != null ? r.getComment() : "") + "</html>");
                comment.setFont(UITheme.FONT_REGULAR);
                comment.setForeground(UITheme.TEXT_PRIMARY);

                card.add(header, BorderLayout.NORTH);
                card.add(comment, BorderLayout.CENTER);
                reviewsListPanel.add(card);
                reviewsListPanel.add(new JSeparator());
            }
        }
        reviewsListPanel.revalidate();
        reviewsListPanel.repaint();
    }
}
