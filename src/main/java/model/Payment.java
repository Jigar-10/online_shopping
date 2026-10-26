package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {
    private Integer paymentId;
    private Integer orderId;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private String paymentType;
    private String status;

    public Payment() {
        this.paymentDate = LocalDateTime.now();
        this.amount = BigDecimal.ZERO;
        this.paymentType = "Credit Card";
        this.status = "Completed";
    }

    public Payment(Integer paymentId, Integer orderId, BigDecimal amount, LocalDateTime paymentDate, String paymentType, String status) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.amount = amount != null ? amount : BigDecimal.ZERO;
        this.paymentDate = paymentDate != null ? paymentDate : LocalDateTime.now();
        this.paymentType = paymentType != null ? paymentType : "Credit Card";
        this.status = status != null ? status : "Completed";
    }

    public Integer getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Integer paymentId) {
        this.paymentId = paymentId;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Payment #" + paymentId + " - Order #" + orderId + " (\u20B9" + amount + ", " + paymentType + ")";
    }
}
