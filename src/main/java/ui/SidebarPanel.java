package ui;

import ui.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SidebarPanel extends JPanel {
    public record NavItem(String id, String label, String icon) {}

    private final Consumer<String> onNavigate;
    private final List<JButton> buttons = new ArrayList<>();
    private String activeId = "dashboard";

    public SidebarPanel(Consumer<String> onNavigate) {
        this.onNavigate = onNavigate;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(240, 700));
        setBackground(UITheme.isDarkMode() ? UITheme.SIDEBAR_DARK : UITheme.SIDEBAR_LIGHT);

        // Brand / Logo Top Panel
        JPanel brandPanel = new JPanel();
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setOpaque(false);
        brandPanel.setBorder(new EmptyBorder(20, 20, 16, 20));

        JLabel brandTitle = new JLabel("ONLINE SHOPPING");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        brandTitle.setForeground(UITheme.PRIMARY);

        JLabel brandSubtitle = new JLabel("DBMS 8.0 Studio");
        brandSubtitle.setFont(UITheme.FONT_SMALL);
        brandSubtitle.setForeground(Color.GRAY);

        brandPanel.add(brandTitle);
        brandPanel.add(Box.createVerticalStrut(2));
        brandPanel.add(brandSubtitle);

        add(brandPanel, BorderLayout.NORTH);

        // Menu items in a scrollable panel
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setOpaque(false);
        menuPanel.setBorder(new EmptyBorder(6, 12, 16, 12));

        addSectionHeader(menuPanel, "MAIN");
        addNavButton(menuPanel, "dashboard", "Dashboard", "📊");

        addSectionHeader(menuPanel, "CUSTOMERS & SHIPPING");
        addNavButton(menuPanel, "customers", "Customers", "👥");
        addNavButton(menuPanel, "addresses", "Addresses", "🏠");
        addNavButton(menuPanel, "shipping", "Shipping", "🚚");

        addSectionHeader(menuPanel, "CATALOG");
        addNavButton(menuPanel, "categories", "Categories", "🏷️");
        addNavButton(menuPanel, "suppliers", "Suppliers", "🏭");
        addNavButton(menuPanel, "products", "Products", "📦");

        addSectionHeader(menuPanel, "CARTS & ORDERS");
        addNavButton(menuPanel, "carts", "Shopping Carts", "🛒");
        addNavButton(menuPanel, "cart_items", "Cart Items", "🛍️");
        addNavButton(menuPanel, "orders", "Orders", "📑");
        addNavButton(menuPanel, "order_items", "Order Items", "📋");

        addSectionHeader(menuPanel, "FINANCE & REVIEWS");
        addNavButton(menuPanel, "payments", "Payments", "💳");
        addNavButton(menuPanel, "credit_cards", "Credit Cards", "💳");
        addNavButton(menuPanel, "digital_wallets", "Digital Wallets", "📱");
        addNavButton(menuPanel, "reviews", "Reviews", "⭐");

        addSectionHeader(menuPanel, "DATABASE & HELP");
        addNavButton(menuPanel, "views", "Database Views", "🔍");
        addNavButton(menuPanel, "connection", "Connection Config", "⚙️");
        addNavButton(menuPanel, "about", "About & Help", "ℹ️");

        JScrollPane scrollPane = new JScrollPane(menuPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        add(scrollPane, BorderLayout.CENTER);

        // Exit at bottom
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(10, 12, 14, 12));

        JButton exitBtn = new JButton("🚪 Exit Application");
        exitBtn.setHorizontalAlignment(SwingConstants.LEFT);
        exitBtn.setFont(UITheme.FONT_BOLD);
        exitBtn.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Are you sure you want to exit?", "Exit", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        bottomPanel.add(exitBtn, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setActive("dashboard");
    }

    private void addSectionHeader(JPanel panel, String title) {
        panel.add(Box.createVerticalStrut(10));
        JLabel header = new JLabel(title);
        header.setFont(new Font("Segoe UI", Font.BOLD, 10));
        header.setForeground(new Color(148, 163, 184)); // Slate 400
        header.setBorder(new EmptyBorder(4, 10, 4, 10));
        panel.add(header);
        panel.add(Box.createVerticalStrut(2));
    }

    private void addNavButton(JPanel panel, String id, String text, String icon) {
        JButton btn = new JButton(icon + "  " + text);
        btn.setActionCommand(id);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFont(UITheme.FONT_REGULAR);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btn.setPreferredSize(new Dimension(200, 36));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(6, 12, 6, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            setActive(id);
            if (onNavigate != null) {
                onNavigate.accept(id);
            }
        });

        buttons.add(btn);
        panel.add(btn);
        panel.add(Box.createVerticalStrut(2));
    }

    public void setActive(String id) {
        this.activeId = id;
        for (JButton b : buttons) {
            boolean active = b.getActionCommand().equals(id);
            if (active) {
                b.setBackground(UITheme.PRIMARY);
                b.setForeground(Color.WHITE);
                b.setFont(UITheme.FONT_BOLD);
            } else {
                b.setBackground(null);
                b.setForeground(null);
                b.setFont(UITheme.FONT_REGULAR);
            }
        }
    }
}
