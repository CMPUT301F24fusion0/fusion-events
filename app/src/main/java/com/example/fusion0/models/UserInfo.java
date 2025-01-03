package com.example.fusion0.models;

import com.example.fusion0.helpers.UserFirestore;
import com.google.firebase.firestore.PropertyName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;

/**
 * This class contains the information for users.
 * @author Sehej Brar
 */
public class UserInfo {
    String firstName, lastName, email, phoneNumber, deviceID, numAccepted, numDeclined;
    ArrayList<String> notifications, events, homepagenotifications;
    Boolean edit;


    /**
     * @author Sehej Brar
     * Used by firestore to create objects in findUser()
     */
    public UserInfo() {
        this.edit = false;
    }

    /**
     * @author Sehej Brar
     * Default constructor if phone number is provided
     * @param notifications notifications
     * @param first first name
     * @param last last name
     * @param email email
     * @param phoneNumber phone number
     * @param dID device id
     * @param events list of events
     */
    public UserInfo(ArrayList<String> notifications, String first, String last, String email, String phoneNumber, String dID, ArrayList<String> events) {
        this.notifications = notifications;
        this.homepagenotifications = new ArrayList<>();
        this.firstName = first;
        this.lastName = last;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.edit = false;
        this.deviceID = dID;
        this.events = new ArrayList<>();
        this.numAccepted = "0";
        this.numDeclined = "0";
    }

    /**
     * @author Sehej Brar
     * Constructor if phone number isn't provided
     * @param first first name
     * @param last last name
     * @param email email address
     */
    public UserInfo(ArrayList<String> notifications, String first, String last, String email, String dID, ArrayList<String> events) {
        this.notifications = notifications;
        this.homepagenotifications = new ArrayList<>();
        this.firstName = first;
        this.lastName = last;
        this.email = email;
        this.edit = false;
        this.deviceID = dID;
        this.events = new ArrayList<>();
        this.numAccepted = "0";
        this.numDeclined = "0";
    }

    /**
     * @author Sehej Brar
     * For efficient additions into Firestore, hashmaps are used
     * @return user stores all information regarding the user in a hashmap
     */
    public HashMap<String,Object> user() {
        HashMap<String, Object> user = new HashMap<>();
        user.put("notifications", this.notifications);
        user.put("homepagenotifications", this.homepagenotifications);
        user.put("dID", this.deviceID);
        user.put("email", this.email);
        user.put("first name", this.firstName);
        user.put("last name", this.lastName);
        user.put("phone number", this.phoneNumber);
        user.put("events", this.events);
        user.put("numaccepted", this.numAccepted);
        user.put("numdeclined", this.numDeclined);

        return user;
    }

    /**
     * Gets the number of accepted events for the user
     * @return string of number of accepted events
     */
    @PropertyName("numaccepted")
    public String getNumAccepted() {
        return numAccepted;
    }

    /**
     * Sets number of accepted events
     * @param numAccepted number of accepted events
     */
    @PropertyName("numaccepted")
    public void setNumAccepted(String numAccepted) {
        this.numAccepted = numAccepted;
        updateUser("numAccepted", new ArrayList<String>(Collections.singletonList(numAccepted)));
    }

    /**
     * Gets number of events declined
     * @return number of events declined
     */
    @PropertyName("numdeclined")
    public String getNumDeclined() {
        return numDeclined;
    }

    /**
     * Sets number of events declined
     * @param numDeclined number of declined events
     */
    @PropertyName("numdeclined")
    public void setNumDeclined(String numDeclined) {
        this.numDeclined = numDeclined;
        updateUser("numDeclined", new ArrayList<String>(Collections.singletonList(numDeclined)));

    }

    /**
     * Gets notifications for home page
     * @author Sehej Brar
     * @return array list of strings of notifications
     */
    @PropertyName("homepagenotifications")
    public ArrayList<String> getHomePageNotifications() {
        return homepagenotifications;
    }

    /**
     * Sets notifications for home page
     * @author Sehej Brar
     * @param homePageNotifications an array list of strings of notifications
     */
    @PropertyName("homepagenotifications")
    public void setHomePageNotifications(ArrayList<String> homePageNotifications) {
        this.homepagenotifications = homePageNotifications;
        updateUser("homePageNotifications", this.homepagenotifications);
    }

    /**
     * @author Sehej Brar
     * Adds notification to the ArrayList
     * @param title title
     * @param body body
     * @param flag flag for invite for info
     * @param eventId event id
     */
    public void addHomePageNotifications (String title, String body, String flag, String eventId) {
        this.homepagenotifications.add(title);
        this.homepagenotifications.add(body);
        this.homepagenotifications.add(flag);
        this.homepagenotifications.add(eventId);
        updateUser("homepagenotifications", this.homepagenotifications);
    }

    /**
     * @author Sehej Brar
     * Gets the events for a user (on fragment_waitlist)
     * @return an arraylist of events
     */
    @PropertyName("events")
    public ArrayList<String> getEvents() {
        return events;
    }

    /**
     * @author Sehej Brar
     * Sets the events for a user (on fragment_waitlist)
     * @param events list of events
     */
    @PropertyName("events")
    public void setEvents(ArrayList<String> events) {
        this.events = events;
    }

    /**
     * @author Sehej Brar
     * Gets notifications key
     * @return FCM for the user which allows for notifications to be sent to their device
     */
    @PropertyName("notifications")
    public ArrayList<String> getNotifications() {
        return notifications;
    }

    /**
     * @author Sehej Brar
     * Sets notifications
     * @param notifications the user notification key for firebase (title, body)
     */
    @PropertyName("notifications")
    public void setNotifications(ArrayList<String> notifications) {
        updateUser("notifications", notifications);
        this.notifications = notifications;
    }

    /**
     * @author Sehej Brar
     * Adds notification to the ArrayList
     * @param title title
     * @param body body
     * @param flag flag for invite for info
     * @param eventId event id
     */
    public void addNotifications(String title, String body, String flag, String eventId) {
        this.notifications.add(title);
        this.notifications.add(body);
        this.notifications.add(flag);
        this.notifications.add(eventId);
        updateUser("notifications", this.notifications);
    }

    /**
     * @author Sehej Brar
     * Gets device id
     * @return device id
     */
    @PropertyName("dID")
    public String getDeviceID() {
        return deviceID;
    }

    /**
     * @author Sehej Brar
     * Sets device ID
     * @param deviceID the device ID
     */
    @PropertyName("dID")
    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
        updateUser("DID", new ArrayList<String>(Collections.singletonList(deviceID)));

    }

    /**
     * @author Sehej Brar
     * Gets first name
     * @return first name
     */
    @PropertyName("first name")
    public String getFirstName() {
        return firstName;
    }

    /**
     * @author Sehej Brar
     * Sets first name
     * @param firstName first name
     */
    @PropertyName("first name")
    public void setFirstName(String firstName) {
        updateUser("first name", new ArrayList<String>(Collections.singletonList(firstName)));
        this.firstName = firstName;
    }

    /**
     * @author Sehej Brar
     * Gets last name
     * @return last name
     */
    @PropertyName("last name")
    public String getLastName() {
        return lastName;
    }

    /**
     * @author Sehej Brar
     * Sets last name
     * @param lastName last name
     */
    @PropertyName("last name")
    public void setLastName(String lastName) {
        updateUser("last name", new ArrayList<String>(Collections.singletonList(lastName)));
        this.lastName = lastName;
    }

    /**
     * @author Sehej Brar
     * Gets email
     * @return email
     */
    @PropertyName("email")
    public String getEmail() {
        return email;
    }

    /**
     * @author Sehej Brar
     * Sets email
     * @param email email address
     */
    @PropertyName("email")
    public void setEmail(String email) {
        updateUser("email", new ArrayList<String>(Collections.singletonList(email)));
        this.email = email;
    }

    /**
     * @author Sehej Brar
     * Gets phone number
     * @return phone number (may be null)
     */
    @PropertyName("phone number")
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * @author Sehej Brar
     * Sets phone number
     * @param phoneNumber phone number
     */
    @PropertyName("phone number")
    public void setPhoneNumber(String phoneNumber) {
        updateUser("phone number", new ArrayList<String>(Collections.singletonList(phoneNumber)));
        this.phoneNumber = phoneNumber;
    }

    /**
     * @author Sehej Brar
     * If edit mode is on then the user can be updated, this prevents findUser() from attempting to update
     * the database while constructing the found user.
     * @param field attribute to change
     * @param newItem new attribute
     */
    private void updateUser(String field, ArrayList<String> newItem) {

        if (edit) {
            new UserFirestore().editUser(this, field, newItem);
        }
    }

    /**
     * @author Sehej Brar
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
     * @author Sehej Brar
     * Redo the hash for the attribute we compared above
     * @return hash for the email
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.getDeviceID());
    }

    /**
     * @author Sehej Brar
     * Edits can only be made when the edit mode is on, it is false by default
     * @param changeEdit a boolean used to denote whether edit is on or off
     */
    public void editMode(boolean changeEdit) {
        this.edit = changeEdit;
    }

    /**
     * Removes event from user's event list
     * @author Sehej Brar
     * @param eventID event id for event
     * @param eventList list of events for user
     * @return updated list of events
     */
    public ArrayList<String> removeEventFromEventList(String eventID, ArrayList<String> eventList){
        eventList.remove(eventID);
        return eventList;
    }
}
