package com.example.cs501_runbuddy.models;

import com.example.cs501_runbuddy.RunBuddyApplication;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class User implements Serializable {
    public String userID;
    public String userFirstName;
    public String userLastName;
    public String userEmail;

    public User() {

    }

    public User(String userID, String userFirstName, String userLastName, String userEmail) {
        this.userID = userID;
        this.userFirstName = userFirstName;
        this.userLastName = userLastName;
        this.userEmail = userEmail;
    }

    @Exclude
    public Map<String, Object> toMap() {

        HashMap<String, Object> result = new HashMap<>();
        result.put("userID", userID);
        result.put("userFirstName", userFirstName);
        result.put("userLastName", userLastName);
        result.put("userEmail", userEmail);

        return result;
    }

    public void writeToDatabase() {
        FirebaseDatabase db = RunBuddyApplication.getDatabase();
        DatabaseReference userRef = db.getReference("users");

        Map<String, Object> userValues = this.toMap();

        // Write a message to the database
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + userID, userValues);
        userRef.updateChildren(childUpdates);
    }
}
