package dao;

import config.DatabaseConnection;
import exception.DatabaseException;
import model.ComboItem;
import model.Shipping;
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

public class ShippingDao implements GenericDao<Shipping, Integer> {
    private static final Logger logger = LoggerFactory.getLogger(ShippingDao.class);

    private static final String BASE_SELECT =
            "SELECT s.shipment_id, s.address_id, s.carrier, s.tracking_no, s.status, " +
            "CONCAT(a.city, ', ', a.state, ' (', c.name, ')') AS address_summary " +
            "FROM SHIPPING s " +
            "LEFT JOIN ADDRESS a ON s.address_id = a.address_id " +
            "LEFT JOIN CUSTOMER c ON a.customer_id = c.customer_id ";

    @Override
    public List<Shipping> findAll() {
        String sql = BASE_SELECT + "ORDER BY s.shipment_id ASC";
        List<Shipping> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding all shipments", e);
            throw new DatabaseException("Failed to load shipments: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
        return list;
    }

    @Override
    public Optional<Shipping> findById(Integer id) {
        String sql = BASE_SELECT + "WHERE s.shipment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding shipment by ID {}", id, e);
            throw new DatabaseException("Failed to find shipment: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
        return Optional.empty();
    }

    @Override
    public Shipping insert(Shipping s) {
        String sql = "INSERT INTO SHIPPING (address_id, carrier, tracking_no, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.getAddressId());
            ps.setString(2, s.getCarrier());
            ps.setString(3, s.getTrackingNo());
            ps.setString(4, s.getStatus() != null ? s.getStatus() : "Pending");
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    s.setShipmentId(keys.getInt(1));
                }
            }
            return s;
        } catch (SQLException e) {
            logger.error("Error inserting shipment", e);
            if (e.getErrorCode() == 1062) {
                throw new DatabaseException("A shipment with tracking number '" + s.getTrackingNo() + "' already exists.", e.getSQLState(), e.getErrorCode(), e);
            } else if (e.getErrorCode() == 1452) {
                throw new DatabaseException("Selected address (ID " + s.getAddressId() + ") does not exist.", e.getSQLState(), e.getErrorCode(), e);
            }
            throw new DatabaseException("Failed to insert shipment: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public boolean update(Shipping s) {
        String sql = "UPDATE SHIPPING SET address_id = ?, carrier = ?, tracking_no = ?, status = ? WHERE shipment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, s.getAddressId());
            ps.setString(2, s.getCarrier());
            ps.setString(3, s.getTrackingNo());
            ps.setString(4, s.getStatus());
            ps.setInt(5, s.getShipmentId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating shipment ID {}", s.getShipmentId(), e);
            if (e.getErrorCode() == 1062) {
                throw new DatabaseException("Another shipment with tracking number '" + s.getTrackingNo() + "' already exists.", e.getSQLState(), e.getErrorCode(), e);
            }
            throw new DatabaseException("Failed to update shipment: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM SHIPPING WHERE shipment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting shipment ID {}", id, e);
            if (e.getErrorCode() == 1451) {
                throw new DatabaseException("Cannot delete shipment (ID " + id + ") because it is linked to orders.", e.getSQLState(), e.getErrorCode(), e);
            }
            throw new DatabaseException("Failed to delete shipment: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    @Override
    public List<Shipping> search(String query) {
        if (query == null || query.isBlank()) return findAll();
        String sql = BASE_SELECT +
                     "WHERE s.carrier LIKE ? OR s.tracking_no LIKE ? OR s.status LIKE ? " +
                     "ORDER BY s.shipment_id ASC";
        String pattern = "%" + query.trim() + "%";
        List<Shipping> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error searching shipments", e);
            throw new DatabaseException("Failed to search shipments: " + e.getMessage(), e.getSQLState(), e.getErrorCode(), e);
        }
        return list;
    }

    public List<ComboItem<Integer>> findAllAsComboItems() {
        List<ComboItem<Integer>> items = new ArrayList<>();
        for (Shipping s : findAll()) {
            items.add(new ComboItem<>(s.getShipmentId(), "Shipment #" + s.getShipmentId() + " (" + (s.getTrackingNo() != null ? s.getTrackingNo() : s.getCarrier()) + ") - " + s.getStatus()));
        }
        return items;
    }

    private Shipping mapRow(ResultSet rs) throws SQLException {
        Shipping s = new Shipping(
                rs.getInt("shipment_id"),
                rs.getInt("address_id"),
                rs.getString("carrier"),
                rs.getString("tracking_no"),
                rs.getString("status")
        );
        s.setAddressSummary(rs.getString("address_summary"));
        return s;
    }
}
