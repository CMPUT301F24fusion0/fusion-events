package com.example.fusion0.helpers;

import com.example.fusion0.models.FacilitiesInfo;
import com.example.fusion0.models.OrganizerInfo;

import java.util.Date;

public class AddEventHelper {

    public OrganizerInfo organizer;
    public FacilitiesInfo facility;
    public FacilitiesInfo newFacility;


    // Device-specific
    public String deviceID;

    // Event details
    public String eventName;
    private String eventPoster;     // URL of the poster
    public String description;

    // Capacity details
    public String waitlistCapacity;
    public String lotteryCapacity;

    // Dates and times
    public Date registrationDate;
    public Date startDate;
    public Date endDate;

    public String endTime;
    public String startTime;

    // Location-related
    public String address;
    public String facilityID;   // Firebase ID of the facility
    public String facilityName;
    public Boolean geolocation;
    private Double longitude;
    private Double latitude;

    // Optional: Radius for geolocation-based signup (in meters)
    public Integer geolocationRadius;

    // Default Constructor
    public AddEventHelper() {}

    // Parameterized Constructor
    public AddEventHelper(OrganizerInfo organizer, FacilitiesInfo facility, FacilitiesInfo newFacility, String deviceID, String eventName, String eventPoster, String description,
                          String waitlistCapacity, String lotteryCapacity, Date registrationDate,
                          Date startDate, String startTime, Date endDate, String endTime,
                          String address, String facilityID, String facilityName,
                          Boolean geolocation, Double longitude, Double latitude, Integer geolocationRadius) {
        this.organizer = organizer;
        this.facility = facility;
        this.newFacility = newFacility;
        this.deviceID = deviceID;
        this.eventName = eventName;
        this.eventPoster = eventPoster;
        this.description = description;
        this.waitlistCapacity = waitlistCapacity;
        this.lotteryCapacity = lotteryCapacity;
        this.registrationDate = registrationDate;
        this.startDate = startDate;
        this.startTime = startTime;
        this.endDate = endDate;
        this.endTime = endTime;
        this.address = address;
        this.facilityID = facilityID;
        this.facilityName = facilityName;
        this.geolocation = geolocation;
        this.longitude = longitude;
        this.latitude = latitude;
        this.geolocationRadius = geolocationRadius;
    }

    // Getters and Setters for each field


    public OrganizerInfo getOrganizer() {
        return organizer;
    }

    public void setOrganizer(OrganizerInfo organizer) {
        this.organizer = organizer;
    }

    public FacilitiesInfo getNewFacility() {
        return newFacility;
    }

    public void setNewFacility(FacilitiesInfo newFacility) {
        this.newFacility = newFacility;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventPoster() {
        return eventPoster;
    }

    public void setEventPoster(String eventPoster) {
        this.eventPoster = eventPoster;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWaitlistCapacity() {
        return waitlistCapacity;
    }

    public void setWaitlistCapacity(String waitlistCapacity) {
        this.waitlistCapacity = waitlistCapacity;
    }

    public String getLotteryCapacity() {
        return lotteryCapacity;
    }

    public void setLotteryCapacity(String lotteryCapacity) {
        this.lotteryCapacity = lotteryCapacity;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public FacilitiesInfo getFacility() {
        return facility;
    }

    public void setFacility(FacilitiesInfo facility) {
        this.facility = facility;
    }

    public String getFacilityID() {
        return facilityID;
    }

    public void setFacilityID(String facilityID) {
        this.facilityID = facilityID;
    }

    public String getFacilityName() {
        return facilityName;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    public Boolean getGeolocation() {
        return geolocation;
    }

    public void setGeolocation(Boolean geolocation) {
        this.geolocation = geolocation;
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

    public Integer getGeolocationRadius() {
        return geolocationRadius;
    }

    public void setGeolocationRadius(Integer geolocationRadius) {
        this.geolocationRadius = geolocationRadius;
    }

    @Override
    public String toString() {
        return "AddEventHelper{" +
                "deviceID='" + deviceID + '\'' +
                ", eventName='" + eventName + '\'' +
                ", eventPoster='" + eventPoster + '\'' +
                ", description='" + description + '\'' +
                ", waitlistCapacity='" + waitlistCapacity + '\'' +
                ", lotteryCapacity='" + lotteryCapacity + '\'' +
                ", registrationDate='" + registrationDate + '\'' +
                ", startDate='" + startDate + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endDate='" + endDate + '\'' +
                ", endTime='" + endTime + '\'' +
                ", address='" + address + '\'' +
                ", facilityID='" + facilityID + '\'' +
                ", facilityName='" + facilityName + '\'' +
                ", geolocation=" + geolocation +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", geolocationRadius=" + geolocationRadius +
                '}';
    }
}
