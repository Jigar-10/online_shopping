package model;

import java.time.LocalDate;

public class Customer {
    private Integer customerId;
    private String name;
    private String email;
    private String phone;
    private LocalDate registeredOn;
    private String gender;

    public Customer() {
        this.registeredOn = LocalDate.now();
    }

    public Customer(Integer customerId, String name, String email, String phone, LocalDate registeredOn, String gender) {
        this.customerId = customerId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.registeredOn = registeredOn != null ? registeredOn : LocalDate.now();
        this.gender = gender;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getRegisteredOn() {
        return registeredOn;
    }

    public void setRegisteredOn(LocalDate registeredOn) {
        this.registeredOn = registeredOn;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @Override
    public String toString() {
        return name + " (" + email + ")";
    }
}
