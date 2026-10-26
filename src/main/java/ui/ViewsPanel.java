package ui;

import dao.ViewDao;
import ui.util.NotificationUtils;
import ui.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ViewsPanel extends JPanel {
    private final ViewDao viewDao = new ViewDao();

    private JComboBox<String> viewSelector;
    private JTextField searchField;
    private JTable viewTable;
    private JLabel statusLabel;
    private JProgressBar progressBar;

    public ViewsPanel() {
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 12));
        setBorder(new EmptyBorder(16, 20, 16, 20));

        // Top Control Bar
        JPanel topPanel = new JPanel(new BorderLayout(12, 8));

        JPanel titleAndNote = new JPanel(new BorderLayout(8, 4));
        JLabel titleLabel = new JLabel("Database Views (Read-Only)");
        titleLabel.setFont(UITheme.FONT_TITLE);

        JLabel noteLabel = new JLabel("Experiment 7: Pre-aggregated relational views defined in schema. Direct modifications are disabled by design.");
        noteLabel.setFont(UITheme.FONT_SMALL);
        noteLabel.setForeground(Color.GRAY);

        titleAndNote.add(titleLabel, BorderLayout.NORTH);
        titleAndNote.add(noteLabel, BorderLayout.SOUTH);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        viewSelector = new JComboBox<>();
        loadViewNames();
        viewSelector.addActionListener(e -> loadViewDataAsync());

        searchField = new JTextField(18);
        searchField.putClientProperty("JTextField.placeholderText", "Filter view records...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { loadViewDataAsync(); }
            @Override public void removeUpdate(DocumentEvent e) { loadViewDataAsync(); }
            @Override public void changedUpdate(DocumentEvent e) { loadViewDataAsync(); }
        });

        JButton refreshBtn = new JButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> loadViewDataAsync());

        controls.add(new JLabel("Select View:"));
        controls.add(viewSelector);
        controls.add(searchField);
        controls.add(refreshBtn);

        topPanel.add(titleAndNote, BorderLayout.WEST);
        topPanel.add(controls, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Table
        viewTable = new JTable();
        viewTable.setRowHeight(30);
        viewTable.getTableHeader().setFont(UITheme.FONT_BOLD);
        viewTable.setFont(UITheme.FONT_REGULAR);

        JScrollPane scrollPane = new JScrollPane(viewTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground")));
        add(scrollPane, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new BorderLayout());
        statusLabel = new JLabel("Select a view to display records");
        statusLabel.setFont(UITheme.FONT_SMALL);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(100, 14));

        footer.add(statusLabel, BorderLayout.WEST);
        footer.add(progressBar, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);

        if (viewSelector.getItemCount() > 0) {
            loadViewDataAsync();
        }
    }

    private void loadViewNames() {
        try {
            List<String> views = viewDao.getAvailableViews();
            viewSelector.removeAllItems();
            for (String v : views) {
                viewSelector.addItem(v);
            }
        } catch (Exception e) {
            NotificationUtils.showError(this, "Could not load view names", e);
        }
    }

    public void reload() {
        loadViewDataAsync();
    }

    private void loadViewDataAsync() {
        String viewName = (String) viewSelector.getSelectedItem();
        if (viewName == null) return;

        String query = searchField.getText().trim();
        progressBar.setVisible(true);
        statusLabel.setText("Querying view '" + viewName + "'...");

        new SwingWorker<DefaultTableModel, Void>() {
            @Override
            protected DefaultTableModel doInBackground() {
                return viewDao.loadViewData(viewName, query);
            }

            @Override
            protected void done() {
                progressBar.setVisible(false);
                try {
                    DefaultTableModel model = get();
                    viewTable.setModel(model);
                    statusLabel.setText("View '" + viewName + "': Loaded " + model.getRowCount() + " records");
                } catch (Exception e) {
                    statusLabel.setText("Error querying view: " + e.getMessage());
                }
            }
        }.execute();
    }
}
