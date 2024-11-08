package com.example.fusion0;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class FacilitiesInfo {
    public String facilityID;
    public String address;
    public String facilityName;
    public String owner;
    ArrayList<String> events;
    EventFirebase firebase;
    private Double latitude;
    private Double longitude;

    public FacilitiesInfo() {
        this.firebase = new EventFirebase();
    }

    public FacilitiesInfo(String address, String facilityName, String owner, Double longitude, Double latitude) {
        this.address = address;
        this.facilityName = facilityName;
        this.owner = owner;
        this.events = new ArrayList<>();
        this.facilityID = UUID.randomUUID().toString();
        this.firebase = new EventFirebase();
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public HashMap<String, Object> facility() {
        HashMap<String, Object> facility = new HashMap<>();

        facility.put("address", this.address);
        facility.put("facilityName", this.facilityName);
        facility.put("owner", this.owner);
        facility.put("events", this.events);

        return facility;
    }

    public String getFacilityID() {
        return facilityID;
    }

    public String getFacilityName() {
        return facilityName;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public ArrayList<String> getEvents() {
        return events;
    }

    public void setEvents(ArrayList<String> events) {
        this.events = events;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

}


