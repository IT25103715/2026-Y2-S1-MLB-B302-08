package com.cinemahub.dto;

/**
 * Backing object for the customer "my account" self-service page - name plus
 * the basic contact fields that live directly on {@link com.cinemahub.model.User}
 * now that the standalone Profile entity (old Function 4) has been removed.
 */
public class AccountForm {

    private String name;
    private String phone;
    private String address;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
