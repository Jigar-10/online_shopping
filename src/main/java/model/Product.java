package model;

import java.math.BigDecimal;

public class Product {
    private Integer productId;
    private Integer categoryId;
    private String categoryName;
    private Integer supplierId;
    private String supplierName;
    private String name;
    private String description;
    private Integer stockQty;
    private BigDecimal price;

    public Product() {
        this.stockQty = 0;
        this.price = BigDecimal.ZERO;
    }

    public Product(Integer productId, Integer categoryId, Integer supplierId, String name, String description, Integer stockQty, BigDecimal price) {
        this.productId = productId;
        this.categoryId = categoryId;
        this.supplierId = supplierId;
        this.name = name;
        this.description = description;
        this.stockQty = stockQty != null ? stockQty : 0;
        this.price = price != null ? price : BigDecimal.ZERO;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Integer getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Integer supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStockQty() {
        return stockQty;
    }

    public void setStockQty(Integer stockQty) {
        this.stockQty = stockQty;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return name + " (\u20B9" + price + ")";
    }
}
