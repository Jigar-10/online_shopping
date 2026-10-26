import exception.ValidationException;
import model.Address;
import model.CreditCard;
import model.Customer;
import model.Product;
import model.Review;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.ValidationService;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ValidationServiceTest {

    @Test
    @DisplayName("Should accept valid customer")
    void testValidCustomer() {
        Customer c = new Customer(null, "Aarav Sharma", "aarav@gmail.com", "9876500001", LocalDate.now(), "Male");
        assertDoesNotThrow(() -> ValidationService.validateCustomer(c));
    }

    @Test
    @DisplayName("Should reject invalid customer email")
    void testInvalidCustomerEmail() {
        Customer c = new Customer(null, "Test User", "invalid-email-address", "9876500001", LocalDate.now(), "Male");
        ValidationException ex = assertThrows(ValidationException.class, () -> ValidationService.validateCustomer(c));
        assertTrue(ex.getMessage().contains("Invalid email format"));
    }

    @Test
    @DisplayName("Should reject customer phone not having 10 digits")
    void testInvalidCustomerPhone() {
        Customer c = new Customer(null, "Test User", "test@example.com", "12345", LocalDate.now(), "Male");
        ValidationException ex = assertThrows(ValidationException.class, () -> ValidationService.validateCustomer(c));
        assertTrue(ex.getMessage().contains("10 digits"));
    }

    @Test
    @DisplayName("Should reject address with invalid pincode")
    void testInvalidAddressPincode() {
        Address a = new Address(null, 1, "MG Road", "Mumbai", "Maharashtra", "India", "4001");
        ValidationException ex = assertThrows(ValidationException.class, () -> ValidationService.validateAddress(a));
        assertTrue(ex.getMessage().contains("6 digits"));
    }

    @Test
    @DisplayName("Should reject product with negative price or stock")
    void testInvalidProductValues() {
        Product p = new Product(null, 1, 1, "Item", "Desc", -5, new BigDecimal("-100.00"));
        ValidationException ex = assertThrows(ValidationException.class, () -> ValidationService.validateProduct(p));
        assertNotNull(ex.getMessage());
    }

    @Test
    @DisplayName("Should validate credit card number and CVV")
    void testCreditCardValidation() {
        CreditCard invalidCard = new CreditCard(1, "1234", "12", LocalDate.now().plusYears(1));
        assertThrows(ValidationException.class, () -> ValidationService.validateCreditCard(invalidCard));

        CreditCard validCard = new CreditCard(1, "4111111111111001", "123", LocalDate.now().plusYears(1));
        assertDoesNotThrow(() -> ValidationService.validateCreditCard(validCard));
    }

    @Test
    @DisplayName("Should validate review rating boundaries (1-5)")
    void testReviewRatingBoundaries() {
        Review invalidReview = new Review(null, 1, 1, 10, "Great");
        assertThrows(ValidationException.class, () -> ValidationService.validateReview(invalidReview));

        Review validReview = new Review(null, 1, 1, 5, "Excellent");
        assertDoesNotThrow(() -> ValidationService.validateReview(validReview));
    }
}
