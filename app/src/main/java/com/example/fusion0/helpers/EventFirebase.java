package com.example.fusion0.helpers;

import com.example.fusion0.models.EventInfo;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.zxing.WriterException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.example.fusion0.models.OrganizerInfo;
import com.example.fusion0.models.FacilitiesInfo;

/**
 * @author Simon Haile
 * This class provides methods for interacting with Firebase Firestore to manage event-related data.
 * It includes methods to add, update, delete, and retrieve data for organizers, facilities, and events.
 */
public class EventFirebase {
    private final FirebaseFirestore firestore;
    private static final CollectionReference organizersRef;
    private static final CollectionReference facilitiesRef;
    private static final CollectionReference eventsRef;
    private static final CollectionReference adminsRef;

    static {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        organizersRef = db.collection("organizers");
        facilitiesRef = db.collection("facilities");
        eventsRef = db.collection("events");
        adminsRef = db.collection("admins");
    }

    public EventFirebase(FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    public EventFirebase() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    /**
     * @author Simon Haile
     * Callback interface for handling results related to organizer data retrieval.
     */
    public interface OrganizerCallback {
        void onSuccess(OrganizerInfo organizerInfo);
        void onFailure(String error);
    }

    /**
     * @author Simon Haile
     * Callback interface for handling results related to facility data retrieval.
     */
    public interface FacilityCallback {
        void onSuccess(FacilitiesInfo facilityInfo);
        void onFailure(String error);
    }

    /**
     * @author Simon Haile
     * Callback interface for handling results related to event data retrieval.
     */
    public interface EventCallback {
        void onSuccess(EventInfo eventInfo) throws WriterException;
        void onFailure(String error);
    }

    /**
     * @author Simon Haile
     * Adds a new organizer to Firebase Firestore.
     *
     * @param organizerInfo The organizer's information to be added
     */
    public void addOrganizer(OrganizerInfo organizerInfo) {
        HashMap<String, Object> organizer = organizerInfo.organizer();
        String deviceId = organizerInfo.getDeviceId();
        organizersRef.document(deviceId).set(organizer)
                .addOnSuccessListener(documentReference -> System.out.println("Organizer added successfully."))
                .addOnFailureListener(error -> System.out.println("Failure: " + error.getMessage()));
    }

    /**
     * @author Simon Haile
     * Edits an existing organizer's data in Firebase Firestore.
     *
     * @param organizer The organizer with updated data
     */
    public void editOrganizer(OrganizerInfo organizer) {
        String deviceId = organizer.getDeviceId();
        organizersRef.document(deviceId).set(organizer, SetOptions.merge())
                .addOnSuccessListener(documentReference -> System.out.println("Organizer data updated successfully."))
                .addOnFailureListener(error -> System.err.println("Error updating organizer data: " + error.getMessage()));
    }

    /**
     * @author Simon Haile
     * Retrieves an organizer's information based on their device ID.
     *
     * @param deviceId The device ID of the organizer to be retrieved
     * @param callback The callback to handle the result of the retrieval
     */
    public void findOrganizer(String deviceId, OrganizerCallback callback) {
        organizersRef.document(deviceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        OrganizerInfo organizer = documentSnapshot.toObject(OrganizerInfo.class);
                        callback.onSuccess(organizer);
                    } else {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(error -> {
                    System.out.println("Failure: " + error.getMessage());
                    callback.onFailure(error.getMessage());
                });
    }

    /**
     * @author Simon Haile
     * Deletes an organizer from Firebase Firestore based on their device ID.
     *
     * @param deviceId The device ID of the organizer to be deleted
     */
    public void deleteOrganizer(String deviceId) {
        organizersRef.document(deviceId).delete().addOnSuccessListener(documentReference -> System.out.println("Organizer deleted successfully.")).addOnFailureListener(error -> System.err.println("Error deleting organizer: " + error.getMessage()));
    }

    /**
     * @author Simon Haile
     * Adds a new facility to Firebase Firestore.
     *
     * @param facilitiesInfo The facility's information to be added
     */
    public void addFacility(FacilitiesInfo facilitiesInfo) {
        HashMap<String, Object> facility = facilitiesInfo.facility();
        String facilityID = facilitiesInfo.getFacilityID();
        facilitiesRef.document(facilityID).set(facility)
                .addOnSuccessListener(documentReference -> System.out.println("Facility added successfully."))
                .addOnFailureListener(error -> System.out.println("Failure: " + error.getMessage()));
    }

    /**
     * @author Simon Haile
     * Edits an existing facility's data in Firebase Firestore.
     *
     * @param facility The facility with updated data
     */
    public void editFacility(FacilitiesInfo facility) {
        String facilityID = facility.getFacilityID();
        facilitiesRef.document(facilityID).set(facility, SetOptions.merge())
                .addOnSuccessListener(documentReference -> System.out.println("Facility data updated successfully."))
                .addOnFailureListener(error -> System.err.println("Error updating facility data: " + error.getMessage()));
    }

    /**
     * @author Simon Haile
     * Retrieves facility information based on its ID.
     *
     * @param facilityID The ID of the facility to be retrieved
     * @param callback The callback to handle the result of the retrieval
     */
    public void findFacility(String facilityID, FacilityCallback callback) {
        facilitiesRef.document(facilityID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        FacilitiesInfo facility = documentSnapshot.toObject(FacilitiesInfo.class);
                        callback.onSuccess(facility);
                    } else {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(error -> {
                    System.out.println("Failure: " + error.getMessage());
                    callback.onFailure(error.getMessage());
                });
    }

    /**
     * @author Simon Haile
     * Deletes a facility from Firebase Firestore based on its ID.
     *
     * @param facilityID The ID of the facility to be deleted
     */
    public void deleteFacility(String facilityID) {
        facilitiesRef.document(facilityID).delete().addOnSuccessListener(documentReference -> System.out.println("Facility deleted successfully.")).addOnFailureListener(error -> System.err.println("Error deleting facility: " + error.getMessage()));
    }

    /**
     * @author Simon Haile
     * Adds a new event to Firebase Firestore.
     *
     * @param eventInfo The event's information to be added
     */
    public void addEvent(EventInfo eventInfo) {
        HashMap<String, Object> event = eventInfo.event();
        String eventID = eventInfo.getEventID();
        eventsRef.document(eventID).set(event).addOnSuccessListener(documentReference -> System.out.println("Event added successfully.")).addOnFailureListener(error -> System.err.println("Error adding event: " + error.getMessage()));
    }

    /**
     * @author Simon Haile
     * Retrieves an event's information based on its ID.
     *
     * @param eventID The ID of the event to be retrieved
     * @param callback The callback to handle the result of the retrieval
     */
    public void findEvent(String eventID, EventCallback callback) {
        eventsRef.document(eventID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        EventInfo event = documentSnapshot.toObject(EventInfo.class);
                        try {
                            callback.onSuccess(event);
                        } catch (WriterException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        try {
                            callback.onSuccess(null);
                        } catch (WriterException e) {
                            throw new RuntimeException(e);
                        }
                    }
                })
                .addOnFailureListener(error -> {
                    System.out.println("Failure: " + error.getMessage());
                    callback.onFailure(error.getMessage());
                });
    }

    /**
     * @author Simon Haile
     * Edits an existing event's data in Firebase Firestore.
     *
     * @param event The event with updated data
     */
    public void editEvent(EventInfo event) {
        String eventID = event.getEventID();
        eventsRef.document(eventID).set(event, SetOptions.merge())
                .addOnSuccessListener(documentReference -> System.out.println("Event data updated successfully."))
                .addOnFailureListener(error -> System.err.println("Error updating event data: " + error.getMessage()));
    }

    /**
     * @author Simon Haile
     * Deletes an event from Firebase Firestore based on its ID.
     *
     * @param eventID The ID of the event to be deleted
     */
    public void deleteEvent(String eventID) {
        eventsRef.document(eventID).delete().addOnSuccessListener(documentReference -> System.out.println("Event deleted successfully.")).addOnFailureListener(error -> System.err.println("Error deleting event: " + error.getMessage()));
    }

    /**
     * @author Malshaan Kodithuwakku
     * Determines if deviceID is in admin collection
     *
     * @param deviceID The deviceID to be checked
     *
     * @return True if deviceID is in admin collection, false otherwise
     */

    public static boolean isDeviceIDAdmin(String deviceID) {
        return adminsRef.document(deviceID).get().isSuccessful();
    }

    /**
     * @author Malshaan Kodithuwakku
     * Callback method of getAllFacilities()
     */

    public interface FacilityListCallBack {
        void onSuccess(List<FacilitiesInfo> facilities);
        void onFailure(String error);
    }

    /**
     * @author Malshaan Kodithuwakku
     * Retrieves all facilities from Firebase Firestore.
     * @param callback The callback to handle the result of the retrieval
     */
    public static void getAllFacilities(FacilityListCallBack callback) {
        facilitiesRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<FacilitiesInfo> facilities = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        if (document.exists()) {
                            FacilitiesInfo facility = document.toObject(FacilitiesInfo.class);
                            facilities.add(facility);
                            callback.onSuccess(facilities);
                        }
                    }
                })
                .addOnFailureListener(error -> {
                    System.err.println("Error fetching facilities: " + error.getMessage());
                    callback.onFailure(error.getMessage());
                });
    }

    /**
     * @author Derin Karas
     * Callback method of getAllEvents()
     */

    public interface EventListCallback {
        void onSuccess(List<EventInfo> events);
        void onFailure(String error);
    }

    /**
     * @author Derin Karas
     * Retrieves all events from Firebase Firestore.
     * @param callback The callback to handle the result of the retrieval
     */
    public static void getAllEvents(EventListCallback callback) {
        eventsRef.get()
                .addOnSuccessListener(querySnapshot -> {
                    List<EventInfo> events = new ArrayList<>();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        try {
                            EventInfo event = document.toObject(EventInfo.class);
                            if (event.getWaitinglist() == null || !(event.getWaitinglist() instanceof List)) {
                                event.setWaitinglist(new ArrayList<>()); // Default to an empty list
                            }
                            events.add(event);
                        } catch (Exception e) {
                            // Handle deserialization errors (optional logging if necessary)
                        }
                    }
                    callback.onSuccess(events);
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                });
    }
}
