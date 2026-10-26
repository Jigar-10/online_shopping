package model;

import java.math.BigDecimal;

public class OrderItem {
    private Integer orderId;
    private Integer productId;
    private String productName; // helper
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal; // helper

    public OrderItem() {
        this.quantity = 1;
        this.unitPrice = BigDecimal.ZERO;
    }

    public OrderItem(Integer orderId, Integer productId, Integer quantity, BigDecimal unitPrice) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity != null ? quantity : 1;
        this.unitPrice = unitPrice != null ? unitPrice : BigDecimal.ZERO;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getSubtotal() {
        if (subtotal != null) return subtotal;
        if (unitPrice != null && quantity != null) {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    @Override
    public String toString() {
        return "Order #" + orderId + " - Item: " + (productName != null ? productName : productId) + " (Qty: " + quantity + ", \u20B9" + unitPrice + ")";
    }
}
