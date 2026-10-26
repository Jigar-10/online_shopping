package ui.util;

import model.Product;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ImageUtils {
    private static final Map<String, ImageIcon> CACHE = new HashMap<>();

    public static ImageIcon getProductImage(Product p, int width, int height) {
        String key = "prod_" + (p != null ? p.getProductId() : 0) + "_" + width + "x" + height;
        if (CACHE.containsKey(key)) {
            return CACHE.get(key);
        }

        String name = (p != null && p.getName() != null) ? p.getName().toLowerCase() : "";
        String cat = (p != null && p.getCategoryName() != null) ? p.getCategoryName().toLowerCase() : "";

        String resourceName = "laptop.jpg";
        if (name.contains("laptop") || (cat.contains("elec") && name.contains("computer"))) {
            resourceName = "laptop.jpg";
        } else if (name.contains("t-shirt") || name.contains("shirt") || cat.contains("cloth")) {
            resourceName = "tshirt.jpg";
        } else if (name.contains("book") || cat.contains("book") || name.contains("sql")) {
            resourceName = "book.jpg";
        } else if (name.contains("mixer") || cat.contains("appliance") || name.contains("kitchen")) {
            resourceName = "mixer.jpg";
        } else if (name.contains("headphone") || name.contains("audio") || cat.contains("access")) {
            resourceName = "headphones.jpg";
        }

        ImageIcon icon = loadScaledImage("/images/" + resourceName, width, height, name.isEmpty() ? "Product" : p.getName());
        CACHE.put(key, icon);
        return icon;
    }

    public static ImageIcon getBannerImage(int width, int height) {
        String key = "banner_" + width + "x" + height;
        if (CACHE.containsKey(key)) {
            return CACHE.get(key);
        }
        ImageIcon icon = loadScaledImage("/images/banner.jpg", width, height, "NovaMart Store");
        CACHE.put(key, icon);
        return icon;
    }

    public static ImageIcon loadScaledImage(String resourcePath, int width, int height, String placeholderText) {
        try {
            BufferedImage img = null;
            URL url = ImageUtils.class.getResource(resourcePath);
            if (url != null) {
                img = ImageIO.read(url);
            } else {
                File file = new File("src/main/resources" + resourcePath);
                if (file.exists()) {
                    img = ImageIO.read(file);
                } else {
                    File file2 = new File("target/classes" + resourcePath);
                    if (file2.exists()) {
                        img = ImageIO.read(file2);
                    }
                }
            }

            if (img != null) {
                Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            }
        } catch (Exception ignored) {}

        // Fallback placeholder image
        return createFallbackIcon(width, height, placeholderText);
    }

    private static ImageIcon createFallbackIcon(int width, int height, String text) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Gradient background
        GradientPaint gp = new GradientPaint(0, 0, new Color(241, 245, 249), width, height, new Color(226, 232, 240));
        g2.setPaint(gp);
        g2.fillRect(0, 0, width, height);

        // Border
        g2.setColor(new Color(203, 213, 225));
        g2.drawRect(0, 0, width - 1, height - 1);

        // Text
        g2.setFont(new Font("Segoe UI", Font.BOLD, Math.max(14, height / 6)));
        g2.setColor(new Color(100, 116, 139));
        FontMetrics fm = g2.getFontMetrics();
        String display = (text != null && !text.isBlank()) ? text : "Product";
        if (display.length() > 15) {
            display = display.substring(0, 12) + "...";
        }
        int tx = (width - fm.stringWidth(display)) / 2;
        int ty = ((height - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(display, tx, ty);

        g2.dispose();
        return new ImageIcon(img);
    }
}
