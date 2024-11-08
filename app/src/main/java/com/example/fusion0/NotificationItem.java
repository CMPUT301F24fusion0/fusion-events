package com.example.fusion0;

public class NotificationItem {
    private String title;
    private String body;
    private String flag;

    public NotificationItem(String title, String body, String flag) {
        this.title = title;
        this.body = body;
        this.flag = flag;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getFlag() {
        return flag;
    }

}
