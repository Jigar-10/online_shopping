package ui.util;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Font;

public class UITheme {
    private static final Logger logger = LoggerFactory.getLogger(UITheme.class);
    private static boolean isDark = true;

    // Harmonious modern color palette
    public static final Color PRIMARY = new Color(79, 70, 229);       // Indigo 600
    public static final Color PRIMARY_HOVER = new Color(99, 102, 241); // Indigo 500
    public static final Color SUCCESS = new Color(16, 185, 129);       // Emerald 500
    public static final Color WARNING = new Color(245, 158, 11);       // Amber 500
    public static final Color DANGER = new Color(239, 68, 68);         // Rose 500
    public static final Color INFO = new Color(59, 130, 246);          // Blue 500

    public static final Color SIDEBAR_DARK = new Color(15, 23, 42);    // Slate 900
    public static final Color SIDEBAR_LIGHT = new Color(241, 245, 249); // Slate 100

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    public static void init() {
        try {
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("ProgressBar.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ScrollBar.showButtons", false);
            UIManager.put("ScrollBar.width", 10);
            UIManager.put("Table.rowHeight", 32);
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.showVerticalLines", false);
            UIManager.put("Table.intercellSpacing", new java.awt.Dimension(0, 1));

            if (isDark) {
                FlatDarkLaf.setup();
            } else {
                FlatLightLaf.setup();
            }
        } catch (Exception e) {
            logger.error("Failed to initialize FlatLaf theme", e);
        }
    }

    public static void toggleTheme() {
        isDark = !isDark;
        if (isDark) {
            FlatDarkLaf.setup();
        } else {
            FlatLightLaf.setup();
        }
        FlatLaf.updateUI();
    }

    public static boolean isDarkMode() {
        return isDark;
    }

    public static Border createCardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(isDark ? new Color(51, 65, 85) : new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        );
    }

    public static void styleCard(JPanel panel) {
        panel.setBorder(createCardBorder());
        panel.setBackground(isDark ? new Color(30, 41, 59) : Color.WHITE);
    }
}
