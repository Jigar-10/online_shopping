package ui;

import dao.DashboardDao;
import model.Order;
import ui.util.NotificationUtils;
import ui.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class DashboardPanel extends JPanel {
    private final DashboardDao dashboardDao = new DashboardDao();
    private final Runnable navigateToCustomers;
    private final Runnable navigateToProducts;
    private final Runnable navigateToOrders;
    private final Runnable openConnectionDialog;

    private JLabel customerStat;
    private JLabel productStat;
    private JLabel orderStat;
    private JLabel revenueStat;
    private JLabel pendingOrderStat;
    private JLabel pendingShipStat;
    private JLabel lowStockStat;

    private JTable recentOrdersTable;
    private DefaultTableModel ordersTableModel;
    private JLabel lastUpdatedLabel;
    private JProgressBar progressBar;

    public DashboardPanel(Runnable navigateToCustomers,
                          Runnable navigateToProducts,
                          Runnable navigateToOrders,
                          Runnable openConnectionDialog) {
        this.navigateToCustomers = navigateToCustomers;
        this.navigateToProducts = navigateToProducts;
        this.navigateToOrders = navigateToOrders;
        this.openConnectionDialog = openConnectionDialog;
        initUI();
        reload();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 16));
        setBorder(new EmptyBorder(18, 22, 18, 22));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel welcome = new JLabel("Online Shopping Management Dashboard");
        welcome.setFont(UITheme.FONT_TITLE);

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        lastUpdatedLabel = new JLabel("Loading...");
        lastUpdatedLabel.setFont(UITheme.FONT_SMALL);
        lastUpdatedLabel.setForeground(Color.GRAY);

        JButton refreshBtn = new JButton("🔄 Refresh Dashboard");
        refreshBtn.addActionListener(e -> reload());

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(80, 14));
        progressBar.setVisible(false);

        headerRight.add(progressBar);
        headerRight.add(lastUpdatedLabel);
        headerRight.add(refreshBtn);

        headerPanel.add(welcome, BorderLayout.WEST);
        headerPanel.add(headerRight, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Scrollable content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // 1. KPI Metric Cards (Grid of 4 top + 3 bottom)
        JPanel statsGrid = new JPanel(new GridLayout(2, 4, 14, 14));
        statsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        customerStat = new JLabel("0");
        productStat = new JLabel("0");
        orderStat = new JLabel("0");
        revenueStat = new JLabel("\u20B90");
        pendingOrderStat = new JLabel("0");
        pendingShipStat = new JLabel("0");
        lowStockStat = new JLabel("0");

        statsGrid.add(createCard("👥 Customers", customerStat, UITheme.INFO));
        statsGrid.add(createCard("📦 Products", productStat, UITheme.PRIMARY));
        statsGrid.add(createCard("📑 Total Orders", orderStat, new Color(139, 92, 246)));
        statsGrid.add(createCard("💰 Total Revenue", revenueStat, UITheme.SUCCESS));
        statsGrid.add(createCard("⏳ Pending Orders", pendingOrderStat, UITheme.WARNING));
        statsGrid.add(createCard("🚚 Pending Shipments", pendingShipStat, new Color(249, 115, 22)));
        statsGrid.add(createCard("⚠ Low Stock Alert", lowStockStat, UITheme.DANGER));
        statsGrid.add(createQuickActionsCard());

        contentPanel.add(statsGrid);
        contentPanel.add(Box.createVerticalStrut(20));

        // 2. Recent Orders & Quick Navigation Section
        JPanel bottomSection = new JPanel(new BorderLayout(14, 0));
        bottomSection.setBorder(BorderFactory.createTitledBorder("Recent Orders (Live MySQL Data)"));

        String[] cols = {"Order ID", "Customer", "Date", "Total Amount", "Status"};
        ordersTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        recentOrdersTable = new JTable(ordersTableModel);
        recentOrdersTable.setRowHeight(30);
        recentOrdersTable.getTableHeader().setFont(UITheme.FONT_BOLD);
        recentOrdersTable.setFont(UITheme.FONT_REGULAR);

        JScrollPane scrollPane = new JScrollPane(recentOrdersTable);
        scrollPane.setPreferredSize(new Dimension(600, 240));
        bottomSection.add(scrollPane, BorderLayout.CENTER);

        contentPanel.add(bottomSection);

        JScrollPane mainScroll = new JScrollPane(contentPanel);
        mainScroll.setBorder(BorderFactory.createEmptyBorder());
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScroll, BorderLayout.CENTER);
    }

    private JPanel createCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(6, 6));
        UITheme.styleCard(card);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UITheme.FONT_BOLD);
        titleLbl.setForeground(accentColor);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createQuickActionsCard() {
        JPanel card = new JPanel(new GridLayout(2, 2, 6, 6));
        UITheme.styleCard(card);

        JButton orderBtn = new JButton("+ Order");
        orderBtn.setToolTipText("Go to Orders module");
        orderBtn.addActionListener(e -> { if (navigateToOrders != null) navigateToOrders.run(); });

        JButton prodBtn = new JButton("+ Product");
        prodBtn.setToolTipText("Go to Products module");
        prodBtn.addActionListener(e -> { if (navigateToProducts != null) navigateToProducts.run(); });

        JButton custBtn = new JButton("+ Customer");
        custBtn.setToolTipText("Go to Customers module");
        custBtn.addActionListener(e -> { if (navigateToCustomers != null) navigateToCustomers.run(); });

        JButton dbBtn = new JButton("⚙ DB Config");
        dbBtn.setToolTipText("Open MySQL Connection Settings");
        dbBtn.addActionListener(e -> { if (openConnectionDialog != null) openConnectionDialog.run(); });

        card.add(orderBtn);
        card.add(prodBtn);
        card.add(custBtn);
        card.add(dbBtn);
        return card;
    }

    public void reload() {
        progressBar.setVisible(true);
        lastUpdatedLabel.setText("Fetching metrics...");

        new SwingWorker<DashboardDao.DashboardStats, Void>() {
            @Override
            protected DashboardDao.DashboardStats doInBackground() {
                return dashboardDao.getStats();
            }

            @Override
            protected void done() {
                progressBar.setVisible(false);
                try {
                    DashboardDao.DashboardStats stats = get();
                    customerStat.setText(String.valueOf(stats.totalCustomers()));
                    productStat.setText(String.valueOf(stats.totalProducts()));
                    orderStat.setText(String.valueOf(stats.totalOrders()));

                    NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
                    revenueStat.setText(nf.format(stats.totalRevenue() != null ? stats.totalRevenue() : BigDecimal.ZERO));

                    pendingOrderStat.setText(String.valueOf(stats.pendingOrders()));
                    pendingShipStat.setText(String.valueOf(stats.pendingShipments()));
                    lowStockStat.setText(String.valueOf(stats.lowStockCount()));

                    // Update recent orders table
                    ordersTableModel.setRowCount(0);
                    for (Order o : stats.recentOrders()) {
                        ordersTableModel.addRow(new Object[]{
                                "#" + o.getOrderId(),
                                o.getCustomerName() != null ? o.getCustomerName() : "Cust " + o.getCustomerId(),
                                o.getOrderDate() != null ? o.getOrderDate().toString().replace("T", " ") : "-",
                                "\u20B9" + o.getTotalAmount(),
                                o.getStatus()
                        });
                    }

                    lastUpdatedLabel.setText("Last updated: " + java.time.LocalTime.now().toString().substring(0, 8));
                } catch (Exception e) {
                    lastUpdatedLabel.setText("Offline (Connect to MySQL)");
                }
            }
        }.execute();
    }
}
