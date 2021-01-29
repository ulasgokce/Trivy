package com.ulasgokce.myapplication.Models;

public class Notification {
    private String notification_id;
    private String sender;
    private String receiver;
    private String event;

    public Notification(String notification_id, String sender, String receiver, String event) {
        this.notification_id = notification_id;
        this.sender = sender;
        this.receiver = receiver;
        this.event = event;
    }

    public Notification() {

    }

    @Override
    public String toString() {
        return "Notification{" +
                "notification_id='" + notification_id + '\'' +
                ", sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", event='" + event + '\'' +
                '}';
    }

    public String getNotification_id() {
        return notification_id;
    }

    public void setNotification_id(String notification_id) {
        this.notification_id = notification_id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }
}
