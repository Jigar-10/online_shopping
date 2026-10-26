package model;

import java.time.LocalDateTime;

public class Cart {
    private Integer cartId;
    private Integer customerId;
    private String customerName; // helper for display
    private LocalDateTime createdOn;

    public Cart() {
        this.createdOn = LocalDateTime.now();
    }

    public Cart(Integer cartId, Integer customerId, LocalDateTime createdOn) {
        this.cartId = cartId;
        this.customerId = customerId;
        this.createdOn = createdOn != null ? createdOn : LocalDateTime.now();
    }

    public Integer getCartId() {
        return cartId;
    }

    public void setCartId(Integer cartId) {
        this.cartId = cartId;
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

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    @Override
    public String toString() {
        return "Cart #" + cartId + " - " + (customerName != null ? customerName : "Customer " + customerId);
    }
}
