package com.example.fusion0;

import com.google.firebase.firestore.PropertyName;

import java.util.HashMap;

public class UserInfo {
    String firstName, lastName, email, phoneNumber;
    Firebase firebase;
    Boolean edit;

    public UserInfo() {
        this.edit = false;
    }

    public UserInfo(String first, String last, String email, String phoneNumber, Boolean edit) {
        this.firstName = first;
        this.lastName = last;
        this.email = email;
        this.phoneNumber = parsed(phoneNumber);
        this.firebase = new Firebase();
        this.edit = edit;
    }

    public UserInfo(String first, String last, String email) {
        this.firstName = first;
        this.lastName = last;
        this.email = email;
        this.firebase = new Firebase();
        this.edit = false;
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

        user.put("email", this.email);
        user.put("first name", this.firstName);
        user.put("last name", this.lastName);
        user.put("phone number", this.phoneNumber);

        return user;
    }
    @PropertyName("first name")
    public String getFirstName() {
        return firstName;
    }

    @PropertyName("first name")
    public void setFirstName(String firstName) {
        updateUser("first name", firstName);
        this.firstName = firstName;
    }

    @PropertyName("last name")
    public String getLastName() {
        return lastName;
    }

    @PropertyName("last name")
    public void setLastName(String lastName) {
        updateUser("last name", lastName);
        this.lastName = lastName;
    }

    @PropertyName("email")
    public String getEmail() {
        return email;
    }

    @PropertyName("email")
    public void setEmail(String email) {
        updateUser("email", email);
        this.email = email;
    }

    @PropertyName("phone number")
    public String getPhoneNumber() {
        return phoneNumber;
    }

    @PropertyName("phone number")
    public void setPhoneNumber(String phoneNumber) {
        updateUser("phone number", phoneNumber);
        this.phoneNumber = phoneNumber;
    }

    private void updateUser(String field, String newItem) {
        if (edit) firebase.editUser(this.getEmail(), field, newItem);
    }
}
