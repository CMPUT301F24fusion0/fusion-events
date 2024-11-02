package com.example.fusion0;


import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.zxing.WriterException;


import java.util.HashMap;


public class EventFirebase {


    private static final CollectionReference organizersRef;
    private static final CollectionReference facilitiesRef;
    private static final CollectionReference eventsRef;

    static {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        organizersRef = db.collection("organizers");
        facilitiesRef = db.collection("facilities");
        eventsRef = db.collection("events");
    }

    public interface OrganizerCallback {
        void onSuccess(OrganizerInfo organizerInfo);
        void onFailure(String error);
    }

    public interface FacilityCallback {
        void onSuccess(FacilitiesInfo facilityInfo);
        void onFailure(String error);
    }
    public interface EventCallback {
        void onSuccess(EventInfo eventInfo) throws WriterException;
        void onFailure(String error);
    }


    public static void addOrganizer(OrganizerInfo organizerInfo){
        HashMap<String, Object> organizer = organizerInfo.organizer();
        String deviceId = organizerInfo.getDeviceId();
        organizersRef.document(deviceId).set(organizer)
                .addOnSuccessListener(documentReference -> {
                    System.out.println("Success");
                })
                .addOnFailureListener(error -> {
                    System.out.println("Failure" + error.getMessage());
                });


    }

    public void editOrganizer(OrganizerInfo organizer) {
        String deviceId = organizer.getDeviceId();
        organizersRef.document(deviceId).set(organizer, SetOptions.merge())
                .addOnSuccessListener(documentReference -> {
                    System.out.println("Organizer data updated successfully.");
                })
                .addOnFailureListener(error -> {
                    System.err.println("Error updating organizer data: " + error.getMessage());
                });
    }

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
                    System.out.println("Failure" + error.getMessage());
                    callback.onFailure(error.getMessage());
                });
    }

    public void deleteOrganizer(String deviceId){
        organizersRef.document(deviceId).delete().addOnSuccessListener(documentReference -> {
            System.out.println("Success");
        }).addOnFailureListener(error -> {
            System.err.println("Failure " + error.getMessage());
        });
    }


    public static void addFacility(FacilitiesInfo facilitiesInfo){
        HashMap<String, Object> facility = facilitiesInfo.facility();
        String facilityID = facilitiesInfo.getFacilityID();
        facilitiesRef.document(facilityID).set(facility)
                .addOnSuccessListener(documentReference -> {
                    System.out.println("Success");
                })
                .addOnFailureListener(error -> {
                    System.out.println("Failure" + error.getMessage());
                });
    }


    public void editFacility(FacilitiesInfo facilitiesInfo, HashMap<String, Object> updatedData){
        String facilityID = facilitiesInfo.getFacilityID();
        facilitiesRef.document(facilityID).set(updatedData, SetOptions.merge())
                .addOnSuccessListener(documentReference -> {
                    System.out.println("Facility data updated successfully.");
                })
                .addOnFailureListener(error -> {
                    System.err.println("Error updating facility data: " + error.getMessage());
                });
    }

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
                    System.out.println("Failure" + error.getMessage());
                    callback.onFailure(error.getMessage());
                });
    }

    public void deleteFacility(String facilityID){
        facilitiesRef.document(facilityID).delete().addOnSuccessListener(documentReference -> {
            System.out.println("Success");
        }).addOnFailureListener(error -> {
            System.err.println("Failure " + error.getMessage());
        });


    }


    public static void addEvent(EventInfo eventInfo){
        HashMap<String, Object> event = eventInfo.event();
        String eventID = eventInfo.getEventID();
        eventsRef.document(eventID).set(event).addOnSuccessListener(documentReference -> {
            System.out.println("Success");
        }).addOnFailureListener(error -> {
            System.err.println("Failure " + error.getMessage());
        });
    }

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
                    System.out.println("Failure" + error.getMessage());
                    callback.onFailure(error.getMessage());
                });
    }



    public void editEvent(EventInfo eventInfo,HashMap<String, Object> updatedData){
        String eventID = eventInfo.getEventID();
        eventsRef.document(eventID).set(updatedData, SetOptions.merge())
                .addOnSuccessListener(documentReference -> {
                    System.out.println("Event data updated successfully.");
                })
                .addOnFailureListener(error -> {
                    System.err.println("Error updating event data: " + error.getMessage());
                });
    }


    public void deleteEvent(String eventID){
        eventsRef.document(eventID).delete().addOnSuccessListener(documentReference -> {
            System.out.println("Success");
        }).addOnFailureListener(error -> {
            System.err.println("Failure " + error.getMessage());
        });
    }


}
