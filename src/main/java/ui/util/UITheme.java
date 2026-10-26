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
    // User requested default light aesthetic with shades of white and gray
    private static boolean isDark = false;

    // Modern light color palette - Crisp white, soft slate grays, and elegant accents
    public static final Color BG_MAIN = new Color(248, 250, 252);        // Slate 50
    public static final Color CARD_BG = new Color(255, 255, 255);        // Pure White
    public static final Color BORDER_GRAY = new Color(226, 232, 240);    // Slate 200
    public static final Color BORDER_HOVER = new Color(203, 213, 225);   // Slate 300

    public static final Color TEXT_PRIMARY = new Color(15, 23, 42);     // Slate 900
    public static final Color TEXT_MUTED = new Color(100, 116, 139);     // Slate 500
    public static final Color TEXT_LIGHT = new Color(148, 163, 184);     // Slate 400

    public static final Color PRIMARY = new Color(37, 99, 235);          // Blue 600
    public static final Color PRIMARY_LIGHT = new Color(239, 246, 255);  // Blue 50
    public static final Color SUCCESS = new Color(16, 185, 129);        // Emerald 500
    public static final Color SUCCESS_LIGHT = new Color(236, 253, 245);  // Emerald 50
    public static final Color WARNING = new Color(245, 158, 11);         // Amber 500
    public static final Color DANGER = new Color(239, 68, 68);           // Rose 500
    public static final Color INFO = new Color(59, 130, 246);            // Sky Blue 500

    public static final Color SIDEBAR_LIGHT = new Color(248, 250, 252);  // Light clean sidebar
    public static final Color SIDEBAR_DARK = new Color(15, 23, 42);     // Slate 900

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_PRICE = new Font("Segoe UI", Font.BOLD, 16);

    public static void init() {
        try {
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("ProgressBar.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ScrollBar.showButtons", false);
            UIManager.put("ScrollBar.width", 10);
            UIManager.put("Table.rowHeight", 34);
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.showVerticalLines", false);
            UIManager.put("Table.intercellSpacing", new java.awt.Dimension(0, 1));
            UIManager.put("TabbedPane.selectedBackground", Color.WHITE);

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
                BorderFactory.createLineBorder(isDark ? new Color(51, 65, 85) : BORDER_GRAY, 1),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        );
    }

    public static Border createProductCardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_GRAY, 1),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        );
    }

    public static void styleCard(JPanel panel) {
        panel.setBorder(createCardBorder());
        panel.setBackground(isDark ? new Color(30, 41, 59) : CARD_BG);
    }
}
