package com.example.fusion0;

import java.util.HashMap;

public class UserInfo {
    String firstName, lastName, email, phoneNumber;

    public UserInfo(String first, String last, String email, String phoneNumber) {
        this.firstName = first;
        this.lastName = last;
        this.email = email;
        this.phoneNumber = parsed(phoneNumber);
    }

    public UserInfo(String first, String last, String email) {
        this.firstName = first;
        this.lastName = last;
        this.email = email;
    }

    private String parsed(String phoneNumber) {
        for (char num: phoneNumber.trim().toCharArray()) {
            if (!Character.isDigit(num)) {
                phoneNumber = phoneNumber.replace(String.valueOf(num), "");
            }
        }
        return phoneNumber;
    }

    public HashMap<String,Object> user() {
        HashMap<String, Object> user = new HashMap<>();

        user.put("First Name", this.firstName);
        user.put("Last Name", this.lastName);
        user.put("Email", this.email);
        user.put("Phone Number", this.phoneNumber);

        return user;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
