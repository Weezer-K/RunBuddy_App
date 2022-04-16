package com.example.cs501_runbuddy.models;

import android.widget.Toast;

import com.example.cs501_runbuddy.RunBuddyApplication;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

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

    public static String getUserNameFromID(String id) {

        DatabaseReference usersRef = RunBuddyApplication.getDatabase().getReference("users");

        Query query = usersRef.orderByChild("userID").equalTo(id);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "game" node with all children with id equal to joinId
                    for (DataSnapshot user : dataSnapshot.getChildren()) {
                        User u = user.getValue(User.class);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        return "";
    }
}
