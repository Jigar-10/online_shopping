package dao;

import config.DatabaseConfig;
import config.DatabaseConnection;
import exception.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class ViewDao {
    private static final Logger logger = LoggerFactory.getLogger(ViewDao.class);

    public static final List<String> KNOWN_VIEWS = List.of(
            "customer_view",
            "product_view",
            "customer_order_view",
            "payment_view",
            "shipping_view"
    );

    public List<String> getAvailableViews() {
        List<String> views = new ArrayList<>();
        String dbName = DatabaseConfig.getInstance().getDatabase();
        String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.VIEWS WHERE TABLE_SCHEMA = ? ORDER BY TABLE_NAME ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dbName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    views.add(rs.getString("TABLE_NAME"));
                }
            }
        } catch (SQLException e) {
            logger.warn("Could not query INFORMATION_SCHEMA.VIEWS: {}", e.getMessage());
            // Fallback to known views
            return new ArrayList<>(KNOWN_VIEWS);
        }

        if (views.isEmpty()) {
            return new ArrayList<>(KNOWN_VIEWS);
        }
        return views;
    }

    public DefaultTableModel loadViewData(String viewName, String searchQuery) {
        // Sanitize view name against allowed list / alphanumeric underscore
        if (!viewName.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("Invalid view name: " + viewName);
        }

        StringBuilder sql = new StringBuilder("SELECT * FROM ").append(viewName);
        boolean hasSearch = searchQuery != null && !searchQuery.isBlank();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString());
             ResultSet rs = ps.executeQuery()) {

            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            Vector<String> colNames = new Vector<>();
            for (int i = 1; i <= colCount; i++) {
                colNames.add(meta.getColumnLabel(i));
            }

            Vector<Vector<Object>> data = new Vector<>();
            String searchLower = hasSearch ? searchQuery.trim().toLowerCase() : "";

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                boolean rowMatches = !hasSearch;

                for (int i = 1; i <= colCount; i++) {
                    Object val = rs.getObject(i);
                    row.add(val);
                    if (hasSearch && val != null && val.toString().toLowerCase().contains(searchLower)) {
                        rowMatches = true;
                    }
                }

                if (rowMatches) {
                    data.add(row);
                }
            }

            return new DefaultTableModel(data, colNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; // Database views are strictly read-only
                }
            };
        } catch (SQLException e) {
            logger.error("Error loading view {}", viewName, e);
            throw new DatabaseException("Failed to load view '" + viewName + "': " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }
}
