package com.example.fusion0;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class OrganizerInfo {
    ArrayList<EventInfo> events;
    ArrayList<FacilitiesInfo>  facilities;
    public String deviceId;
    EventFirebase firebase;
    private ArrayList<String> eventsNames;
    private ArrayList<String> facilitiesNames;


    public OrganizerInfo(){
        this.events = new ArrayList<>();
        this.deviceId = "deviceID";
        this.facilities = new ArrayList<>();
        this.firebase = new EventFirebase();
    }

    /**
     * Default constructor for the OrganizerInfo class
     * @param deviceId the device ID of the organizer
     */
    public OrganizerInfo(String deviceId){
        this.events = new ArrayList<>();
        this.deviceId = deviceId;
        this.facilities = new ArrayList<>();
        this.firebase = new EventFirebase();
    }



    /**
     * Creates a hashmap of the information needed for Firebase for the organizer
     * @return the hashmap for the organizer
     */
    public HashMap<String,Object> organizer() {
        HashMap<String, Object> organizer = new HashMap<>();
        organizer.put("deviceId", this.deviceId);
        organizer.put("events", this.events);
        organizer.put("facilities", this.facilities);
        return organizer;
    }

    /**
     * Gets Events
     * @return events of the organizer
     */
    public ArrayList<EventInfo> getEvents() {
        return events;
    }

    /**
     * Sets events
     * @param events a list of events
     */
    public void setEvents(ArrayList<EventInfo> events) {
        this.events = events;
    }

    /**
     * Gets facilities
     * @return a list of strings that are facilities
     */
    public ArrayList<FacilitiesInfo> getFacilities() {
        return facilities;
    }


    public void setFacilities(ArrayList<FacilitiesInfo> facilities) {
        this.facilities = facilities;
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
    }

    public String getFacilityIdByName(String facilityName) {
        for (FacilitiesInfo facility : facilities) {
            if (facility.getFacilityName().equals(facilityName)) {
                return facility.getFacilityID(); // Return the ID if names match
            }
        }
        return null; // Return null if no match is found
    }

    public ArrayList<String> getEventsNames() {
        ArrayList<String> eventsName = new ArrayList<>();
        if (events != null) {
            for (EventInfo event : events) {
                eventsName.add(event.getEventName());
            }
        }
        return eventsName;
    }

    public ArrayList<String> getFacilitiesNames() {
        ArrayList<String> facilitiesName = new ArrayList<>();
        if (facilities != null) {
            for (FacilitiesInfo facility : facilities) {
                facilitiesName.add(facility.getFacilityName());
            }
        }
        return facilitiesName;
    }



}
