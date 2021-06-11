package com.bc.chatbc;

public class Dialog {
    private String nameFriend, nameUser, lastMessage;

    public Dialog(){}
    public Dialog(String nameFriend, String userName){
        this.nameUser = userName;
        this.nameFriend = nameFriend;
        this.lastMessage = "...";
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getNameFriend() {
        return nameFriend;
    }

    public void setNameFriend(String nameFriend) {
        this.nameFriend = nameFriend;
    }

    public String getNameUser() {
        return nameUser;
    }

    public void setNameUser(String nameUser) {
        this.nameUser = nameUser;
    }
}
