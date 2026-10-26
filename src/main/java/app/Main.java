package app;

import config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ui.MainFrame;
import ui.util.UITheme;

import javax.swing.SwingUtilities;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Starting Online Shopping DBMS Application (Experiments 3 to 8)...");
        logger.info("Java Version: {}", System.getProperty("java.version"));
        logger.info("Operating System: {}", System.getProperty("os.name"));

        // Initialize Database Config
        DatabaseConfig.getInstance();

        // Initialize Swing Look & Feel (FlatLaf)
        UITheme.init();

        // Launch UI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
                logger.info("Main application window displayed successfully.");
            } catch (Exception e) {
                logger.error("Fatal error starting application UI", e);
            }
        });
    }
}
