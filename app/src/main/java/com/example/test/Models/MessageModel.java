package com.example.test.Models;

public class MessageModel {
    String Uid,message,messageId,type,localisation;
    Long timeStamp;

    public MessageModel(String uid, String message, String messageId, String type, Long timeStamp,String localisation) {
        Uid = uid;
        this.message = message;
        this.messageId = messageId;
        this.type = type;
        this.timeStamp = timeStamp;
        this.localisation=localisation;
    }


    public MessageModel(String uid, String message, String type,String localisation) {
        Uid = uid;
        this.message = message;
        this.type = type;
        this.localisation=localisation;
    }

    public MessageModel() {
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getLocalisation() {
        return localisation;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }
}
