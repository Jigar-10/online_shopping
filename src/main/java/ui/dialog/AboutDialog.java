package ui.dialog;

import ui.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AboutDialog extends JDialog {

    public AboutDialog(Window owner) {
        super(owner, "About - Online Shopping DBMS Desktop Application", ModalityType.APPLICATION_MODAL);
        initUI();
    }

    private void initUI() {
        setSize(620, 520);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(24, 28, 20, 28));

        JLabel titleLabel = new JLabel("Online Shopping DBMS Desktop Studio");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(UITheme.PRIMARY);

        JLabel subtitleLabel = new JLabel("Integrated DBMS College Project (Experiments 3 to 8)");
        subtitleLabel.setFont(UITheme.FONT_BOLD);

        JEditorPane infoPane = new JEditorPane();
        infoPane.setContentType("text/html");
        infoPane.setEditable(false);
        infoPane.setOpaque(false);
        infoPane.setText("""
            <html>
            <body style='font-family: Segoe UI, sans-serif; font-size: 11px;'>
            <p><b>Architecture:</b> Java Swing UI &rarr; Service Layer &rarr; DAO Layer &rarr; JDBC &rarr; MySQL 8.0</p>
            
            <p><b>DBMS Experiments Covered:</b></p>
            <ul>
                <li><b>Experiment 3 (DDL):</b> 14 normalized relational tables with PRIMARY KEY, FOREIGN KEY, NOT NULL, UNIQUE, CHECK, and DEFAULT constraints.</li>
                <li><b>Experiment 4 (DML):</b> Full Create, Read, Update, and Delete (CRUD) operations on all base tables with prepared statements.</li>
                <li><b>Experiment 5 (DQL):</b> Rich filtering, relational joins, multi-column search, and live aggregation metrics.</li>
                <li><b>Experiment 6 (DCL):</b> Support for multi-user database roles, least-privilege schemas, and dynamic connection credentials.</li>
                <li><b>Experiment 7 (Views & Triggers):</b> Built-in viewer for 5 database views (<code>customer_view</code>, <code>product_view</code>, <code>customer_order_view</code>, <code>payment_view</code>, <code>shipping_view</code>) plus enforcement of integrity triggers (stock deduction, price non-negativity, email formatting, ratings validation).</li>
                <li><b>Experiment 8 (TCL):</b> Atomic multi-table transactions (Commit / Rollback) for multi-item orders, payments, and inventory adjustments.</li>
            </ul>
            
            <p><b>UI Features:</b> Modern FlatLaf design, Dark & Light mode toggle, live connection status indicator, async background querying (no EDT freezes), dynamic table search, and descriptive foreign-key selectors.</p>
            </body>
            </html>
            """);

        JScrollPane scrollPane = new JScrollPane(infoPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        content.add(titleLabel);
        content.add(Box.createVerticalStrut(4));
        content.add(subtitleLabel);
        content.add(Box.createVerticalStrut(12));
        content.add(scrollPane);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());
        buttonPanel.add(closeBtn);

        add(content, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}
