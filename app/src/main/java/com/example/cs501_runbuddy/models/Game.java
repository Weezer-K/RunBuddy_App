package com.example.cs501_runbuddy.models;

import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.example.cs501_runbuddy.RunBuddyApplication;
import com.example.cs501_runbuddy.SearchFragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Game implements Serializable {

    public String ID;//Game Lobby ID
    public Boolean isPrivate;//private or public
    public Boolean joinAble;//Is the game ready to be searchable
    public Double totalDistance;//mile for this game
    public RacePlayer player1;
    public RacePlayer player2;

    public String winner; // Determine who wins
    public Long date; // The date where the game happened, 4/18/2022

    //default initiate, which should never be called
    public Game(){

    }

    //Starting Game with id and type
    public Game(String ID,
                Boolean isPrivate,
                Double totalDistance,
                Boolean joinAble,
                RacePlayer player1,
                RacePlayer player2,
                String winner,
                Long date){

        this.isPrivate = isPrivate;
        this.ID = ID;
        this.joinAble = joinAble;
        this.totalDistance = totalDistance;
        this.player1 = player1;
        this.player2 = player2;
        this.winner = winner;
        this.date = date;
    }


    public void addLocData(Boolean isPlayer1, LatLngDB loc, double time) {
        if (isPlayer1) {
            if(player1.playerLocation == null){
                player1.playerLocation = new ArrayList<RaceLocation>();
            }
            RaceLocation curRaceLoc = new RaceLocation(loc, time);
            player1.playerLocation.add(curRaceLoc);
            writeToDatabase("player1/playerLocation");
        }
        else {
            if(player2.playerLocation == null){
                player2.playerLocation = new ArrayList<RaceLocation>();
            }
            RaceLocation curRaceLoc = new RaceLocation(loc, time);
            player2.playerLocation.add(curRaceLoc);
            writeToDatabase("player2/playerLocation");

        }
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("ID", ID);
        result.put("isPrivate", isPrivate);
        result.put("joinAble", joinAble);
        result.put("totalDistance", totalDistance);
        result.put("player1", player1);
        result.put("player2", player2);
        result.put("winner", winner);
        result.put("date", date);
        return result;
    }

    public void writeToDatabase(String field) {
        FirebaseDatabase db = RunBuddyApplication.getDatabase();
        DatabaseReference gameRef = db.getReference("games");

        Map<String, Object> gameValues = this.toMap();
        //Update database to have proper player locations
        if (field != "" && field.contains("player")) {
            if(field.contains("player1")){
                Map<String, Object> player1Values = player1.toMap();
                if (field.contains("playerLocation"))
                    gameRef.child(ID).child(field).setValue(player1Values.get("playerLocation"));
                else
                    gameRef.child(ID).child(field).setValue(player1Values);
            }else{
                Map<String, Object> player2Values = player2.toMap();
                if (field.contains("playerLocation"))
                    gameRef.child(ID).child(field).setValue(player2Values.get("playerLocation"));
                else
                    gameRef.child(ID).child(field).setValue(player2Values);
            }

        } else {
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
                        if (g.joinAble == true && !g.player1.playerId.equals(GoogleSignIn.getLastSignedInAccount(context).getId())) {
                            listener.joinGame(g);
                        }else if(!g.joinAble){
                            Toast.makeText(context, "You cannot join a full game", Toast.LENGTH_SHORT).show();
                        }else if(g.player1.playerId.equals(GoogleSignIn.getLastSignedInAccount(context).getId())){
                            Toast.makeText(context, "You can't join your own game", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(context, "Couldn't join game", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public String getStringDate() {
        LocalDateTime date = null;
        DateTimeFormatter formatter = null;
        String formatDate = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            date = Instant.ofEpochSecond(this.date).atZone(ZoneId.of("UTC"))
                    .withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
            formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            formatDate = date.format(formatter);
        }

        return formatDate;
    }
}
