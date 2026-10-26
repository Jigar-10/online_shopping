package dao;

import config.DatabaseConnection;
import exception.DatabaseException;
import model.Review;
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

public class ReviewDao implements GenericDao<Review, Integer> {
    private static final Logger logger = LoggerFactory.getLogger(ReviewDao.class);

    private static final String BASE_SELECT =
            "SELECT r.review_id, r.customer_id, c.name AS customer_name, " +
            "r.product_id, p.name AS product_name, r.rating, r.comment " +
            "FROM REVIEW r " +
            "LEFT JOIN CUSTOMER c ON r.customer_id = c.customer_id " +
            "LEFT JOIN PRODUCT p ON r.product_id = p.product_id ";

    @Override
    public List<Review> findAll() {
        String sql = BASE_SELECT + "ORDER BY r.review_id ASC";
        List<Review> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding all reviews", e);
            throw new DatabaseException("Failed to load reviews: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
        return list;
    }

    @Override
    public Optional<Review> findById(Integer id) {
        String sql = BASE_SELECT + "WHERE r.review_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding review ID {}", id, e);
            throw new DatabaseException("Failed to find review: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
        return Optional.empty();
    }

    @Override
    public Review insert(Review r) {
        String sql = "INSERT INTO REVIEW (customer_id, product_id, rating, comment) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getCustomerId());
            ps.setInt(2, r.getProductId());
            ps.setInt(3, r.getRating() != null ? r.getRating() : 5);
            ps.setString(4, r.getComment());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    r.setReviewId(keys.getInt(1));
                }
            }
            return r;
        } catch (SQLException e) {
            logger.error("Error inserting review", e);
            if (e.getErrorCode() == 1452) {
                throw new DatabaseException("Selected Customer or Product does not exist.", e.getSQLState(), e.getErrorCode(), e);
            }
            throw new DatabaseException("Failed to insert review: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public boolean update(Review r) {
        String sql = "UPDATE REVIEW SET customer_id = ?, product_id = ?, rating = ?, comment = ? WHERE review_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, r.getCustomerId());
            ps.setInt(2, r.getProductId());
            ps.setInt(3, r.getRating() != null ? r.getRating() : 5);
            ps.setString(4, r.getComment());
            ps.setInt(5, r.getReviewId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating review ID {}", r.getReviewId(), e);
            if (e.getErrorCode() == 1452) {
                throw new DatabaseException("Selected Customer or Product does not exist.", e.getSQLState(), e.getErrorCode(), e);
            }
            throw new DatabaseException("Failed to update review: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM REVIEW WHERE review_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting review ID {}", id, e);
            throw new DatabaseException("Failed to delete review: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public List<Review> search(String query) {
        if (query == null || query.isBlank()) return findAll();
        String sql = BASE_SELECT +
                     "WHERE c.name LIKE ? OR p.name LIKE ? OR r.comment LIKE ? " +
                     "ORDER BY r.review_id ASC";
        String pattern = "%" + query.trim() + "%";
        List<Review> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 1; i <= 3; i++) ps.setString(i, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error searching reviews", e);
            throw new DatabaseException("Failed to search reviews: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
        return list;
    }

    private Review mapRow(ResultSet rs) throws SQLException {
        Review r = new Review(
                rs.getInt("review_id"),
                rs.getInt("customer_id"),
                rs.getInt("product_id"),
                rs.getInt("rating"),
                rs.getString("comment")
        );
        r.setCustomerName(rs.getString("customer_name"));
        r.setProductName(rs.getString("product_name"));
        return r;
    }
}
