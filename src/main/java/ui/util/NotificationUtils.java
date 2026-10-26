package ui.util;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

public class NotificationUtils {

    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(Component parent, String message) {
        showError(parent, message, null);
    }

    public static void showError(Component parent, String message, Throwable ex) {
        StringBuilder sb = new StringBuilder(message);
        if (ex != null && ex.getMessage() != null && !message.contains(ex.getMessage())) {
            sb.append("\n\nDetails: ").append(ex.getMessage());
        }
        JOptionPane.showMessageDialog(parent, sb.toString(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showWarning(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    public static boolean confirm(Component parent, String title, String message) {
        int result = JOptionPane.showConfirmDialog(
                parent,
                message,
                title,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        return result == JOptionPane.YES_OPTION;
    }

    public static JLabel createStatusBadge(String status) {
        JLabel label = new JLabel(status != null ? status : "N/A");
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setOpaque(true);
        label.setBorder(new EmptyBorder(3, 8, 3, 8));

        Color bg;
        Color fg = Color.WHITE;

        if (status == null) {
            bg = Color.GRAY;
        } else {
            String s = status.toLowerCase();
            if (s.contains("deliver") || s.contains("complete") || s.contains("connect")) {
                bg = UITheme.SUCCESS;
            } else if (s.contains("ship") || s.contains("process")) {
                bg = UITheme.INFO;
            } else if (s.contains("pend") || s.contains("warn")) {
                bg = UITheme.WARNING;
                fg = Color.BLACK;
            } else if (s.contains("cancel") || s.contains("fail") || s.contains("denied")) {
                bg = UITheme.DANGER;
            } else {
                bg = new Color(100, 116, 139);
            }
        }

        label.setBackground(bg);
        label.setForeground(fg);
        return label;
    }
}
