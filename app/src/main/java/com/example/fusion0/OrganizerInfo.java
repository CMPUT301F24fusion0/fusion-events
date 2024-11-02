package com.example.fusion0;


import java.util.ArrayList;
import java.util.HashMap;


public class OrganizerInfo {
    ArrayList<String> events;
    ArrayList<String>  facilities;
    public String deviceId;
    EventFirebase firebase;

    /**
     * Default constructor for the OrganizerInfo class
     * @param deviceId the device ID of the organizer
     */
    public OrganizerInfo(String deviceId){
        this.events = new ArrayList<>();
        this.deviceId = deviceId;
        this.facilities = new ArrayList<>();
        facilities.add("Add Facility");
        this.firebase = new EventFirebase();
    }

    public OrganizerInfo(){
        this.firebase = new EventFirebase();

    }


    /**
     * Creates a hashmap of the information needed for Firebase for the organizer
     * @return the hashmap for the organizer
     */
    public HashMap<String,Object> organizer() {
        HashMap<String, Object> organizer = new HashMap<>();
        organizer.put("events", this.events);
        organizer.put("facilities", this.facilities);
        organizer.put("deviceId", this.deviceId);
        return organizer;
    }

    /**
     * Gets Events
     * @return events of the organizer
     */
    public ArrayList<String> getEvents() {
        return events;
    }

    /**
     * Sets events
     * @param events a list of events
     */
    public void setEvents(ArrayList<String> events) {
        this.events = events;
        updateOrganizer(organizer());
    }

    /**
     * Gets facilities
     * @return a list of strings that are facilities
     */
    public ArrayList<String> getFacilities() {
        return facilities;
    }


    public void setFacilities(ArrayList<String> facilities) {
        this.facilities = facilities;
        updateOrganizer(organizer());
    }

    /**
     * Gets device ID
     * @return a string denoted as device ID
     */
    public String getDeviceId() {
        return deviceId;
    }


    /**
     * Sets device id
     * @param deviceId the new device ID
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        updateOrganizer(organizer());
    }


    /**
     * Calls upon firebase to edit the organizer as need be
     */

    public void updateOrganizer(HashMap<String,Object> organizer){
        String deviceId = (String) organizer.get("deviceId");
        if (deviceId == null || deviceId.isEmpty()) {
            System.err.println("Error: Device ID is null or empty in updateOrganizer.");
            return;
        }
        firebase.editOrganizer(this);
    }


}
