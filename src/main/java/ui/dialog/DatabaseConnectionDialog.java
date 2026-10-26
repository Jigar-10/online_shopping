package ui.dialog;

import config.DatabaseConfig;
import config.DatabaseConnection;
import config.DatabaseInitializer;
import ui.util.NotificationUtils;
import ui.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

public class DatabaseConnectionDialog extends JDialog {
    private final DatabaseConfig config = DatabaseConfig.getInstance();

    private JTextField hostField;
    private JTextField portField;
    private JTextField dbField;
    private JTextField userField;
    private JPasswordField passField;
    private JLabel statusLabel;
    private JButton testBtn;
    private JButton saveBtn;
    private JButton initDbBtn;

    public DatabaseConnectionDialog(Window owner) {
        super(owner, "MySQL Database Connection & Setup", ModalityType.APPLICATION_MODAL);
        initUI();
    }

    private void initUI() {
        setSize(520, 480);
        setLocationRelativeTo(getOwner());
        setResizable(false);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(16, 24, 16, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        hostField = new JTextField(config.getHost(), 20);
        portField = new JTextField(String.valueOf(config.getPort()), 20);
        dbField = new JTextField(config.getDatabase(), 20);
        userField = new JTextField(config.getUsername(), 20);
        passField = new JPasswordField(config.getPassword(), 20);

        statusLabel = new JLabel("Status: " + DatabaseConnection.getLastResult().message());
        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setForeground(DatabaseConnection.getLastResult().isSuccess() ? UITheme.SUCCESS : UITheme.DANGER);

        int row = 0;
        addFormField(form, gbc, row++, "Server Host:", hostField);
        addFormField(form, gbc, row++, "Port Number:", portField);
        addFormField(form, gbc, row++, "Database Name:", dbField);
        addFormField(form, gbc, row++, "Username:", userField);
        addFormField(form, gbc, row++, "Password:", passField);

        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        form.add(statusLabel, gbc);

        // Advanced Tools Panel (Seed / Init DB)
        JPanel toolsPanel = new JPanel(new BorderLayout(8, 8));
        toolsPanel.setBorder(BorderFactory.createTitledBorder("Database Schema & Seed Tools"));
        initDbBtn = new JButton("Run / Verify 'online_shopping_full_database.sql'");
        initDbBtn.setToolTipText("Executes the bundled SQL script to create tables, views, triggers, and sample data");
        initDbBtn.addActionListener(e -> onInitDatabase());
        toolsPanel.add(initDbBtn, BorderLayout.CENTER);

        gbc.gridy = row++;
        form.add(toolsPanel, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Separator.foreground")));

        testBtn = new JButton("Test Connection");
        testBtn.addActionListener(e -> onTestConnection());

        JButton cancelBtn = new JButton("Close");
        cancelBtn.addActionListener(e -> dispose());

        saveBtn = new JButton("Save & Connect");
        saveBtn.setBackground(UITheme.PRIMARY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(UITheme.FONT_BOLD);
        saveBtn.addActionListener(e -> onSave());

        buttonPanel.add(testBtn);
        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        add(form, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0.35;
        JLabel label = new JLabel(labelText);
        label.setFont(UITheme.FONT_BOLD);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        panel.add(field, gbc);
    }

    private void onTestConnection() {
        testBtn.setEnabled(false);
        statusLabel.setText("Status: Testing connection to MySQL...");
        statusLabel.setForeground(UITheme.INFO);

        String host = hostField.getText().trim();
        int port;
        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException e) {
            statusLabel.setText("Status: Invalid port number.");
            statusLabel.setForeground(UITheme.DANGER);
            testBtn.setEnabled(true);
            return;
        }

        String db = dbField.getText().trim();
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword());

        new SwingWorker<DatabaseConnection.ConnectionResult, Void>() {
            @Override
            protected DatabaseConnection.ConnectionResult doInBackground() {
                return DatabaseConnection.testConnection(host, port, db, user, pass);
            }

            @Override
            protected void done() {
                testBtn.setEnabled(true);
                try {
                    DatabaseConnection.ConnectionResult res = get();
                    statusLabel.setText("Status: " + res.message());
                    statusLabel.setForeground(res.isSuccess() ? UITheme.SUCCESS : UITheme.DANGER);
                    if (res.isSuccess()) {
                        NotificationUtils.showSuccess(DatabaseConnectionDialog.this, res.message());
                    } else {
                        NotificationUtils.showError(DatabaseConnectionDialog.this, res.message());
                    }
                } catch (Exception e) {
                    statusLabel.setText("Status: " + e.getMessage());
                    statusLabel.setForeground(UITheme.DANGER);
                }
            }
        }.execute();
    }

    private void onSave() {
        try {
            config.setHost(hostField.getText().trim());
            config.setPort(Integer.parseInt(portField.getText().trim()));
            config.setDatabase(dbField.getText().trim());
            config.setUsername(userField.getText().trim());
            config.setPassword(new String(passField.getPassword()));
            config.save();

            DatabaseConnection.ConnectionResult res = DatabaseConnection.testConnection();
            if (res.isSuccess()) {
                NotificationUtils.showSuccess(this, "Connected successfully to " + config.getDatabase() + "!");
                dispose();
            } else {
                NotificationUtils.showWarning(this, "Configuration saved, but connection test failed: " + res.message());
            }
        } catch (Exception e) {
            NotificationUtils.showError(this, "Failed to save configuration", e);
        }
    }

    private void onInitDatabase() {
        File sqlFile = new File("online_shopping_full_database.sql");
        if (!sqlFile.exists()) {
            NotificationUtils.showError(this, "online_shopping_full_database.sql was not found in project directory.");
            return;
        }

        boolean ok = NotificationUtils.confirm(this, "Initialize Database",
                "This will execute 'online_shopping_full_database.sql' on MySQL host " + hostField.getText().trim() +
                ". Existing tables and seed data will be created or verified.\n\nProceed?");
        if (!ok) return;

        initDbBtn.setEnabled(false);
        initDbBtn.setText("Executing SQL Script...");

        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                // Save current config fields first so initializer uses them
                config.setHost(hostField.getText().trim());
                config.setPort(Integer.parseInt(portField.getText().trim()));
                config.setDatabase(dbField.getText().trim());
                config.setUsername(userField.getText().trim());
                config.setPassword(new String(passField.getPassword()));
                return DatabaseInitializer.executeSqlScript(sqlFile);
            }

            @Override
            protected void done() {
                initDbBtn.setEnabled(true);
                initDbBtn.setText("Run / Verify 'online_shopping_full_database.sql'");
                try {
                    int count = get();
                    NotificationUtils.showSuccess(DatabaseConnectionDialog.this,
                            "Database initialized successfully! Executed " + count + " statements.\n\nNow testing connection...");
                    DatabaseConnection.testConnection();
                } catch (Exception e) {
                    NotificationUtils.showError(DatabaseConnectionDialog.this, "Database script execution failed", e);
                }
            }
        }.execute();
    }
}
