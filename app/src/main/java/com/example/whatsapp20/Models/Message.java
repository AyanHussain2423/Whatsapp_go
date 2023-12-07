package com.example.whatsapp20.Models;

public class Message {
    private String messageId, message, senderId, imageUrl, cimageurl ;

    private long timestamp;
    private long feeling = -1;

    public Message() {
    }
 
    public Message(String message, String senderId, long timestamp) {
        this.message = message;
        this.senderId = senderId;
        this.timestamp = timestamp;
    }

    public String getCimageurl() {
        return cimageurl;
    }

    public void setCimageurl(String cimageurl) {
        this.cimageurl = cimageurl;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getFeeling() {
        return feeling;
    }

    public void setFeeling(long feeling) {
        this.feeling = feeling;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

}