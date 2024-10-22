package com.example.fusion0;

import java.util.HashMap;

public class UserInfo {
    String firstName, lastName, email, phoneNumber;
    Firebase firebase;

    public UserInfo(String first, String last, String email, String phoneNumber) {
        this.firstName = first;
        this.lastName = last;
        this.email = email;
        this.phoneNumber = parsed(phoneNumber);
        this.firebase = new Firebase();
    }

    public UserInfo(String first, String last, String email) {
        this.firstName = first;
        this.lastName = last;
        this.email = email;
    }

    private String parsed(String phone) {
        for (char num: phone.trim().toCharArray()) {
            if (!Character.isDigit(num)) {
                phone = phone.replace(String.valueOf(num), "");
            }
        }
        return phone;
    }

    public HashMap<String,Object> user() {
        HashMap<String, Object> user = new HashMap<>();

        user.put("first name", this.firstName);
        user.put("last name", this.lastName);
        user.put("email", this.email);
        user.put("phone number", this.phoneNumber);

        return user;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
        firebase.editUser(this.getEmail(), "first name", firstName);

    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        firebase.editUser(this.getEmail(), "last name", lastName);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        firebase.editUser(this.getEmail(), "email", email);
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        firebase.editUser(this.getEmail(), "phone number", phoneNumber);
    }
}
