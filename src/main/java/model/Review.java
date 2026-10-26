package model;

public class Review {
    private Integer reviewId;
    private Integer customerId;
    private String customerName; // helper
    private Integer productId;
    private String productName;  // helper
    private Integer rating;
    private String comment;

    public Review() {
        this.rating = 5;
    }

    public Review(Integer reviewId, Integer customerId, Integer productId, Integer rating, String comment) {
        this.reviewId = reviewId;
        this.customerId = customerId;
        this.productId = productId;
        this.rating = rating != null ? rating : 5;
        this.comment = comment;
    }

    public Integer getReviewId() {
        return reviewId;
    }

    public void setReviewId(Integer reviewId) {
        this.reviewId = reviewId;
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

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getStars() {
        if (rating == null) return "";
        return "\u2605".repeat(Math.max(0, Math.min(5, rating))) +
               "\u2606".repeat(Math.max(0, 5 - Math.min(5, rating)));
    }

    @Override
    public String toString() {
        return "Review #" + reviewId + " (" + rating + " \u2605)";
    }
}
