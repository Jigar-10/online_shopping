package ui.customer;

import dao.AddressDao;
import model.Address;
import model.Customer;
import service.CustomerPortalService;
import ui.dialog.AddressDialog;
import ui.util.NotificationUtils;
import ui.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MyAddressesPanel extends JPanel {
    private final CustomerPortalService portalService;
    private final java.util.function.Supplier<Customer> customerSupplier;
    private final AddressDao addressDao = new AddressDao();

    private DefaultTableModel tableModel;
    private JTable addressTable;
    private List<Address> addresses = new ArrayList<>();

    public MyAddressesPanel(CustomerPortalService portalService,
                            java.util.function.Supplier<Customer> customerSupplier) {
        this.portalService = portalService;
        this.customerSupplier = customerSupplier;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Saved Delivery Addresses");
        title.setFont(UITheme.FONT_TITLE);
        header.add(title, BorderLayout.WEST);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        toolbar.setOpaque(false);

        JButton addBtn = new JButton("+ Add New Address");
        addBtn.setBackground(UITheme.PRIMARY);
        addBtn.setForeground(Color.WHITE);
        addBtn.setFont(UITheme.FONT_BOLD);
        addBtn.addActionListener(e -> {
            Customer c = customerSupplier.get();
            if (c == null) return;
            Address a = new Address();
            a.setCustomerId(c.getCustomerId());
            AddressDialog d = new AddressDialog(SwingUtilities.getWindowAncestor(this), a);
            d.setVisible(true);
            if (d.isSaved()) reload();
        });

        JButton editBtn = new JButton("✏ Edit");
        editBtn.addActionListener(e -> {
            int row = addressTable.getSelectedRow();
            if (row != -1 && row < addresses.size()) {
                AddressDialog d = new AddressDialog(SwingUtilities.getWindowAncestor(this), addresses.get(row));
                d.setVisible(true);
                if (d.isSaved()) reload();
            }
        });

        JButton deleteBtn = new JButton("🗑 Delete");
        deleteBtn.addActionListener(e -> {
            int row = addressTable.getSelectedRow();
            if (row != -1 && row < addresses.size()) {
                Address a = addresses.get(row);
                if (NotificationUtils.confirm(this, "Delete Address", "Delete address: " + a.getStreet() + ", " + a.getCity() + "?")) {
                    try {
                        addressDao.delete(a.getAddressId());
                        NotificationUtils.showSuccess(this, "Address deleted.");
                        reload();
                    } catch (Exception ex) {
                        NotificationUtils.showError(this, ex.getMessage(), ex);
                    }
                }
            }
        });

        JButton refreshBtn = new JButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> reload());

        toolbar.add(addBtn);
        toolbar.add(editBtn);
        toolbar.add(deleteBtn);
        toolbar.add(refreshBtn);
        header.add(toolbar, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        String[] cols = {"Address ID", "Street Address", "City", "State", "Country", "Pincode"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        addressTable = new JTable(tableModel);
        addressTable.setRowHeight(34);
        addressTable.getTableHeader().setFont(UITheme.FONT_BOLD);
        addressTable.setFont(UITheme.FONT_REGULAR);

        JScrollPane scrollPane = new JScrollPane(addressTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_GRAY, 1));
        add(scrollPane, BorderLayout.CENTER);
    }

    public void reload() {
        Customer cust = customerSupplier.get();
        if (cust == null) {
            tableModel.setRowCount(0);
            return;
        }

        new SwingWorker<List<Address>, Void>() {
            @Override
            protected List<Address> doInBackground() {
                return portalService.getCustomerAddresses(cust.getCustomerId());
            }

            @Override
            protected void done() {
                try {
                    addresses = get();
                    tableModel.setRowCount(0);
                    for (Address a : addresses) {
                        tableModel.addRow(new Object[]{
                                a.getAddressId(),
                                a.getStreet(),
                                a.getCity(),
                                a.getState(),
                                a.getCountry(),
                                a.getPincode()
                        });
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }
}
