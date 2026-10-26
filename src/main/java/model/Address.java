package model;

public class Address {
    private Integer addressId;
    private Integer customerId;
    private String customerName; // helper for display
    private String street;
    private String city;
    private String state;
    private String country;
    private String pincode;

    public Address() {
        this.country = "India";
    }

    public Address(Integer addressId, Integer customerId, String street, String city, String state, String country, String pincode) {
        this.addressId = addressId;
        this.customerId = customerId;
        this.street = street;
        this.city = city;
        this.state = state;
        this.country = country != null ? country : "India";
        this.pincode = pincode;
    }

    public Integer getAddressId() {
        return addressId;
    }

    public void setAddressId(Integer addressId) {
        this.addressId = addressId;
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

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getFullAddress() {
        return street + ", " + city + ", " + state + " - " + pincode;
    }

    @Override
    public String toString() {
        return "ID " + addressId + ": " + getFullAddress();
    }
}
