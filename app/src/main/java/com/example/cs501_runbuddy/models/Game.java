package com.example.cs501_runbuddy.models;

import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.example.cs501_runbuddy.RunBuddyApplication;
import com.example.cs501_runbuddy.SearchFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Game implements Serializable {

    public String ID;//Game Lobby ID
    public Boolean isPrivate;//private or public
    public Boolean joinAble;//Is the game ready to be searchable
    public Double totalDistance;//mile for this game


    public String playerOneId; // Player one name
    public ArrayList<RaceLocation> playerOneLocation;//distance that player One has been completed during the time
    public String playerTwoId; // PLayer two name
    public ArrayList<RaceLocation> playerTwoLocation;//distance that player Two has been completed and system time



    //default initiate, which should never be called
    public Game(){

    }

    //Starting Game with id and type
    public Game(String ID,
                Boolean isPrivate,
                Double totalDistance,
                Boolean joinAble,
                String playerOneId,
                String playerTwoId,
                ArrayList<RaceLocation> playerOneLocation,
                ArrayList<RaceLocation> playerTwoLocation){

        this.isPrivate = isPrivate;
        this.ID = ID;
        this.joinAble = joinAble;
        this.totalDistance = totalDistance;
        this.playerOneId = playerOneId;
        this.playerTwoId = playerTwoId;
        this.playerOneLocation = playerOneLocation;
        this.playerTwoLocation = playerTwoLocation;

    }

    private String getID(){
        return this.ID;
    }

    public void setJoinAble(boolean x){
        this.joinAble = x;
    }

    public void addLocData(Boolean isPlayer1, LatLng loc, double time) {
        if (isPlayer1) {
            RaceLocation curRaceLoc = new RaceLocation(loc, time);
            playerOneLocation.add(curRaceLoc);
            writeToDatabase("playerOneLocation");
        }
        else {
            RaceLocation curRaceLoc = new RaceLocation(loc, time);
            playerTwoLocation.add(curRaceLoc);
            writeToDatabase("playerTwoLocation");
        }
    }

    @Exclude
    public Map<String, Object> toMap() {

        HashMap<String, Object> result = new HashMap<>();
        result.put("ID", ID);
        result.put("isPrivate", isPrivate);
        result.put("joinAble", joinAble);
        result.put("totalDistance", totalDistance);
        result.put("playerOneId", playerOneId);
        result.put("playerTwoId", playerTwoId);
        result.put("playerOneLocation", playerOneLocation);
        result.put("playerTwoLocation", playerTwoLocation);

        return result;
    }

    public void writeToDatabase(String field) {
        FirebaseDatabase db = RunBuddyApplication.getDatabase();
        DatabaseReference gameRef = db.getReference("games");

        Map<String, Object> gameValues = this.toMap();
        //Update database to have proper player locations
        if (field != "") {
            gameRef.child(ID).child(field).setValue(gameValues.get(field));
        }else{
            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("/" + ID, gameValues);
            gameRef.updateChildren(childUpdates);
        }
    }

    public static void joinGameFromDB(String gameId, SearchFragment.SearchGame listener, FragmentActivity context) {
        DatabaseReference gamesRef = RunBuddyApplication.getDatabase().getReference("games");

        Query query = gamesRef.orderByChild("ID").equalTo(gameId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "game" node with all children with id equal to joinId
                    for (DataSnapshot game : dataSnapshot.getChildren()) {
                        Game g = game.getValue(Game.class);
                        // do something with the individual "game"
                        if (g.joinAble == true)
                            listener.joinGame(g);
                        else
                            Toast.makeText(context, "Couldn't join game, already full", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}
