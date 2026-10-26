package dao;

import config.DatabaseConnection;
import exception.DatabaseException;
import model.Category;
import model.ComboItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoryDao implements GenericDao<Category, Integer> {
    private static final Logger logger = LoggerFactory.getLogger(CategoryDao.class);

    @Override
    public List<Category> findAll() {
        String sql = "SELECT category_id, name FROM CATEGORY ORDER BY category_id ASC";
        List<Category> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding categories", e);
            throw new DatabaseException("Failed to load categories: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
        return list;
    }

    @Override
    public Optional<Category> findById(Integer id) {
        String sql = "SELECT category_id, name FROM CATEGORY WHERE category_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding category ID {}", id, e);
            throw new DatabaseException("Failed to find category: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
        return Optional.empty();
    }

    @Override
    public Category insert(Category c) {
        String sql = "INSERT INTO CATEGORY (name) VALUES (?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getName());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    c.setCategoryId(keys.getInt(1));
                }
            }
            return c;
        } catch (SQLException e) {
            logger.error("Error inserting category", e);
            if (e.getErrorCode() == 1062) {
                throw new DatabaseException("A category named '" + c.getName() + "' already exists.", e.getSQLState(), e.getErrorCode(), e);
            }
            throw new DatabaseException("Failed to insert category: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public boolean update(Category c) {
        String sql = "UPDATE CATEGORY SET name = ? WHERE category_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getName());
            ps.setInt(2, c.getCategoryId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating category ID {}", c.getCategoryId(), e);
            if (e.getErrorCode() == 1062) {
                throw new DatabaseException("A category named '" + c.getName() + "' already exists.", e.getSQLState(), e.getErrorCode(), e);
            }
            throw new DatabaseException("Failed to update category: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM CATEGORY WHERE category_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting category ID {}", id, e);
            if (e.getErrorCode() == 1451) {
                throw new DatabaseException("Cannot delete category (ID " + id + ") because products belong to it.", e.getSQLState(), e.getErrorCode(), e);
            }
            throw new DatabaseException("Failed to delete category: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public List<Category> search(String query) {
        if (query == null || query.isBlank()) return findAll();
        String sql = "SELECT category_id, name FROM CATEGORY WHERE name LIKE ? ORDER BY category_id ASC";
        String pattern = "%" + query.trim() + "%";
        List<Category> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error searching categories", e);
            throw new DatabaseException("Failed to search categories: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
        return list;
    }

    public List<ComboItem<Integer>> findAllAsComboItems() {
        List<ComboItem<Integer>> items = new ArrayList<>();
        for (Category c : findAll()) {
            items.add(new ComboItem<>(c.getCategoryId(), c.getName()));
        }
        return items;
    }

    private Category mapRow(ResultSet rs) throws SQLException {
        return new Category(
                rs.getInt("category_id"),
                rs.getString("name")
        );
    }
}
