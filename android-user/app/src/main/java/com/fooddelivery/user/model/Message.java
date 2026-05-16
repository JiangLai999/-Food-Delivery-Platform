package com.fooddelivery.user.model;

import java.io.Serializable;

public class Message implements Serializable {
    private String id;
    private String sender; // 'user', 'merchant', 'rider'
    private String text;
    private String time;

    public Message() {
    }

    public Message(String id, String sender, String text, String time) {
        this.id = id;
        this.sender = sender;
        this.text = text;
        this.time = time;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
}
