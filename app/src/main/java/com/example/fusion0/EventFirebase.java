package com.example.fusion0;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;


import java.util.HashMap;

public class EventFirebase {

    private final CollectionReference organizersRef;
    private final CollectionReference facilitiesRef;
    private final CollectionReference eventsRef;




    public EventFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        organizersRef = db.collection("organizers");
        facilitiesRef = db.collection("facilities");
        eventsRef = db.collection("facilities");
    }


    public void addOrganizer(OrganizerInfo organizerInfo){
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

    public void editOrganizer(OrganizerInfo organizerInfo, HashMap<String, Object> updatedData) {
        String deviceId = organizerInfo.getDeviceId();
        organizersRef.document(deviceId).set(updatedData, SetOptions.merge())
                .addOnSuccessListener(documentReference -> {
                    System.out.println("Organizer data updated successfully.");
                })
                .addOnFailureListener(error -> {
                    System.err.println("Error updating organizer data: " + error.getMessage());
                });
    }

    public void deleteOrganizer(String deviceId){
        organizersRef.document(deviceId).delete().addOnSuccessListener(documentReference -> {
            System.out.println("Success");
        }).addOnFailureListener(error -> {
            System.err.println("Failure " + error.getMessage());
        });
    }

    public void addFacility(FacilitiesInfo facilitiesInfo){
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

    public void deleteFacility(String facilityID){
        facilitiesRef.document(facilityID).delete().addOnSuccessListener(documentReference -> {
                    System.out.println("Success");
                }).addOnFailureListener(error -> {
                    System.err.println("Failure " + error.getMessage());
                });

    }


    public void addEvent(EventInfo eventInfo){
        HashMap<String, Object> event = eventInfo.event();
        String eventID = eventInfo.getEventID();
        eventsRef.document(eventID).set(event).addOnSuccessListener(documentReference -> {
                    System.out.println("Success");
                }).addOnFailureListener(error -> {
                    System.err.println("Failure " + error.getMessage());
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
