package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Order {
    private Integer orderId;
    private Integer customerId;
    private String customerName; // helper
    private Integer shipmentId;
    private String trackingNo;   // helper
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private String status;

    public Order() {
        this.orderDate = LocalDateTime.now();
        this.totalAmount = BigDecimal.ZERO;
        this.status = "Pending";
    }

    public Order(Integer orderId, Integer customerId, Integer shipmentId, LocalDateTime orderDate, BigDecimal totalAmount, String status) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.shipmentId = shipmentId;
        this.orderDate = orderDate != null ? orderDate : LocalDateTime.now();
        this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
        this.status = status != null ? status : "Pending";
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Integer getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(Integer shipmentId) {
        this.shipmentId = shipmentId;
    }

    public String getTrackingNo() {
        return trackingNo;
    }

    public void setTrackingNo(String trackingNo) {
        this.trackingNo = trackingNo;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Order #" + orderId + " (" + (customerName != null ? customerName : "Cust " + customerId) + " - \u20B9" + totalAmount + ")";
    }
}
