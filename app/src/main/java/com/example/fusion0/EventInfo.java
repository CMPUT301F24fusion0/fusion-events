package com.example.fusion0;

import android.media.Image;
import android.net.Uri;

import com.google.zxing.WriterException;

import java.net.URI;
import java.util.HashMap;


import java.util.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.UUID;


public class EventInfo {
    public String eventID;
    private String organizer;
    private String eventName;
    private String description;
    private String address;
    private String facilityName;
    private String capacity;
    private Date startDate;
    private Date endDate;
    private String startTime;
    private String endTime;
    ArrayList<String> entrants;
    ArrayList<String> chosenEntrants;
    ArrayList<String> cancelledEntrants;
    private Uri eventPoster;
    QRCode qrCode;
    EventFirebase firebase;
    private Long acceptedCount;


    public EventInfo(String organizer, String eventName, String address, String facilityName, String capacity, String description, Date startDate, Date endDate, String startTime, String endTime, Uri eventPoster) throws WriterException {
        this.eventID = UUID.randomUUID().toString();
        this.organizer = organizer;
        this.eventName = eventName;
        this.address = address;
        this.facilityName = facilityName;
        this.capacity = capacity;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.qrCode = new QRCode(eventID);
        this.entrants = new ArrayList<>();
        this.chosenEntrants = new ArrayList<>();
        this.cancelledEntrants = new ArrayList<>();
        this.firebase = new EventFirebase();
        this.acceptedCount = 0L;
        this.eventPoster = eventPoster;
    }


    public HashMap<String,Object> event() {
        HashMap<String, Object> event = new HashMap<>();
        event.put("eventID", this.eventID);
        event.put("organizer", this.organizer);
        event.put("eventName", this.eventName);
        event.put("address", this.address);
        event.put("facilityName", this.facilityName);
        event.put("capacity", this.capacity);
        event.put("startDate", this.startDate);
        event.put("endDate", this.endDate);
        event.put("startTime", this.startTime);
        event.put("endTime", this.endTime);
        event.put("entrants", this.entrants);
        event.put("chosenEntrants", this.chosenEntrants);
        event.put("cancelledEntrants", this.cancelledEntrants);
        event.put("qrCode",this.qrCode);
        event.put("description", this.description);
        event.put("eventPoster", this.eventPoster);
        return event;
    }


    public String getEventID(){
        return eventID;
    }

    public String getOrganizer(){
        return organizer;
    }


    public void setOrganizer(String organizer) {
        this.organizer = organizer;
        updateEvent(event());
    }


    public String getEventName() {
        return eventName;
    }


    public void setEventName(String eventName) {
        this.eventName = eventName;
        updateEvent(event());
    }


    public String getAddress() {
        return address;
    }


    public void setAddress(String address) {
        this.address = address;
        updateEvent(event());
    }


    public String getFacilityName() {
        return facilityName;
    }


    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
        updateEvent(event());
    }


    public String getDescription() {
        return description;
    }


    public void setDescription(String description) {
        this.description = description;
        updateEvent(event());
    }


    public String getCapacity() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
        updateEvent(event());
    }

    public Long getAcceptedCount() {
        return acceptedCount;
    }

    public void setAcceptedCount(Long acceptedCount) {
        this.acceptedCount = acceptedCount;
    }



    public Date getStartDate() {
        return startDate;
    }


    public void setStartDate(Date startDate) {
        this.startDate = startDate;
        updateEvent(event());
    }


    public Date getEndDate() {
        return endDate;
    }


    public void setEndDate(Date endDate) {
        this.endDate = endDate;
        updateEvent(event());
    }


    public String getStartTime() {
        return startTime;
    }


    public void setStartTime(String startTime) {
        this.startTime = startTime;
        updateEvent(event());
    }


    public String getEndTime() {
        return endTime;
    }


    public void setEndTime(String endTime) {
        this.endTime = endTime;
        updateEvent(event());
    }


    public ArrayList<String> getEntrants() {
        return entrants;
    }


    public void setEntrants(ArrayList<String> entrants) {
        this.entrants = entrants;
        updateEvent(event());
    }


    public ArrayList<String> getChosenEntrants() {
        return chosenEntrants;
    }


    public void setChosenEntrants(ArrayList<String> chosenEntrants) {
        this.chosenEntrants = chosenEntrants;
        updateEvent(event());
    }


    public ArrayList<String> getCancelledEntrants() {
        return cancelledEntrants;
    }


    public void setCancelledEntrants(ArrayList<String> cancelledEntrants) {
        this.cancelledEntrants = cancelledEntrants;
        updateEvent(event());
    }


    public QRCode getQrCode() {
        return qrCode;
    }

    public void setQrCode(QRCode qrCode) {
        this.qrCode = qrCode;
        updateEvent(event());
    }


    public Uri getEventPoster() {
        return eventPoster;
    }

    public void setEventPoster(Uri eventPoster) {
        this.eventPoster = eventPoster;
        updateEvent(event());
    }

    public void updateEvent(HashMap<String,Object> event){
        String deviceId = (String) event.get("deviceId");
        if (deviceId == null || deviceId.isEmpty()) {
            System.err.println("Error: Device ID is null or empty in updateEvent.");
            return;
        }
        firebase.editEvent(this, event);
    }

}