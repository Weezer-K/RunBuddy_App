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

    // user attributes, extracted after successful google sign in
    public String userID;
    public String userFirstName;
    public String userLastName;
    public String userEmail;

    // default constructor, needed for firebase db to work when reading data
    public User() {

    }

    // constructor that takes all necessary fields
    public User(String userID, String userFirstName, String userLastName, String userEmail) {
        this.userID = userID;
        this.userFirstName = userFirstName;
        this.userLastName = userLastName;
        this.userEmail = userEmail;
    }

    // toMap function converts User object into HashMap<String, Object> with each attribute
    // hashmap of string keys and object values is the accepted format to write to the database
    @Exclude
    public Map<String, Object> toMap() {

        HashMap<String, Object> result = new HashMap<>();
        result.put("userID", userID);
        result.put("userFirstName", userFirstName);
        result.put("userLastName", userLastName);
        result.put("userEmail", userEmail);

        return result;
    }

    // helper function that writes a user object to the db
    // needed so that we can display other user's names to each other while in the app
    public void writeToDatabase() {

        // get db instance and create reference to the games table
        FirebaseDatabase db = RunBuddyApplication.getDatabase();
        DatabaseReference userRef = db.getReference("users");

        // get db format of the user object
        Map<String, Object> userValues = this.toMap();

        // write the user object to the db in the accepted format
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + userID, userValues);
        userRef.updateChildren(childUpdates);
    }

    // callback interface used so that data can be manipulated after it is retrieved
    // workaround for the async nature of data calls
    public interface MyCallback {
        void onCallback(String value);
    }

    // helper function that takes in a user's ID and returns its first name as a string
    // needed to display a other players' names while using the app
    public static void getUserNameFromID(String id, MyCallback myCallback) {

        // get db instance and create reference to the users table
        DatabaseReference usersRef = RunBuddyApplication.getDatabase().getReference("users");

        // create a query to read a user from the db by using it's ID
        Query query = usersRef.orderByChild("userID").equalTo(id);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // if data request was successful
                if (dataSnapshot.exists()) {
                    // should be one since there should be no duplicates
                    // must loop though since there is no guarantee that just one object is returned
                    for (DataSnapshot user : dataSnapshot.getChildren()) {
                        // convert child into user object
                        User u = user.getValue(User.class);

                        // extract user's first name and pass it into the callback function
                        // callback is usually to display the name on the UI
                        myCallback.onCallback(u.userFirstName);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}
