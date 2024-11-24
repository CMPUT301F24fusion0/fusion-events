package com.example.fusion0.models;

/**
 * Model for a notification item that will be populated by Firestore.
 * It contains the title, body, and flag to distinguish different types of notifications.
 *
 * @author Nimi Akinroye
 */
public class NotificationItem {
    private String title;
    private String body;
    private String flag;

    /**
     * Constructs a new NotificationItem.
     * @author Nimi Akinroye
     * @param title The title of the notification.
     * @param body  The body/content of the notification.
     * @param flag  The flag indicating the type of notification (e.g., "0" for standard, "1" for accept/decline type).
     */
    public NotificationItem(String title, String body, String flag) {
        this.title = title;
        this.body = body;
        this.flag = flag;
    }

    /**
     * Returns the title of the notification.
     * @author Nimi Akinroye
     * @return The notification title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the body/content of the notification.
     * @author Nimi Akinroye
     * @return The notification body.
     */
    public String getBody() {
        return body;
    }

    /**
     * Returns the flag indicating the type of notification.
     * @author Nimi Akinroye
     * @return The notification flag.
     */
    public String getFlag() {
        return flag;
    }
}