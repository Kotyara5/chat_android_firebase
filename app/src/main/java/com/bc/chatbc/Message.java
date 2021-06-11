package com.bc.chatbc;

import java.util.Date;

public class Message {
    public String userName;
    public String textMessage;
    private long timeMessage;

    public Message() {}
    public Message(String userName, String textMessage) {
        this.userName = userName;
        this.textMessage = textMessage;
        this.timeMessage = new Date().getTime();
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTextMessage() {
        return textMessage;
    }

    public void setTextMessage(String textMessage) {
        this.textMessage = textMessage;
    }

    public long getTimeMessage() {
        return timeMessage;
    }

    public void setTimeMessage(long timeMessage) {
        this.timeMessage = timeMessage;
    }
}
