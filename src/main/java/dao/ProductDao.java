package dao;

import config.DatabaseConnection;
import exception.DatabaseException;
import model.ComboItem;
import model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductDao implements GenericDao<Product, Integer> {
    private static final Logger logger = LoggerFactory.getLogger(ProductDao.class);

    private static final String BASE_SELECT =
            "SELECT p.product_id, p.category_id, c.name AS category_name, " +
            "p.supplier_id, s.name AS supplier_name, p.name, p.description, p.stock_qty, p.price " +
            "FROM PRODUCT p " +
            "LEFT JOIN CATEGORY c ON p.category_id = c.category_id " +
            "LEFT JOIN SUPPLIER s ON p.supplier_id = s.supplier_id ";

    @Override
    public List<Product> findAll() {
        String sql = BASE_SELECT + "ORDER BY p.product_id ASC";
        List<Product> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding products", e);
            throw new DatabaseException("Failed to load products: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
        return list;
    }

    @Override
    public Optional<Product> findById(Integer id) {
        String sql = BASE_SELECT + "WHERE p.product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding product ID {}", id, e);
            throw new DatabaseException("Failed to find product: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
        return Optional.empty();
    }

    @Override
    public Product insert(Product p) {
        String sql = "INSERT INTO PRODUCT (category_id, supplier_id, name, description, stock_qty, price) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getCategoryId());
            ps.setInt(2, p.getSupplierId());
            ps.setString(3, p.getName());
            ps.setString(4, p.getDescription());
            ps.setInt(5, p.getStockQty() != null ? p.getStockQty() : 0);
            ps.setBigDecimal(6, p.getPrice() != null ? p.getPrice() : BigDecimal.ZERO);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    p.setProductId(keys.getInt(1));
                }
            }
            return p;
        } catch (SQLException e) {
            logger.error("Error inserting product", e);
            if (e.getErrorCode() == 1452) {
                throw new DatabaseException("Invalid Category or Supplier selected.", e.getSQLState(), e.getErrorCode(), e);
            } else if (e.getErrorCode() == 3819 || e.getMessage().contains("chk_product")) {
                throw new DatabaseException("Product validation failed: stock quantity and price must be non-negative.", e.getSQLState(), e.getErrorCode(), e);
            }
            throw new DatabaseException("Failed to insert product: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public boolean update(Product p) {
        String sql = "UPDATE PRODUCT SET category_id = ?, supplier_id = ?, name = ?, description = ?, stock_qty = ?, price = ? WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, p.getCategoryId());
            ps.setInt(2, p.getSupplierId());
            ps.setString(3, p.getName());
            ps.setString(4, p.getDescription());
            ps.setInt(5, p.getStockQty() != null ? p.getStockQty() : 0);
            ps.setBigDecimal(6, p.getPrice() != null ? p.getPrice() : BigDecimal.ZERO);
            ps.setInt(7, p.getProductId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating product ID {}", p.getProductId(), e);
            if (e.getErrorCode() == 1452) {
                throw new DatabaseException("Invalid Category or Supplier selected.", e.getSQLState(), e.getErrorCode(), e);
            } else if (e.getErrorCode() == 3819 || e.getMessage().contains("chk_product")) {
                throw new DatabaseException("Product validation failed: stock quantity and price must be non-negative.", e.getSQLState(), e.getErrorCode(), e);
            }
            throw new DatabaseException("Failed to update product: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM PRODUCT WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting product ID {}", id, e);
            if (e.getErrorCode() == 1451) {
                throw new DatabaseException("Cannot delete product (ID " + id + ") because cart items, orders, or reviews reference it.", e.getSQLState(), e.getErrorCode(), e);
            }
            throw new DatabaseException("Failed to delete product: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public List<Product> search(String query) {
        if (query == null || query.isBlank()) return findAll();
        String sql = BASE_SELECT +
                     "WHERE p.name LIKE ? OR p.description LIKE ? OR c.name LIKE ? OR s.name LIKE ? " +
                     "ORDER BY p.product_id ASC";
        String pattern = "%" + query.trim() + "%";
        List<Product> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 1; i <= 4; i++) ps.setString(i, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error searching products", e);
            throw new DatabaseException("Failed to search products: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
        return list;
    }

    public List<ComboItem<Integer>> findAllAsComboItems() {
        List<ComboItem<Integer>> items = new ArrayList<>();
        for (Product p : findAll()) {
            items.add(new ComboItem<>(p.getProductId(), p.getName() + " (\u20B9" + p.getPrice() + ", Stock: " + p.getStockQty() + ")", p.getPrice()));
        }
        return items;
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product(
                rs.getInt("product_id"),
                rs.getInt("category_id"),
                rs.getInt("supplier_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getInt("stock_qty"),
                rs.getBigDecimal("price")
        );
        p.setCategoryName(rs.getString("category_name"));
        p.setSupplierName(rs.getString("supplier_name"));
        return p;
    }
}
