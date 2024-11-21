package com.example.fusion0;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.HashMap;


import java.util.Date;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Simon Haile
 * This class contains the information for events.
 */
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
    ArrayList<Map<String, String>> waitinglist;
    ArrayList<String> chosenEntrants;
    ArrayList<String> cancelledEntrants;
    private String eventPoster;
    private String qrCode;
    EventFirebase firebase;
    private Long acceptedCount;
    private Boolean geolocation;
    private Double latitude;
    private Double longitude;
    private Integer radius;

    /**
     * Default constructor that initializes an empty event with default values.
     * A unique event ID is generated, and an associated QR code is created for the event.
     *
     * @throws WriterException If an error occurs while generating the QR code.
     */
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
        this.qrCode = (new QRCode(eventID)).getQrCode();
        this.waitinglist = new ArrayList<Map<String, String>>();
        this.chosenEntrants = new ArrayList<>();
        this.cancelledEntrants = new ArrayList<>();
        this.firebase = new EventFirebase();
        this.acceptedCount = 0L;
        this.eventPoster = null;
        this.geolocation = false;
        this.latitude = 0.0;
        this.longitude = 0.0;
        this.radius = 0;
    }

    /**
     * Constructor that initializes an event with the given details.
     * A unique event ID is generated, and an associated QR code is created for the event.
     *
     * @param organizer      The organizer of the event.
     * @param eventName      The name of the event.
     * @param address        The address where the event is held.
     * @param facilityName   The name of the facility hosting the event.
     * @param capacity       The maximum number of participants allowed for the event.
     * @param description    A description of the event.
     * @param startDate      The starting date of the event.
     * @param endDate        The ending date of the event.
     * @param startTime      The starting time of the event.
     * @param endTime        The ending time of the event.
     * @param eventPoster    The URL or path of the event poster image.
     * @param geolocation    Boolean indicating whether geolocation is enabled for the event.
     * @param latitude       The latitude of the event's location.
     * @param longitude      The longitude of the event's location.
     * @param radius         The radius within which the event is valid (if geolocation is enabled).
     * @throws WriterException If an error occurs while generating the QR code.
     */
    public EventInfo(String organizer, String eventName, String address, String facilityName, String capacity, String description, Date startDate, Date endDate, String startTime, String endTime, String eventPoster, Boolean geolocation, Double longitude, Double latitude, Integer radius) throws WriterException {
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
        this.qrCode =(new QRCode(eventID)).getQrCode();
        this.waitinglist = new ArrayList<>();
        this.chosenEntrants = new ArrayList<>();
        this.cancelledEntrants = new ArrayList<>();
        this.firebase = new EventFirebase();
        this.acceptedCount = 0L;
        this.eventPoster = eventPoster;
        this.geolocation = geolocation;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
    }

    /**
     * Converts the event object into a `HashMap` that can be used to save the event data to a database (e.g., Firebase).
     *
     * @return A `HashMap` representation of the event with key-value pairs for each field.
     */
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
        event.put("waitinglist", this.waitinglist);
        event.put("chosenEntrants", this.chosenEntrants);
        event.put("cancelledEntrants", this.cancelledEntrants);
        event.put("qrCode",this.qrCode);
        event.put("description", this.description);
        event.put("eventPoster", this.eventPoster);
        event.put("geolocation", this.geolocation);
        event.put("latitude", this.latitude);
        event.put("longitude", this.longitude);
        event.put("radius", this.radius);
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


    public ArrayList<Map<String, String>> getWaitinglist() {
        return waitinglist;
    }


    public void setWaitinglist(ArrayList<Map<String, String>> waitinglist) {
        this.waitinglist = waitinglist;
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


    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }


    public String getEventPoster() {
        return eventPoster;
    }

    public void setEventPoster(String eventPoster) {
        this.eventPoster = eventPoster;
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

    public Integer getRadius() {
        return radius;
    }

    public void setRadius(Integer radius) {
        this.radius = radius;
    }


    /**
     * @author Simon Haile
     * Generates a QR code image from the given QR code string.
     * This method uses ZXing to create a Bitmap from the hashed QR code string.
     *
     * @param width  The width of the QR code image.
     * @param height The height of the QR code image.
     * @param qrCode The hashed string to be encoded into a QR code image.
     * @return A Bitmap representation of the QR code.
     * @throws WriterException If an error occurs during QR code image generation.
     */
    public Bitmap generateQRCodeImage(int width, int height, String qrCode) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(qrCode, BarcodeFormat.QR_CODE, width, height);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? android.graphics.Color.BLACK : android.graphics.Color.WHITE);
            }
        }
        return bitmap;
    }

    /**
     * Removes a user from the waiting list
     * @author Sehej Brar
     * @param deviceID device id
     * @param waitingList an arraylist of maps containing waitlist information
     * @return an arraylist of maps containing waitlist information
     */
    public ArrayList<Map<String, String>> removeUserFromWaitingList(String deviceID, ArrayList<Map<String, String>> waitingList) {
        Iterator<Map<String, String>> iterator = waitingList.iterator();
        while (iterator.hasNext()) {
            Map<String, String> next = iterator.next();

            if (next.containsKey("did") && Objects.equals(next.get("did"), deviceID)) {
                iterator.remove();
            }
        }
        return waitingList;
    }

    /*
    This was already made (see waitlist)
    public removeUserFromCancelledEntrants(String deviceID){

    }

    if this is necessary, just copy the wishlist one
    public removeUserFromChosenEntrants(String deviceID){

    }

    this was already made (see waitlist)
    public ArrayList<String> getArrayOfEntrants(ArrayList<String> list){
        ArrayList<String> userNames = new ArrayList<>();
    }

     */

}