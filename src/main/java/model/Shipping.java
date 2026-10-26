package model;

public class Shipping {
    private Integer shipmentId;
    private Integer addressId;
    private String addressSummary; // helper for display
    private String carrier;
    private String trackingNo;
    private String status;

    public Shipping() {
        this.status = "Pending";
    }

    public Shipping(Integer shipmentId, Integer addressId, String carrier, String trackingNo, String status) {
        this.shipmentId = shipmentId;
        this.addressId = addressId;
        this.carrier = carrier;
        this.trackingNo = trackingNo;
        this.status = status != null ? status : "Pending";
    }

    public Integer getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(Integer shipmentId) {
        this.shipmentId = shipmentId;
    }

    public Integer getAddressId() {
        return addressId;
    }

    public void setAddressId(Integer addressId) {
        this.addressId = addressId;
    }

    public String getAddressSummary() {
        return addressSummary;
    }

    public void setAddressSummary(String addressSummary) {
        this.addressSummary = addressSummary;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getTrackingNo() {
        return trackingNo;
    }

    public void setTrackingNo(String trackingNo) {
        this.trackingNo = trackingNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Shipment #" + shipmentId + " (" + (trackingNo != null ? trackingNo : "No Track #") + " - " + status + ")";
    }
}
