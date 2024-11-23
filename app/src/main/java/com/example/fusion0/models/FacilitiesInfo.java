package com.example.fusion0.models;

import com.example.fusion0.helpers.EventFirebase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * @author Simon Haile
 * This class contains the information for facilities.
 */
public class FacilitiesInfo {
    public String facilityID;
    public String address;
    public String facilityName;
    public String owner;
    ArrayList<String> events;
    EventFirebase firebase;
    private Double latitude;
    private Double longitude;
    private String facilityImage;

    /**
     * Default constructor. Initializes the firebase instance and prepares
     * an empty list of events. The facilityID is left unset.
     */
    public FacilitiesInfo() {
        this.firebase = new EventFirebase();
    }

    /**
     * Constructs a new FacilitiesInfo object with specified address, facility name,
     * owner, longitude, and latitude. A unique facility ID is generated automatically.
     *
     * @param address The address of the facility.
     * @param facilityName The name of the facility.
     * @param owner The owner of the facility.
     * @param longitude The longitude coordinate of the facility's location.
     * @param latitude The latitude coordinate of the facility's location.
     */
    public FacilitiesInfo(String address, String facilityName, String owner, Double longitude, Double latitude, String facilityImage) {
        this.address = address;
        this.facilityName = facilityName;
        this.owner = owner;
        this.events = new ArrayList<>();
        this.facilityID = UUID.randomUUID().toString();
        this.firebase = new EventFirebase();
        this.latitude = latitude;
        this.longitude = longitude;
        this.facilityImage = facilityImage;
    }

    /**
     * Returns a map representation of the facility's information. The map contains
     * the facility's address, name, owner, and events.
     *
     * @return A hashmap containing the facility's data.
     */
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

    public String getFacilityImage(){
        return facilityImage;
    }
    public void setFacilityImage(String facilityImage){
        this.facilityImage = facilityImage;
    }

}


