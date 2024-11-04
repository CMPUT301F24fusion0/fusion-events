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
    private String eventPoster;
    QRCode qrCode;
    EventFirebase firebase;
    private Long acceptedCount;

    public EventInfo() throws WriterException {
        this.eventID = UUID.randomUUID().toString();
        this.organizer = "";
        this.eventName = "";
        this.address = "";
        this.facilityName = "";
        this.capacity = "0";
        this.description = "";
        this.startDate = new Date();
        this.endDate = new Date();
        this.startTime = "00:00";
        this.endTime = "00:00";
        this.qrCode = new QRCode(eventID);
        this.entrants = new ArrayList<>();
        this.chosenEntrants = new ArrayList<>();
        this.cancelledEntrants = new ArrayList<>();
        this.firebase = new EventFirebase();
        this.acceptedCount = 0L;
        this.eventPoster = null;
    }

    public EventInfo(String organizer, String eventName, String address, String facilityName, String capacity, String description, Date startDate, Date endDate, String startTime, String endTime, String eventPoster) throws WriterException {
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


    public String getDescription() {
        return description;
    }


    public void setDescription(String description) {
        this.description = description;
    }


    public String getCapacity() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
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
    }


    public Date getEndDate() {
        return endDate;
    }


    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }


    public String getStartTime() {
        return startTime;
    }


    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }


    public String getEndTime() {
        return endTime;
    }


    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }


    public ArrayList<String> getEntrants() {
        return entrants;
    }


    public void setEntrants(ArrayList<String> entrants) {
        this.entrants = entrants;
    }


    public ArrayList<String> getChosenEntrants() {
        return chosenEntrants;
    }


    public void setChosenEntrants(ArrayList<String> chosenEntrants) {
        this.chosenEntrants = chosenEntrants;
    }


    public ArrayList<String> getCancelledEntrants() {
        return cancelledEntrants;
    }


    public void setCancelledEntrants(ArrayList<String> cancelledEntrants) {
        this.cancelledEntrants = cancelledEntrants;
    }


    public QRCode getQrCode() {
        return qrCode;
    }

    public void setQrCode(QRCode qrCode) {
        this.qrCode = qrCode;
    }


    public String getEventPoster() {
        return eventPoster;
    }

    public void setEventPoster(String eventPoster) {
        this.eventPoster = eventPoster;
    }

    public Uri getEventPosterUri() {
        return Uri.parse(eventPoster);
    }


}