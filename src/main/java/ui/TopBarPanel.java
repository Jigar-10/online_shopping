package ui;

import config.DatabaseConfig;
import config.DatabaseConnection;
import ui.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TopBarPanel extends JPanel implements DatabaseConnection.StatusListener {
    private final Runnable openConnectionDialog;
    private final Runnable toggleThemeAction;
    private final Runnable onRefreshCurrent;

    private JLabel titleLabel;
    private JButton statusPill;
    private JButton themeBtn;

    public TopBarPanel(Runnable openConnectionDialog, Runnable toggleThemeAction, Runnable onRefreshCurrent) {
        this.openConnectionDialog = openConnectionDialog;
        this.toggleThemeAction = toggleThemeAction;
        this.onRefreshCurrent = onRefreshCurrent;
        initUI();
        DatabaseConnection.addStatusListener(this);
    }

    private void initUI() {
        setLayout(new BorderLayout(16, 0));
        setBorder(new EmptyBorder(12, 20, 12, 20));

        titleLabel = new JLabel("Dashboard Overview");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        add(titleLabel, BorderLayout.WEST);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        statusPill = new JButton("Checking MySQL...");
        statusPill.setFont(UITheme.FONT_SMALL);
        statusPill.setCursor(new Cursor(Cursor.HAND_CURSOR));
        statusPill.setToolTipText("Click to configure MySQL connection");
        statusPill.addActionListener(e -> {
            if (openConnectionDialog != null) openConnectionDialog.run();
        });

        themeBtn = new JButton(UITheme.isDarkMode() ? "☀ Light Mode" : "🌙 Dark Mode");
        themeBtn.setFont(UITheme.FONT_SMALL);
        themeBtn.addActionListener(e -> {
            if (toggleThemeAction != null) {
                toggleThemeAction.run();
                themeBtn.setText(UITheme.isDarkMode() ? "☀ Light Mode" : "🌙 Dark Mode");
            }
        });

        JButton refreshBtn = new JButton("🔄");
        refreshBtn.setToolTipText("Refresh active screen");
        refreshBtn.addActionListener(e -> {
            if (onRefreshCurrent != null) onRefreshCurrent.run();
        });

        rightControls.add(statusPill);
        rightControls.add(themeBtn);
        rightControls.add(refreshBtn);
        add(rightControls, BorderLayout.EAST);
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    @Override
    public void onStatusChanged(DatabaseConnection.ConnectionResult result) {
        SwingUtilities.invokeLater(() -> {
            DatabaseConfig cfg = DatabaseConfig.getInstance();
            if (result.isSuccess()) {
                statusPill.setText("🟢 Connected: " + cfg.getHost() + ":" + cfg.getPort() + "/" + cfg.getDatabase());
                statusPill.setBackground(new Color(16, 185, 129, 30));
                statusPill.setForeground(UITheme.SUCCESS);
            } else {
                statusPill.setText("🔴 " + result.status().getDisplayText() + " (Click to configure)");
                statusPill.setBackground(new Color(239, 68, 68, 30));
                statusPill.setForeground(UITheme.DANGER);
            }
        });
    }
}
