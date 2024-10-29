package com.example.fusion0;

import java.util.HashMap;


//import java.awt.Image;
//import android.media.Image;

import java.util.Date;
import java.sql.Time;
import java.util.ArrayList;


public class EventActivity {
    private String organizer;
    private String eventName;
    private String address;
    private String facilityName;
    private Integer capacity;
    //private Image eventPoster;
    private float distance;
    private Date startDate;
    private Date endDate;
    private Time startTime;
    private Time endTime;
    ArrayList<String> entrants;

    public EventActivity(String organizer, String eventName, String address, String facilityName, Integer capacity, float distance, Date startDate, Date endDate, Time startTime, Time endTime, ArrayList<String> entrants) {
        this.organizer = organizer;
        this.eventName = eventName;
        this.address = address;
        this.facilityName = facilityName;
        this.capacity = capacity;
        this.distance = distance;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.entrants = entrants;
    }

    public HashMap<String,Object> event() {
        HashMap<String, Object> event = new HashMap<>();

        event.put("organizer", this.organizer);
        event.put("eventName", this.eventName);
        event.put("address", this.address);
        event.put("facilityName", this.facilityName);
        event.put("capacity", this.capacity);
        event.put("distance", this.distance);
        event.put("startDate", this.startDate);
        event.put("endDate", this.endDate);
        event.put("startTime", this.startTime);
        event.put("endTime", this.endTime);
        event.put("entrants", this.entrants);

        return event;
    }

    public String getOrganizer(){
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    public String getEventName() {

        return eventName;
    }

    public void setEventName(String eventName) {

        this.eventName = eventName;
    }

    public String getAddress() {

        return address;
    }

    public void setAddress(String address) {

        this.address = address;
    }

    public String getFacilityName() {

        return facilityName;
    }

    public void setFacilityName(String facilityName) {

        this.facilityName = facilityName;
    }

    public Integer getCapacity() {

        return capacity;
    }

    public void setCapacity(Integer capacity) {

        this.capacity = capacity;
    }

    public float getDistance() {

        return distance;
    }

    public void setDistance(float distance) {

        this.distance = distance;
    }

    public Date getStartDate() {

        return startDate;
    }

    public void setStartDate(Date startDate) {

        this.startDate = startDate;
    }

    public Date getEndDate() {

        return endDate;
    }

    public void setEndDate(Date endDate) {

        this.endDate = endDate;
    }

    public Time getStartTime() {

        return startTime;
    }

    public void setStartTime(Time startTime) {

        this.startTime = startTime;
    }

    public Time getEndTime() {

        return endTime;
    }

    public void setEndTime(Time endTime) {

        this.endTime = endTime;
    }


    public ArrayList<String> getEntrants() {
        return entrants;
    }

    public void setEntrants(ArrayList<String> entrants) {
        this.entrants = entrants;
    }

}
