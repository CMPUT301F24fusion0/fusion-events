package com.example.fusion0;

import java.util.ArrayList;
import java.util.HashMap;

public class OrganizerActivity {
    ArrayList<String> events;
    ArrayList<String>  facilities;
    public String deviceId;
    Firebase firebase;


    public OrganizerActivity(ArrayList<String> events, ArrayList<String> facilities, String deviceId){
        this.events = events;
        this.deviceId = deviceId;
        this.facilities = facilities;
        this.firebase = new Firebase();

    }

    public HashMap<String,Object> organizer() {
        HashMap<String, Object> organizer = new HashMap<>();
        organizer().put("events", this.events);
        organizer().put("facilities", this.facilities);
        organizer().put("deviceId", this.deviceId);
        return organizer;
    }

    public ArrayList<String> getEvents() {
        return events;
    }

    public void setEvents(ArrayList<String> events) {
        this.events = events;
        updateOrganizer(organizer());
    }

    public ArrayList<String> getFacilities() {
        return facilities;
    }

    public void setFacilities(ArrayList<String> facilities) {
        this.facilities = facilities;
        updateOrganizer(organizer());

    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        updateOrganizer(organizer());
    }


    public void updateOrganizer(HashMap<String,Object> organizer){
        firebase.editOrganizer(this, organizer);
    }

}
