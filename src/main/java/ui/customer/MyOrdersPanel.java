package ui.customer;

import dao.OrderItemDao;
import model.Customer;
import model.Order;
import model.OrderItem;
import service.CustomerPortalService;
import ui.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MyOrdersPanel extends JPanel {
    private final CustomerPortalService portalService;
    private final java.util.function.Supplier<Customer> customerSupplier;
    private final OrderItemDao orderItemDao = new OrderItemDao();

    private DefaultTableModel ordersModel;
    private JTable ordersTable;
    private DefaultTableModel itemsModel;
    private JTable itemsTable;
    private JLabel orderInfoLabel;

    private List<Order> customerOrders = new ArrayList<>();

    public MyOrdersPanel(CustomerPortalService portalService,
                         java.util.function.Supplier<Customer> customerSupplier) {
        this.portalService = portalService;
        this.customerSupplier = customerSupplier;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 14));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("My Orders & Tracking");
        title.setFont(UITheme.FONT_TITLE);
        header.add(title, BorderLayout.WEST);

        JButton refreshBtn = new JButton("🔄 Refresh Orders");
        refreshBtn.addActionListener(e -> reload());
        header.add(refreshBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Split Pane: Top = Orders list, Bottom = Items in selected order
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.55);
        splitPane.setBorder(BorderFactory.createEmptyBorder());

        // Top Panel: Orders Table
        JPanel topPanel = new JPanel(new BorderLayout(0, 6));
        topPanel.setBackground(UITheme.CARD_BG);
        topPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_GRAY, 1),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JLabel ordersLbl = new JLabel("Your Past Orders (Select to view purchased items)");
        ordersLbl.setFont(UITheme.FONT_BOLD);
        topPanel.add(ordersLbl, BorderLayout.NORTH);

        String[] orderCols = {"Order ID", "Order Date", "Shipment / Tracking #", "Total Amount (\u20B9)", "Status"};
        ordersModel = new DefaultTableModel(orderCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        ordersTable = new JTable(ordersModel);
        ordersTable.setRowHeight(32);
        ordersTable.getTableHeader().setFont(UITheme.FONT_BOLD);
        ordersTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = ordersTable.getSelectedRow();
                if (row != -1 && row < customerOrders.size()) {
                    loadOrderItems(customerOrders.get(row));
                }
            }
        });

        topPanel.add(new JScrollPane(ordersTable), BorderLayout.CENTER);
        splitPane.setTopComponent(topPanel);

        // Bottom Panel: Selected Order Items
        JPanel bottomPanel = new JPanel(new BorderLayout(0, 6));
        bottomPanel.setBackground(UITheme.CARD_BG);
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_GRAY, 1),
                new EmptyBorder(10, 12, 10, 12)
        ));

        orderInfoLabel = new JLabel("Items in Selected Order");
        orderInfoLabel.setFont(UITheme.FONT_BOLD);
        bottomPanel.add(orderInfoLabel, BorderLayout.NORTH);

        String[] itemCols = {"Product Name", "Quantity", "Unit Price (\u20B9)", "Subtotal (\u20B9)"};
        itemsModel = new DefaultTableModel(itemCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        itemsTable = new JTable(itemsModel);
        itemsTable.setRowHeight(30);
        itemsTable.getTableHeader().setFont(UITheme.FONT_BOLD);

        bottomPanel.add(new JScrollPane(itemsTable), BorderLayout.CENTER);
        splitPane.setBottomComponent(bottomPanel);

        add(splitPane, BorderLayout.CENTER);
    }

    public void reload() {
        Customer cust = customerSupplier.get();
        if (cust == null) {
            ordersModel.setRowCount(0);
            itemsModel.setRowCount(0);
            return;
        }

        new SwingWorker<List<Order>, Void>() {
            @Override
            protected List<Order> doInBackground() {
                return portalService.getCustomerOrders(cust.getCustomerId());
            }

            @Override
            protected void done() {
                try {
                    customerOrders = get();
                    ordersModel.setRowCount(0);
                    itemsModel.setRowCount(0);
                    orderInfoLabel.setText("Items in Selected Order");

                    for (Order o : customerOrders) {
                        ordersModel.addRow(new Object[]{
                                "#" + o.getOrderId(),
                                o.getOrderDate() != null ? o.getOrderDate().toString().replace("T", " ") : "-",
                                o.getTrackingNo() != null ? o.getTrackingNo() : "Pending Assignment",
                                "\u20B9 " + o.getTotalAmount(),
                                o.getStatus()
                        });
                    }

                    if (!customerOrders.isEmpty()) {
                        ordersTable.setRowSelectionInterval(0, 0);
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void loadOrderItems(Order o) {
        orderInfoLabel.setText("Items in Order #" + o.getOrderId() + " (" + o.getStatus() + ") - Total: \u20B9" + o.getTotalAmount());
        new SwingWorker<List<OrderItem>, Void>() {
            @Override
            protected List<OrderItem> doInBackground() {
                return orderItemDao.findByOrderId(o.getOrderId());
            }

            @Override
            protected void done() {
                try {
                    List<OrderItem> items = get();
                    itemsModel.setRowCount(0);
                    for (OrderItem it : items) {
                        itemsModel.addRow(new Object[]{
                                it.getProductName() != null ? it.getProductName() : "Product #" + it.getProductId(),
                                it.getQuantity(),
                                "\u20B9 " + it.getUnitPrice(),
                                "\u20B9 " + it.getSubtotal()
                        });
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }
}
