package dao;

import config.DatabaseConnection;
import exception.DatabaseException;
import model.Address;
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

public class AddressDao implements GenericDao<Address, Integer> {
    private static final Logger logger = LoggerFactory.getLogger(AddressDao.class);

    private static final String BASE_SELECT =
            "SELECT a.address_id, a.customer_id, c.name AS customer_name, a.street, a.city, a.state, a.country, a.pincode " +
            "FROM ADDRESS a LEFT JOIN CUSTOMER c ON a.customer_id = c.customer_id ";

    @Override
    public List<Address> findAll() {
        String sql = BASE_SELECT + "ORDER BY a.address_id ASC";
        List<Address> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding addresses", e);
            throw new DatabaseException("Failed to load addresses: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
        return list;
    }

    @Override
    public Optional<Address> findById(Integer id) {
        String sql = BASE_SELECT + "WHERE a.address_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding address by ID {}", id, e);
            throw new DatabaseException("Failed to find address: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
        return Optional.empty();
    }

    @Override
    public Address insert(Address a) {
        String sql = "INSERT INTO ADDRESS (customer_id, street, city, state, country, pincode) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getCustomerId());
            ps.setString(2, a.getStreet());
            ps.setString(3, a.getCity());
            ps.setString(4, a.getState());
            ps.setString(5, a.getCountry());
            ps.setString(6, a.getPincode());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    a.setAddressId(keys.getInt(1));
                }
            }
            return a;
        } catch (SQLException e) {
            logger.error("Error inserting address", e);
            if (e.getErrorCode() == 1452) {
                throw new DatabaseException("Selected customer (ID " + a.getCustomerId() + ") does not exist.", e.getSQLState(), e.getErrorCode(), e);
            }
            throw new DatabaseException("Failed to insert address: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public boolean update(Address a) {
        String sql = "UPDATE ADDRESS SET customer_id = ?, street = ?, city = ?, state = ?, country = ?, pincode = ? WHERE address_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, a.getCustomerId());
            ps.setString(2, a.getStreet());
            ps.setString(3, a.getCity());
            ps.setString(4, a.getState());
            ps.setString(5, a.getCountry());
            ps.setString(6, a.getPincode());
            ps.setInt(7, a.getAddressId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating address ID {}", a.getAddressId(), e);
            if (e.getErrorCode() == 1452) {
                throw new DatabaseException("Selected customer does not exist.", e.getSQLState(), e.getErrorCode(), e);
            }
            throw new DatabaseException("Failed to update address: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM ADDRESS WHERE address_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting address ID {}", id, e);
            if (e.getErrorCode() == 1451) {
                throw new DatabaseException("Cannot delete address (ID " + id + ") because it is linked to shipments.", e.getSQLState(), e.getErrorCode(), e);
            }
            throw new DatabaseException("Failed to delete address: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public List<Address> search(String query) {
        if (query == null || query.isBlank()) return findAll();
        String sql = BASE_SELECT +
                     "WHERE a.street LIKE ? OR a.city LIKE ? OR a.state LIKE ? OR a.pincode LIKE ? OR c.name LIKE ? " +
                     "ORDER BY a.address_id ASC";
        String pattern = "%" + query.trim() + "%";
        List<Address> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 1; i <= 5; i++) ps.setString(i, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error searching addresses", e);
            throw new DatabaseException("Failed to search addresses: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
        return list;
    }

    public List<ComboItem<Integer>> findAllAsComboItems() {
        List<ComboItem<Integer>> items = new ArrayList<>();
        for (Address a : findAll()) {
            items.add(new ComboItem<>(a.getAddressId(), "ID " + a.getAddressId() + ": " + a.getCity() + " - " + a.getStreet() + " (" + a.getCustomerName() + ")"));
        }
        return items;
    }

    private Address mapRow(ResultSet rs) throws SQLException {
        Address a = new Address(
                rs.getInt("address_id"),
                rs.getInt("customer_id"),
                rs.getString("street"),
                rs.getString("city"),
                rs.getString("state"),
                rs.getString("country"),
                rs.getString("pincode")
        );
        a.setCustomerName(rs.getString("customer_name"));
        return a;
    }
}
