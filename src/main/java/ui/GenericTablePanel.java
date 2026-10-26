package ui;

import ui.util.NotificationUtils;
import ui.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Vector;

public class GenericTablePanel<T> extends JPanel {
    public interface DataLoader<T> {
        List<T> loadData(String searchQuery) throws Exception;
        Vector<String> getColumnNames();
        Vector<Object> toRowData(T item);
    }

    public interface EntityAction<T> {
        void execute(T item, GenericTablePanel<T> panel);
    }

    private final String title;
    private final DataLoader<T> loader;
    private final EntityAction<T> addAction;
    private final EntityAction<T> editAction;
    private final EntityAction<T> deleteAction;

    private List<T> currentItems;
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField searchField;
    private JButton addBtn;
    private JButton editBtn;
    private JButton deleteBtn;
    private JButton refreshBtn;
    private JLabel statusLabel;
    private JProgressBar progressBar;

    public GenericTablePanel(String title,
                             DataLoader<T> loader,
                             EntityAction<T> addAction,
                             EntityAction<T> editAction,
                             EntityAction<T> deleteAction) {
        this.title = title;
        this.loader = loader;
        this.addAction = addAction;
        this.editAction = editAction;
        this.deleteAction = deleteAction;
        initUI();
        loadDataAsync(null);
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 10));
        setBorder(new EmptyBorder(16, 20, 16, 20));

        // Top Header and Controls Panel
        JPanel topPanel = new JPanel(new BorderLayout(12, 10));

        // Title and Search
        JPanel titleAndSearch = new JPanel(new BorderLayout(16, 0));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleAndSearch.add(titleLabel, BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        searchField = new JTextField(20);
        searchField.putClientProperty("JTextField.placeholderText", "Search records...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { onSearch(); }
            @Override public void removeUpdate(DocumentEvent e) { onSearch(); }
            @Override public void changedUpdate(DocumentEvent e) { onSearch(); }
        });

        JButton clearSearchBtn = new JButton("\u2715");
        clearSearchBtn.setToolTipText("Clear search");
        clearSearchBtn.addActionListener(e -> searchField.setText(""));

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(clearSearchBtn);
        titleAndSearch.add(searchPanel, BorderLayout.EAST);

        // Action Toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBorder(new EmptyBorder(4, 0, 4, 0));

        addBtn = new JButton("+ Add Record");
        addBtn.setBackground(UITheme.PRIMARY);
        addBtn.setForeground(Color.WHITE);
        addBtn.setFont(UITheme.FONT_BOLD);
        addBtn.addActionListener(e -> {
            if (addAction != null) addAction.execute(null, this);
        });

        editBtn = new JButton("✏ Edit Selected");
        editBtn.setEnabled(false);
        editBtn.addActionListener(e -> onEditSelected());

        deleteBtn = new JButton("🗑 Delete Selected");
        deleteBtn.setEnabled(false);
        deleteBtn.addActionListener(e -> onDeleteSelected());

        refreshBtn = new JButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> loadDataAsync(searchField.getText().trim()));

        toolBar.add(addBtn);
        toolBar.add(Box.createHorizontalStrut(8));
        toolBar.add(editBtn);
        toolBar.add(Box.createHorizontalStrut(8));
        toolBar.add(deleteBtn);
        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(refreshBtn);

        topPanel.add(titleAndSearch, BorderLayout.NORTH);
        topPanel.add(toolBar, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // Table setup
        tableModel = new DefaultTableModel(loader.getColumnNames(), 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(32);
        table.getTableHeader().setFont(UITheme.FONT_BOLD);
        table.setFont(UITheme.FONT_REGULAR);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = table.getSelectedRow() != -1;
                editBtn.setEnabled(hasSelection && editAction != null);
                deleteBtn.setEnabled(hasSelection && deleteAction != null);
                updateStatusText();
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    onEditSelected();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground")));
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Footer
        JPanel footerPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel("Loading data from database...");
        statusLabel.setFont(UITheme.FONT_SMALL);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(100, 14));

        footerPanel.add(statusLabel, BorderLayout.WEST);
        footerPanel.add(progressBar, BorderLayout.EAST);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private void onSearch() {
        String query = searchField.getText().trim();
        loadDataAsync(query);
    }

    private void onEditSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1 || editAction == null) return;
        int modelRow = table.convertRowIndexToModel(selectedRow);
        if (modelRow >= 0 && modelRow < currentItems.size()) {
            editAction.execute(currentItems.get(modelRow), this);
        }
    }

    private void onDeleteSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1 || deleteAction == null) return;
        int modelRow = table.convertRowIndexToModel(selectedRow);
        if (modelRow >= 0 && modelRow < currentItems.size()) {
            deleteAction.execute(currentItems.get(modelRow), this);
        }
    }

    public void reload() {
        loadDataAsync(searchField.getText().trim());
    }

    public void loadDataAsync(String query) {
        progressBar.setVisible(true);
        statusLabel.setText("Querying MySQL database...");

        new SwingWorker<List<T>, Void>() {
            @Override
            protected List<T> doInBackground() throws Exception {
                return loader.loadData(query);
            }

            @Override
            protected void done() {
                progressBar.setVisible(false);
                try {
                    currentItems = get();
                    tableModel.setRowCount(0);
                    for (T item : currentItems) {
                        tableModel.addRow(loader.toRowData(item));
                    }
                    updateStatusText();
                } catch (Exception e) {
                    statusLabel.setText("Error loading data: " + e.getMessage());
                    NotificationUtils.showError(GenericTablePanel.this, "Database Query Error", e);
                }
            }
        }.execute();
    }

    private void updateStatusText() {
        int total = currentItems != null ? currentItems.size() : 0;
        int selected = table.getSelectedRow();
        if (selected != -1) {
            statusLabel.setText("Displaying " + total + " records | Selected row: " + (selected + 1));
        } else {
            statusLabel.setText("Displaying " + total + " records | No row selected");
        }
    }

    public T getSelectedItem() {
        int row = table.getSelectedRow();
        if (row != -1 && currentItems != null) {
            int modelRow = table.convertRowIndexToModel(row);
            if (modelRow >= 0 && modelRow < currentItems.size()) {
                return currentItems.get(modelRow);
            }
        }
        return null;
    }
}
