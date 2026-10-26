package service;

import exception.ValidationException;
import model.*;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public class ValidationService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$");
    private static final Pattern PINCODE_PATTERN = Pattern.compile("^[0-9]{6}$");
    private static final Pattern CARD_PATTERN = Pattern.compile("^[0-9]{13,19}$");
    private static final Pattern CVV_PATTERN = Pattern.compile("^[0-9]{3,4}$");

    public static void validateCustomer(Customer c) {
        if (c.getName() == null || c.getName().trim().isEmpty()) {
            throw new ValidationException("Customer name cannot be empty.");
        }
        if (c.getName().length() > 50) {
            throw new ValidationException("Customer name cannot exceed 50 characters.");
        }
        if (c.getEmail() == null || c.getEmail().trim().isEmpty()) {
            throw new ValidationException("Customer email is required.");
        }
        if (!EMAIL_PATTERN.matcher(c.getEmail().trim()).matches()) {
            throw new ValidationException("Invalid email format: '" + c.getEmail() + "'.");
        }
        if (c.getPhone() != null && !c.getPhone().isBlank() && !PHONE_PATTERN.matcher(c.getPhone().trim()).matches()) {
            throw new ValidationException("Phone number must be exactly 10 digits.");
        }
    }

    public static void validateAddress(Address a) {
        if (a.getCustomerId() == null || a.getCustomerId() <= 0) {
            throw new ValidationException("Please select a valid customer.");
        }
        if (a.getStreet() == null || a.getStreet().trim().isEmpty()) {
            throw new ValidationException("Street address is required.");
        }
        if (a.getCity() == null || a.getCity().trim().isEmpty()) {
            throw new ValidationException("City is required.");
        }
        if (a.getState() == null || a.getState().trim().isEmpty()) {
            throw new ValidationException("State is required.");
        }
        if (a.getPincode() == null || !PINCODE_PATTERN.matcher(a.getPincode().trim()).matches()) {
            throw new ValidationException("Pincode must be exactly 6 digits.");
        }
    }

    public static void validateShipping(Shipping s) {
        if (s.getAddressId() == null || s.getAddressId() <= 0) {
            throw new ValidationException("Please select an address for shipping.");
        }
        if (s.getCarrier() == null || s.getCarrier().trim().isEmpty()) {
            throw new ValidationException("Shipping carrier is required.");
        }
        if (s.getTrackingNo() == null || s.getTrackingNo().trim().isEmpty()) {
            throw new ValidationException("Tracking number is required.");
        }
    }

    public static void validateCategory(Category c) {
        if (c.getName() == null || c.getName().trim().isEmpty()) {
            throw new ValidationException("Category name is required.");
        }
    }

    public static void validateSupplier(Supplier s) {
        if (s.getName() == null || s.getName().trim().isEmpty()) {
            throw new ValidationException("Supplier name is required.");
        }
        if (s.getContactEmail() != null && !s.getContactEmail().isBlank() && !EMAIL_PATTERN.matcher(s.getContactEmail().trim()).matches()) {
            throw new ValidationException("Invalid supplier email format.");
        }
    }

    public static void validateProduct(Product p) {
        if (p.getName() == null || p.getName().trim().isEmpty()) {
            throw new ValidationException("Product name is required.");
        }
        if (p.getCategoryId() == null || p.getCategoryId() <= 0) {
            throw new ValidationException("Please select a product category.");
        }
        if (p.getSupplierId() == null || p.getSupplierId() <= 0) {
            throw new ValidationException("Please select a product supplier.");
        }
        if (p.getPrice() == null || p.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Price must be greater than or equal to 0.00.");
        }
        if (p.getStockQty() == null || p.getStockQty() < 0) {
            throw new ValidationException("Stock quantity cannot be negative.");
        }
    }

    public static void validateCart(Cart c) {
        if (c.getCustomerId() == null || c.getCustomerId() <= 0) {
            throw new ValidationException("Please select a customer for this cart.");
        }
    }

    public static void validateCartItem(CartItem ci) {
        if (ci.getCartId() == null || ci.getCartId() <= 0) {
            throw new ValidationException("Please select a cart.");
        }
        if (ci.getProductId() == null || ci.getProductId() <= 0) {
            throw new ValidationException("Please select a product.");
        }
        if (ci.getQuantity() == null || ci.getQuantity() <= 0) {
            throw new ValidationException("Quantity must be at least 1.");
        }
    }

    public static void validateOrder(Order o) {
        if (o.getCustomerId() == null || o.getCustomerId() <= 0) {
            throw new ValidationException("Please select a customer for this order.");
        }
        if (o.getTotalAmount() == null || o.getTotalAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Total amount cannot be negative.");
        }
    }

    public static void validateOrderItem(OrderItem oi) {
        if (oi.getOrderId() == null || oi.getOrderId() <= 0) {
            throw new ValidationException("Please select an order.");
        }
        if (oi.getProductId() == null || oi.getProductId() <= 0) {
            throw new ValidationException("Please select a product.");
        }
        if (oi.getQuantity() == null || oi.getQuantity() <= 0) {
            throw new ValidationException("Quantity must be at least 1.");
        }
        if (oi.getUnitPrice() == null || oi.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Unit price cannot be negative.");
        }
    }

    public static void validatePayment(Payment p) {
        if (p.getOrderId() == null || p.getOrderId() <= 0) {
            throw new ValidationException("Please select an order for payment.");
        }
        if (p.getAmount() == null || p.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Payment amount cannot be negative.");
        }
        if (p.getPaymentType() == null || p.getPaymentType().trim().isEmpty()) {
            throw new ValidationException("Payment type is required.");
        }
    }

    public static void validateCreditCard(CreditCard card) {
        if (card.getPaymentId() == null || card.getPaymentId() <= 0) {
            throw new ValidationException("Please select a valid payment ID.");
        }
        if (card.getCardNumber() == null || !CARD_PATTERN.matcher(card.getCardNumber().replaceAll("\\s+", "")).matches()) {
            throw new ValidationException("Card number must contain between 13 and 19 digits.");
        }
        if (card.getCvv() == null || !CVV_PATTERN.matcher(card.getCvv().trim()).matches()) {
            throw new ValidationException("CVV must be 3 or 4 digits.");
        }
        if (card.getExpiryDate() == null) {
            throw new ValidationException("Expiry date is required.");
        }
    }

    public static void validateDigitalWallet(DigitalWallet wallet) {
        if (wallet.getPaymentId() == null || wallet.getPaymentId() <= 0) {
            throw new ValidationException("Please select a valid payment ID.");
        }
        if (wallet.getProvider() == null || wallet.getProvider().trim().isEmpty()) {
            throw new ValidationException("Wallet provider (e.g. Paytm, PhonePe, Google Pay) is required.");
        }
        if (wallet.getWalletRef() == null || wallet.getWalletRef().trim().isEmpty()) {
            throw new ValidationException("Wallet reference ID is required.");
        }
    }

    public static void validateReview(Review r) {
        if (r.getCustomerId() == null || r.getCustomerId() <= 0) {
            throw new ValidationException("Please select a customer.");
        }
        if (r.getProductId() == null || r.getProductId() <= 0) {
            throw new ValidationException("Please select a product.");
        }
        if (r.getRating() == null || r.getRating() < 1 || r.getRating() > 5) {
            throw new ValidationException("Rating must be between 1 and 5 stars.");
        }
    }
}
