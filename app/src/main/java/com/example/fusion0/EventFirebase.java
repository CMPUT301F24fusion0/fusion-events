package com.example.fusion0;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.zxing.WriterException;
import java.util.HashMap;

/**
 * @author Simon Haile
 * This class provides methods for interacting with Firebase Firestore to manage event-related data.
 * It includes methods to add, update, delete, and retrieve data for organizers, facilities, and events.
 *
 * Created by Simon Haile.
 */
public class EventFirebase {

    private static final CollectionReference organizersRef;
    private static final CollectionReference facilitiesRef;
    private static final CollectionReference eventsRef;

    // Static initialization of Firebase Firestore references
    static {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        organizersRef = db.collection("organizers");
        facilitiesRef = db.collection("facilities");
        eventsRef = db.collection("events");
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
    public static void addOrganizer(OrganizerInfo organizerInfo) {
        HashMap<String, Object> organizer = organizerInfo.organizer();
        String deviceId = organizerInfo.getDeviceId();
        organizersRef.document(deviceId).set(organizer)
                .addOnSuccessListener(documentReference -> {
                    System.out.println("Organizer added successfully.");
                })
                .addOnFailureListener(error -> {
                    System.out.println("Failure: " + error.getMessage());
                });
    }

    /**
     * @author Simon Haile
     * Edits an existing organizer's data in Firebase Firestore.
     *
     * @param organizer The organizer with updated data
     */
    public static void editOrganizer(OrganizerInfo organizer) {
        String deviceId = organizer.getDeviceId();
        organizersRef.document(deviceId).set(organizer, SetOptions.merge())
                .addOnSuccessListener(documentReference -> {
                    System.out.println("Organizer data updated successfully.");
                })
                .addOnFailureListener(error -> {
                    System.err.println("Error updating organizer data: " + error.getMessage());
                });
    }

    /**
     * @author Simon Haile
     * Retrieves an organizer's information based on their device ID.
     *
     * @param deviceId The device ID of the organizer to be retrieved
     * @param callback The callback to handle the result of the retrieval
     */
    public static void findOrganizer(String deviceId, OrganizerCallback callback) {
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
        organizersRef.document(deviceId).delete().addOnSuccessListener(documentReference -> {
            System.out.println("Organizer deleted successfully.");
        }).addOnFailureListener(error -> {
            System.err.println("Error deleting organizer: " + error.getMessage());
        });
    }

    /**
     * @author Simon Haile
     * Adds a new facility to Firebase Firestore.
     *
     * @param facilitiesInfo The facility's information to be added
     */
    public static void addFacility(FacilitiesInfo facilitiesInfo) {
        HashMap<String, Object> facility = facilitiesInfo.facility();
        String facilityID = facilitiesInfo.getFacilityID();
        facilitiesRef.document(facilityID).set(facility)
                .addOnSuccessListener(documentReference -> {
                    System.out.println("Facility added successfully.");
                })
                .addOnFailureListener(error -> {
                    System.out.println("Failure: " + error.getMessage());
                });
    }

    /**
     * @author Simon Haile
     * Edits an existing facility's data in Firebase Firestore.
     *
     * @param facility The facility with updated data
     */
    public static void editFacility(FacilitiesInfo facility) {
        String facilityID = facility.getFacilityID();
        facilitiesRef.document(facilityID).set(facility, SetOptions.merge())
                .addOnSuccessListener(documentReference -> {
                    System.out.println("Facility data updated successfully.");
                })
                .addOnFailureListener(error -> {
                    System.err.println("Error updating facility data: " + error.getMessage());
                });
    }

    /**
     * @author Simon Haile
     * Retrieves facility information based on its ID.
     *
     * @param facilityID The ID of the facility to be retrieved
     * @param callback The callback to handle the result of the retrieval
     */
    public static void findFacility(String facilityID, FacilityCallback callback) {
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
    public static void deleteFacility(String facilityID) {
        facilitiesRef.document(facilityID).delete().addOnSuccessListener(documentReference -> {
            System.out.println("Facility deleted successfully.");
        }).addOnFailureListener(error -> {
            System.err.println("Error deleting facility: " + error.getMessage());
        });
    }

    /**
     * @author Simon Haile
     * Adds a new event to Firebase Firestore.
     *
     * @param eventInfo The event's information to be added
     */
    public static void addEvent(EventInfo eventInfo) {
        HashMap<String, Object> event = eventInfo.event();
        String eventID = eventInfo.getEventID();
        eventsRef.document(eventID).set(event).addOnSuccessListener(documentReference -> {
            System.out.println("Event added successfully.");
        }).addOnFailureListener(error -> {
            System.err.println("Error adding event: " + error.getMessage());
        });
    }

    /**
     * @author Simon Haile
     * Retrieves an event's information based on its ID.
     *
     * @param eventID The ID of the event to be retrieved
     * @param callback The callback to handle the result of the retrieval
     */
    public static void findEvent(String eventID, EventCallback callback) {
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
    public static void editEvent(EventInfo event) {
        String eventID = event.getEventID();
        eventsRef.document(eventID).set(event, SetOptions.merge())
                .addOnSuccessListener(documentReference -> {
                    System.out.println("Event data updated successfully.");
                })
                .addOnFailureListener(error -> {
                    System.err.println("Error updating event data: " + error.getMessage());
                });
    }

    /**
     * @author Simon Haile
     * Deletes an event from Firebase Firestore based on its ID.
     *
     * @param eventID The ID of the event to be deleted
     */
    public static void deleteEvent(String eventID) {
        eventsRef.document(eventID).delete().addOnSuccessListener(documentReference -> {
            System.out.println("Event deleted successfully.");
        }).addOnFailureListener(error -> {
            System.err.println("Error deleting event: " + error.getMessage());
        });
    }
}
