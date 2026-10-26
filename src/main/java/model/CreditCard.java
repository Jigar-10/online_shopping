package model;

import java.time.LocalDate;

public class CreditCard {
    private Integer paymentId;
    private String cardNumber;
    private String cvv;
    private LocalDate expiryDate;

    public CreditCard() {}

    public CreditCard(Integer paymentId, String cardNumber, String cvv, LocalDate expiryDate) {
        this.paymentId = paymentId;
        this.cardNumber = cardNumber;
        this.cvv = cvv;
        this.expiryDate = expiryDate;
    }

    public Integer getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Integer paymentId) {
        this.paymentId = paymentId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getMaskedCardNumber() {
        if (cardNumber == null || cardNumber.length() < 4) return "****";
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }

    @Override
    public String toString() {
        return "Card for Payment #" + paymentId + " (" + getMaskedCardNumber() + ")";
    }
}
