package model;

public class DigitalWallet {
    private Integer paymentId;
    private String provider;
    private String walletRef;

    public DigitalWallet() {}

    public DigitalWallet(Integer paymentId, String provider, String walletRef) {
        this.paymentId = paymentId;
        this.provider = provider;
        this.walletRef = walletRef;
    }

    public Integer getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Integer paymentId) {
        this.paymentId = paymentId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getWalletRef() {
        return walletRef;
    }

    public void setWalletRef(String walletRef) {
        this.walletRef = walletRef;
    }

    @Override
    public String toString() {
        return provider + " (" + walletRef + ") - Payment #" + paymentId;
    }
}
