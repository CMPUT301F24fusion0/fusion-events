package com.example.fusion0;


import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;




import java.util.HashMap;


public class EventFirebase {

    private final CollectionReference organizersRef;
    private final CollectionReference facilitiesRef;
    private final CollectionReference eventsRef;


    /**
     * Constructor for the EventFirebase class
     */
    public EventFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        organizersRef = db.collection("organizers");
        facilitiesRef = db.collection("facilities");
        eventsRef = db.collection("facilities");
    }


    /**
     * Adds an organizer to the firebase
     * @param organizerInfo a class that contains information about the organizer
     */

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

    /**
     * Allows for the editing of the organizer through the organizerInfo class
     * @param organizerInfo the class that contains the organizer information
     * @param updatedData the hashmap that contains edited information about the organizer
     */
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

    /**
     * This function allows for the deletion of an organizer based on their device id
     * @param deviceId a string containing the device id
     */
    public void deleteOrganizer(String deviceId){
        organizersRef.document(deviceId).delete().addOnSuccessListener(documentReference -> {
            System.out.println("Success");
        }).addOnFailureListener(error -> {
            System.err.println("Failure " + error.getMessage());
        });
    }


    /**
     * Add a facility based on the FacilitiesInfo class
     * @param facilitiesInfo the class containing the facility information
     */
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



    /**
     * This function allows for the editing of a facility using the information and the hashmap
     * @param facilitiesInfo the class containing the information about the facility
     * @param updatedData the hashmap with the edited information
     */
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


    /**
     * Deletes a facility based on the facilityID
     * @param facilityID a random ID generated in the facility class
     */
    public void deleteFacility(String facilityID){
        facilitiesRef.document(facilityID).delete().addOnSuccessListener(documentReference -> {
            System.out.println("Success");
        }).addOnFailureListener(error -> {
            System.err.println("Failure " + error.getMessage());
        });


    }


    /**
     * Adds a new event to the events collection using the event ID
     * @param eventInfo a class containing the eventInfo information
     */
    public void addEvent(EventInfo eventInfo){
        HashMap<String, Object> event = eventInfo.event();
        String eventID = eventInfo.getEventID();
        eventsRef.document(eventID).set(event).addOnSuccessListener(documentReference -> {
            System.out.println("Success");
        }).addOnFailureListener(error -> {
            System.err.println("Failure " + error.getMessage());
        });
    }



    /**
     * Edits an event using the EventInfo class and a hashmap with the edited information
     * @param eventInfo the class containing all the event information
     * @param updatedData the hashmap with the new edited information
     */
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


    /**
     * Allows for the deletion of an event using the event ID
     * @param eventID the randomly generated eventID generated in the EventInfo class
     */
    public void deleteEvent(String eventID){
        eventsRef.document(eventID).delete().addOnSuccessListener(documentReference -> {
            System.out.println("Success");
        }).addOnFailureListener(error -> {
            System.err.println("Failure " + error.getMessage());
        });
    }

}
