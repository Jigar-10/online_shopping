package ui.customer;

import dao.CategoryDao;
import dao.ProductDao;
import model.Category;
import model.Customer;
import model.Product;
import service.CustomerPortalService;
import ui.util.ImageUtils;
import ui.util.NotificationUtils;
import ui.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ProductCatalogPanel extends JPanel {
    private final ProductDao productDao = new ProductDao();
    private final CategoryDao categoryDao = new CategoryDao();
    private final CustomerPortalService portalService;
    private final java.util.function.Supplier<Customer> customerSupplier;
    private final Runnable onCartUpdated;

    private JPanel gridPanel;
    private JComboBox<String> categoryFilter;
    private JTextField searchField;
    private JComboBox<String> sortCombo;
    private JLabel countLabel;
    private JProgressBar progressBar;

    private List<Product> allProducts = new ArrayList<>();

    public ProductCatalogPanel(CustomerPortalService portalService,
                               java.util.function.Supplier<Customer> customerSupplier,
                               Runnable onCartUpdated) {
        this.portalService = portalService;
        this.customerSupplier = customerSupplier;
        this.onCartUpdated = onCartUpdated;
        initUI();
        loadCatalogAsync();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(14, 20, 14, 20));

        JPanel northContainer = new JPanel(new BorderLayout(0, 10));
        northContainer.setOpaque(false);

        // Storefront Hero Promo Banner
        JPanel bannerCard = new JPanel(new BorderLayout());
        bannerCard.setBackground(UITheme.CARD_BG);
        bannerCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_GRAY, 1),
                new EmptyBorder(4, 4, 4, 4)
        ));
        JLabel bannerLabel = new JLabel(ImageUtils.getBannerImage(960, 130));
        bannerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        bannerCard.add(bannerLabel, BorderLayout.CENTER);
        northContainer.add(bannerCard, BorderLayout.NORTH);

        // Top Filter & Search Bar
        JPanel topBar = new JPanel(new BorderLayout(14, 8));
        topBar.setOpaque(false);

        JPanel leftFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftFilters.setOpaque(false);

        categoryFilter = new JComboBox<>();
        categoryFilter.setPreferredSize(new Dimension(160, 32));
        loadCategories();
        categoryFilter.addActionListener(e -> filterAndRender());

        sortCombo = new JComboBox<>(new String[]{
                "Featured",
                "Price: Low to High",
                "Price: High to Low",
                "Name: A to Z"
        });
        sortCombo.setPreferredSize(new Dimension(160, 32));
        sortCombo.addActionListener(e -> filterAndRender());

        leftFilters.add(new JLabel("Category:"));
        leftFilters.add(categoryFilter);
        leftFilters.add(new JLabel("Sort by:"));
        leftFilters.add(sortCombo);

        JPanel rightSearch = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightSearch.setOpaque(false);

        searchField = new JTextField(18);
        searchField.setPreferredSize(new Dimension(200, 32));
        searchField.putClientProperty("JTextField.placeholderText", "Search products...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterAndRender(); }
            @Override public void removeUpdate(DocumentEvent e) { filterAndRender(); }
            @Override public void changedUpdate(DocumentEvent e) { filterAndRender(); }
        });

        JButton refreshBtn = new JButton("🔄 Refresh");
        refreshBtn.setPreferredSize(new Dimension(95, 32));
        refreshBtn.addActionListener(e -> loadCatalogAsync());

        rightSearch.add(searchField);
        rightSearch.add(refreshBtn);

        topBar.add(leftFilters, BorderLayout.WEST);
        topBar.add(rightSearch, BorderLayout.EAST);
        northContainer.add(topBar, BorderLayout.SOUTH);

        add(northContainer, BorderLayout.NORTH);

        // Product Cards Grid
        gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(0, 3, 16, 16));
        gridPanel.setBackground(UITheme.BG_MAIN);
        gridPanel.setBorder(new EmptyBorder(10, 4, 10, 4));

        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(UITheme.BG_MAIN);
        scrollPane.getViewport().setBackground(UITheme.BG_MAIN);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Footer Bar
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        countLabel = new JLabel("Loading products...");
        countLabel.setFont(UITheme.FONT_SMALL);
        countLabel.setForeground(UITheme.TEXT_MUTED);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(100, 14));
        progressBar.setVisible(false);

        footer.add(countLabel, BorderLayout.WEST);
        footer.add(progressBar, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);
    }

    private void loadCategories() {
        categoryFilter.removeAllItems();
        categoryFilter.addItem("All Categories");
        try {
            for (Category c : categoryDao.findAll()) {
                categoryFilter.addItem(c.getName());
            }
        } catch (Exception e) {
            // Ignored if offline
        }
    }

    public void reload() {
        loadCatalogAsync();
    }

    public void loadCatalogAsync() {
        progressBar.setVisible(true);
        countLabel.setText("Fetching catalog from MySQL...");

        new SwingWorker<List<Product>, Void>() {
            @Override
            protected List<Product> doInBackground() {
                return productDao.findAll();
            }

            @Override
            protected void done() {
                progressBar.setVisible(false);
                try {
                    allProducts = get();
                    loadCategories();
                    filterAndRender();
                } catch (Exception e) {
                    countLabel.setText("Failed to load products: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void filterAndRender() {
        String query = searchField.getText().trim().toLowerCase();
        String selectedCat = (String) categoryFilter.getSelectedItem();
        boolean filterCat = selectedCat != null && !selectedCat.equals("All Categories");

        List<Product> filtered = new ArrayList<>();
        for (Product p : allProducts) {
            boolean matchesSearch = query.isEmpty() ||
                    p.getName().toLowerCase().contains(query) ||
                    (p.getDescription() != null && p.getDescription().toLowerCase().contains(query));

            boolean matchesCategory = !filterCat ||
                    (p.getCategoryName() != null && p.getCategoryName().equalsIgnoreCase(selectedCat));

            if (matchesSearch && matchesCategory) {
                filtered.add(p);
            }
        }

        // Sorting
        int sortIdx = sortCombo.getSelectedIndex();
        if (sortIdx == 1) { // Low to High
            filtered.sort(Comparator.comparing(Product::getPrice));
        } else if (sortIdx == 2) { // High to Low
            filtered.sort(Comparator.comparing(Product::getPrice).reversed());
        } else if (sortIdx == 3) { // A to Z
            filtered.sort(Comparator.comparing(Product::getName));
        }

        // Render Cards
        gridPanel.removeAll();
        for (Product p : filtered) {
            gridPanel.add(createProductCard(p));
        }

        countLabel.setText("Showing " + filtered.size() + " of " + allProducts.size() + " products");
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JPanel createProductCard(Product p) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(UITheme.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_GRAY, 1),
                new EmptyBorder(14, 16, 14, 16)
        ));

        // Top info (Category Pill & Stock)
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JLabel catBadge = new JLabel(p.getCategoryName() != null ? p.getCategoryName() : "General");
        catBadge.setFont(UITheme.FONT_SMALL);
        catBadge.setForeground(UITheme.PRIMARY);
        catBadge.setOpaque(true);
        catBadge.setBackground(UITheme.PRIMARY_LIGHT);
        catBadge.setBorder(new EmptyBorder(2, 6, 2, 6));

        boolean inStock = p.getStockQty() != null && p.getStockQty() > 0;
        JLabel stockPill = new JLabel(inStock ? p.getStockQty() + " in stock" : "Out of stock");
        stockPill.setFont(UITheme.FONT_SMALL);
        stockPill.setForeground(inStock ? UITheme.SUCCESS : UITheme.DANGER);

        topRow.add(catBadge, BorderLayout.WEST);
        topRow.add(stockPill, BorderLayout.EAST);
        card.add(topRow, BorderLayout.NORTH);

        // Center info (Image, Name, Supplier, Description)
        JPanel centerInfo = new JPanel();
        centerInfo.setLayout(new BoxLayout(centerInfo, BoxLayout.Y_AXIS));
        centerInfo.setOpaque(false);

        JLabel imgLbl = new JLabel(ImageUtils.getProductImage(p, 190, 125));
        imgLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        imgLbl.setHorizontalAlignment(SwingConstants.CENTER);
        imgLbl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_GRAY, 1),
                new EmptyBorder(4, 4, 4, 4)
        ));

        JLabel nameLbl = new JLabel(p.getName());
        nameLbl.setFont(UITheme.FONT_HEADER);
        nameLbl.setForeground(UITheme.TEXT_PRIMARY);
        nameLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel supLbl = new JLabel("by " + (p.getSupplierName() != null ? p.getSupplierName() : "Verified Supplier"));
        supLbl.setFont(UITheme.FONT_SMALL);
        supLbl.setForeground(UITheme.TEXT_MUTED);
        supLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        String desc = p.getDescription() != null ? p.getDescription() : "High quality item from catalog.";
        if (desc.length() > 65) desc = desc.substring(0, 62) + "...";
        JLabel descLbl = new JLabel("<html><body style='width: 170px;'>" + desc + "</body></html>");
        descLbl.setFont(UITheme.FONT_REGULAR);
        descLbl.setForeground(UITheme.TEXT_MUTED);
        descLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        centerInfo.add(Box.createVerticalStrut(6));
        centerInfo.add(imgLbl);
        centerInfo.add(Box.createVerticalStrut(8));
        centerInfo.add(nameLbl);
        centerInfo.add(Box.createVerticalStrut(2));
        centerInfo.add(supLbl);
        centerInfo.add(Box.createVerticalStrut(4));
        centerInfo.add(descLbl);
        card.add(centerInfo, BorderLayout.CENTER);

        // Bottom row (Price + Actions)
        JPanel bottomRow = new JPanel(new BorderLayout(6, 6));
        bottomRow.setOpaque(false);

        JLabel priceLbl = new JLabel("\u20B9 " + p.getPrice());
        priceLbl.setFont(new Font("Segoe UI", Font.BOLD, 17));
        priceLbl.setForeground(UITheme.TEXT_PRIMARY);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnPanel.setOpaque(false);

        JButton viewBtn = new JButton("Details");
        viewBtn.setFont(UITheme.FONT_SMALL);
        viewBtn.addActionListener(e -> {
            Window win = SwingUtilities.getWindowAncestor(this);
            ProductDetailDialog dlg = new ProductDetailDialog(win, p, customerSupplier.get(), portalService, onCartUpdated);
            dlg.setVisible(true);
        });

        JButton addBtn = new JButton("+ Cart");
        addBtn.setFont(UITheme.FONT_BOLD);
        addBtn.setBackground(UITheme.PRIMARY);
        addBtn.setForeground(Color.WHITE);
        addBtn.setEnabled(inStock);
        addBtn.addActionListener(e -> {
            Customer cust = customerSupplier.get();
            if (cust == null) {
                NotificationUtils.showWarning(this, "Please select an active customer account to shop.");
                return;
            }
            var cart = portalService.getOrCreateCart(cust.getCustomerId());
            portalService.addItemToCart(cart.getCartId(), p.getProductId(), 1);
            NotificationUtils.showSuccess(this, p.getName() + " added to your cart!");
            if (onCartUpdated != null) onCartUpdated.run();
        });

        btnPanel.add(viewBtn);
        btnPanel.add(addBtn);

        bottomRow.add(priceLbl, BorderLayout.WEST);
        bottomRow.add(btnPanel, BorderLayout.EAST);
        card.add(bottomRow, BorderLayout.SOUTH);

        return card;
    }
}
