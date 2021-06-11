package com.bc.chatbc;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class User {
    private String id, email, password, name;

    public User(){}
    public User(String id, String email, String password, String name) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
    }
    //В списке друзей ключом является ид друга, а содержимым ид диалога
    public void addFriendsAndDialogs(String idOfFriends, String idOfDialogs) {
        DatabaseReference ref_friends = FirebaseDatabase.getInstance().getReference("Users/" + id + "/Friends");
        ref_friends.child(idOfFriends).setValue(idOfDialogs);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
