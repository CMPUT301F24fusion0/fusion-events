package com.example.fusion0;

import com.google.firebase.firestore.PropertyName;

import java.util.HashMap;
import java.util.Objects;

/**
 * This class contains the information for users.
 */
public class UserInfo {
    String firstName, lastName, email, phoneNumber, deviceID;
    UserFirestore firebase;
    Boolean edit;

    /**
     * Used by firestore to create objects in findUser()
     */
    public UserInfo() {
        this.edit = false;
    }

    /**
     * Default constructor if phone number is provided
     * @param first first name
     * @param last last name
     * @param email email address
     * @param phoneNumber phone number
     */
    public UserInfo(String first, String last, String email, String phoneNumber, String dID) {
        this.firstName = first;
        this.lastName = last;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.firebase = new UserFirestore();
        this.edit = false;
        this.deviceID = dID;
    }

    /**
     * Constructor if phone number isn't provided
     * @param first first name
     * @param last last name
     * @param email email address
     */
    public UserInfo(String first, String last, String email, String dID) {
        this.firstName = first;
        this.lastName = last;
        this.email = email;
        this.firebase = new UserFirestore();
        this.edit = false;
        this.deviceID = dID;
    }

    /**
     * For efficient additions into Firestore, hashmaps are used
     * @return user stores all information regarding the user in a hashmap
     */
    public HashMap<String,Object> user() {
        HashMap<String, Object> user = new HashMap<>();
        user.put("dID", this.deviceID);
        user.put("email", this.email);
        user.put("first name", this.firstName);
        user.put("last name", this.lastName);
        user.put("phone number", this.phoneNumber);

        return user;
    }

    /**
     * Gets device id
     * @return device id
     */
    @PropertyName("dID")
    public String getDeviceID() {
        return deviceID;
    }

    /**
     * Sets device ID
     * @param deviceID the device ID
     */
    @PropertyName("dID")
    public void setDeviceID(String deviceID) {
        updateUser("DID", deviceID);
        this.deviceID = deviceID;
    }

    /**
     * Gets first name
     * @return first name
     */
    @PropertyName("first name")
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets first name
     * @param firstName first name
     */
    @PropertyName("first name")
    public void setFirstName(String firstName) {
        updateUser("first name", firstName);
        this.firstName = firstName;
    }

    /**
     * Gets last name
     * @return last name
     */
    @PropertyName("last name")
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets last name
     * @param lastName last name
     */
    @PropertyName("last name")
    public void setLastName(String lastName) {
        updateUser("last name", lastName);
        this.lastName = lastName;
    }

    /**
     * Gets email
     * @return email
     */
    @PropertyName("email")
    public String getEmail() {
        return email;
    }

    /**
     * Sets email
     * @param email email address
     */
    @PropertyName("email")
    public void setEmail(String email) {
        updateUser("email", email);
        this.email = email;
    }

    /**
     * Gets phone number
     * @return phone number (may be null)
     */
    @PropertyName("phone number")
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Sets phone number
     * @param phoneNumber phone number
     */
    @PropertyName("phone number")
    public void setPhoneNumber(String phoneNumber) {
        updateUser("phone number", phoneNumber);
        this.phoneNumber = phoneNumber;
    }

    /**
     * If edit mode is on then the user can be updated, this prevents findUser() from attempting to update
     * the database while constructing the found user.
     * @param field attribute to change
     * @param newItem new attribute
     */
    private void updateUser(String field, String newItem) {
        if (edit) firebase.editUser(this, field, newItem);
    }

    /**
     * Compares the User and Obj by seeing if their equal, of the same instance, or have the same email
     * @param obj object to compare to
     * @return true if object is same, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof UserInfo)) {
            return false;
        }

        UserInfo user = (UserInfo) obj;
        return Objects.equals(this.getDeviceID(), user.getDeviceID());
    }

    /**
     * Redo the hash for the attribute we compared above
     * @return hash for the email
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.getDeviceID());
    }

    /**
     * Edits can only be made when the edit mode is on, it is false by default
     * @param changeEdit a boolean used to denote whether edit is on or off
     */
    public void editMode(boolean changeEdit) {
        this.edit = changeEdit;
    }
}
