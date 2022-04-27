package com.example.cs501_runbuddy.models;

import android.widget.Toast;

import androidx.annotation.Nullable;
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
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class Game implements Serializable, Comparable<Game>{

    public String ID;//Game Lobby ID
    public Boolean isPrivate;//private or public
    public Boolean joinAble;//Is the game ready to be searchable
    public Boolean isAsync;//Is the game asynchronous or synchronous
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
                Boolean isAsync,
                RacePlayer player1,
                RacePlayer player2,
                String winner,
                Long date){

        this.isPrivate = isPrivate;
        this.ID = ID;
        this.joinAble = joinAble;
        this.isAsync = isAsync;
        this.totalDistance = totalDistance;
        this.player1 = player1;
        this.player2 = player2;
        this.winner = winner;
        this.date = date;
    }


    public void addLocData(Boolean isPlayer1, LatLngDB loc, double time) {
        FirebaseDatabase db = RunBuddyApplication.getDatabase();
        DatabaseReference gameRef = db.getReference("games");
        if (isPlayer1) {
            if(player1.playerLocation == null){
                player1.playerLocation = new HashMap<String, RaceLocation>();
            }
            RaceLocation curRaceLoc = new RaceLocation(loc, time);
            DatabaseReference key = gameRef.child(ID).child("player1/playerLocation").push();
            player1.playerLocation.put(key.toString(), curRaceLoc);
            key.setValue(player1.playerLocation.get(key.toString()));
        }
        else {
            if(player2.playerLocation == null){
                player2.playerLocation = new HashMap<String, RaceLocation>();
            }
            RaceLocation curRaceLoc = new RaceLocation(loc, time);
            DatabaseReference key = gameRef.child(ID).child("player2/playerLocation").push();
            player2.playerLocation.put(key.toString(), curRaceLoc);
            key.setValue(player2.playerLocation.get(key.toString()));
        }
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("ID", ID);
        result.put("isPrivate", isPrivate);
        result.put("joinAble", joinAble);
        result.put("isAsync", isAsync);
        result.put("totalDistance", totalDistance);
        result.put("player1", player1);
        result.put("player2", player2);
        result.put("winner", winner);
        result.put("date", date);
        return result;
    }

    public void writeToDatabase(String field, String subField) {
        FirebaseDatabase db = RunBuddyApplication.getDatabase();
        DatabaseReference gameRef = db.getReference("games");

        Map<String, Object> gameValues = this.toMap();
        //Update database to have proper player locations
        if (!field.equals("")) {
            if(field.equals("player1")){
                Map<String, Object> player1Values = player1.toMap();
                if (subField.equals(""))
                    gameRef.child(ID).child(field).setValue(player1Values);
                else
                    gameRef.child(ID).child(field + "/" + subField).setValue(player1Values.get(subField));
            } else if (field.equals("player2")) {
                Map<String, Object> player2Values = player2.toMap();
                if (subField.equals(""))
                    gameRef.child(ID).child(field).setValue(player2Values);
                else
                    gameRef.child(ID).child(field + "/" + subField).setValue(player2Values.get(subField));
            } else {
                gameRef.child(ID).child(field).setValue(gameValues.get(field));
            }
        }else {
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

    public interface OtherPlayerCallback {
        void onCallback();
    }

    public void readOtherPlayer(boolean isPlayer1, Game.OtherPlayerCallback myCallback) {
        DatabaseReference otherPlayerLocationRef;
        if(isPlayer1) {
            otherPlayerLocationRef = RunBuddyApplication.getDatabase().getReference("games").child(ID).child("player2");
        }else{
            otherPlayerLocationRef = RunBuddyApplication.getDatabase().getReference("games").child(ID).child("player1");
        }


        otherPlayerLocationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    RacePlayer r = dataSnapshot.getValue(RacePlayer.class);
                    if(isPlayer1){
                        player2 = r;
                    }else{
                        player1 = r;
                    }
                    myCallback.onCallback();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    public interface OtherPlayerDoubleFieldCallback {
        void onCallback(Double value);
    }

    public void readOtherPlayerDoubleField(boolean isPlayer1, String field, Game.OtherPlayerDoubleFieldCallback myCallback) {
        DatabaseReference otherPlayerLocationRef;
        if(isPlayer1) {
            otherPlayerLocationRef = RunBuddyApplication.getDatabase().getReference("games")
                    .child(ID).child("player2").child(field);
        }else{
            otherPlayerLocationRef = RunBuddyApplication.getDatabase().getReference("games")
                    .child(ID).child("player1").child(field);
        }


        otherPlayerLocationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    myCallback.onCallback(dataSnapshot.getValue(Double.class));
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    @Override
    public int compareTo(Game game) {
        return game.date.intValue() - this.date.intValue();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        Game other = new Game();
        if(obj instanceof Game){ other = (Game) obj; }
        return this.ID.equals(other.ID);
    }
}
