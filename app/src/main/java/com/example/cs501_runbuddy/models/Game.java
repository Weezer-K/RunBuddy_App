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
    public Long date; // The date when the game was created, i.e. 4/18/2022
    public Double totalDistance; // the miles to be ran for this game

    public RacePlayer player1; // player1 of the game
    public RacePlayer player2; // player2 of the game
    public String winner; // the account id of the winning player

    public Boolean isPrivate;// private or public game
    public Boolean joinAble; // is the game joinable with an empty spot
    public Boolean isAsync; // is the game asynchronous or synchronous

    // default constructor, needed for firebase db to work when reading data
    public Game(){

    }

    // constructor that takes all necessary fields
    public Game(String ID,
                Boolean isPrivate,
                Double totalDistance,
                Boolean joinAble,
                Boolean isAsync,
                RacePlayer player1,
                RacePlayer player2,
                String winner,
                Long date){

        this.ID = ID;
        this.isPrivate = isPrivate;
        this.joinAble = joinAble;
        this.isAsync = isAsync;
        this.totalDistance = totalDistance;
        this.player1 = player1;
        this.player2 = player2;
        this.winner = winner;
        this.date = date;
    }

    // helper function that allows a user to write a gps coordinate to the db
    // with a corresponding timestamp
    public void addLocData(Boolean isPlayer1, LatLngDB loc, double time) {

        // get db instance and create reference to the games table
        FirebaseDatabase db = RunBuddyApplication.getDatabase();
        DatabaseReference gameRef = db.getReference("games");

        // check if we are updating player1 or player 2
        if (isPlayer1) {
            // check if this is the first location object for this player
            // if so, instantiate hashmap for location data
            if(player1.playerLocation == null){
                player1.playerLocation = new HashMap<String, RaceLocation>();
            }

            // create RaceLocation object with location and time
            RaceLocation curRaceLoc = new RaceLocation(loc, time);

            // get new key for hashmap from db
            DatabaseReference key = gameRef.child(ID).child("player1/playerLocation").push();

            // put race location object in the player's location hashmap
            player1.playerLocation.put(key.toString(), curRaceLoc);

            // write the location data to the database
            key.setValue(player1.playerLocation.get(key.toString()));
        }
        else {
            // check if this is the first location object for this player
            // if so, instantiate hashmap for location data
            if(player2.playerLocation == null){
                player2.playerLocation = new HashMap<String, RaceLocation>();
            }

            // create RaceLocation object with location and time
            RaceLocation curRaceLoc = new RaceLocation(loc, time);

            // get new key for hashmap from db
            DatabaseReference key = gameRef.child(ID).child("player2/playerLocation").push();

            // put race location object in the player's location hashmap
            player2.playerLocation.put(key.toString(), curRaceLoc);

            // write the location data to the database
            key.setValue(player2.playerLocation.get(key.toString()));
        }
    }

    // toMap function converts Game object into HashMap<String, Object> with each attribute
    // hashmap of string keys and object values is the accepted format to write to the database
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

    // helper function that writes updates to the game table, possibly updating a field
    // a subfield within field could also be targeted for the db update
    public void writeToDatabase(String field, String subField) {

        // get db instance and create reference to the games table
        FirebaseDatabase db = RunBuddyApplication.getDatabase();
        DatabaseReference gameRef = db.getReference("games");

        // get db format of the game object
        Map<String, Object> gameValues = this.toMap();

        // if field != "", then we arent updating the whole game object
        if (!field.equals("")) {

            // check if we are trying to update player1 data
            if(field.equals("player1")){
                // get db format of the race player object for player1
                Map<String, Object> player1Values = player1.toMap();

                // if subfield == "", write the whole player1 object to the db
                if (subField.equals(""))
                    gameRef.child(ID).child(field).setValue(player1Values);

                // else, update the specific player1 field in the db
                else
                    gameRef.child(ID).child(field + "/" + subField).setValue(player1Values.get(subField));
            }
            // check if we are trying to update player2 data
            else if (field.equals("player2")) {
                // get db format of the race player object for player2
                Map<String, Object> player2Values = player2.toMap();

                // if subfield == "", write the whole player1 object to the db
                if (subField.equals(""))
                    gameRef.child(ID).child(field).setValue(player2Values);

                // else, update the specific player1 field in the db
                else
                    gameRef.child(ID).child(field + "/" + subField).setValue(player2Values.get(subField));
            }
            // else, update non-player specific data i.e. game isJoinAble changes
            else {
                gameRef.child(ID).child(field).setValue(gameValues.get(field));
            }
        }

        // overwrite the whole game object, usually just used when a game is created
        else {
            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("/" + ID, gameValues);
            gameRef.updateChildren(childUpdates);
        }
    }

    // helper function that allows a player to join a created game by using the game ID
    public static void joinGameFromDB(String gameId, SearchFragment.SearchGame listener, FragmentActivity context) {

        // get db instance and create reference to the games table
        FirebaseDatabase db = RunBuddyApplication.getDatabase();
        DatabaseReference gamesRef = db.getReference("games");

        // create a query to read a game from the db by using it's ID
        Query query = gamesRef.orderByChild("ID").equalTo(gameId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // if data request was successful
                if (dataSnapshot.exists()) {
                    // should be one since there should be no duplicates
                    // must loop though since there is no guarantee that just one object is returned
                    for (DataSnapshot game : dataSnapshot.getChildren()) {

                        // convert child into game object
                        Game g = game.getValue(Game.class);

                        // if the game is joinable and isn't current player's own game,
                        // allow it to be joined by the logged in user
                        if (g.joinAble == true && !g.player1.playerId.equals(GoogleSignIn.getLastSignedInAccount(context).getId())) {
                            listener.joinGame(g);
                        }
                        // else, toast respective message
                        else if(!g.joinAble){
                            Toast.makeText(context, "You cannot join a full game", Toast.LENGTH_SHORT).show();
                        }
                        else if(g.player1.playerId.equals(GoogleSignIn.getLastSignedInAccount(context).getId())){
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

    // helper function that converts the game's date from Long to String in the display format
    public String getStringDate() {
        String formatDate = "";

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // convert UTC date to local time zone date
            LocalDateTime date = Instant.ofEpochSecond(this.date).atZone(ZoneId.of("UTC"))
                    .withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();

            // create string formatter for datetime object and format the object
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            formatDate = date.format(formatter);
        }

        return formatDate;
    }

    // callback interface used so that data can be manipulated after it is retrieved
    // workaround for the async nature of data calls
    public interface OtherPlayerCallback {
        void onCallback();
    }

    // helper function read other player data from db so logged in user has other player data locally
    public void readOtherPlayer(boolean isPlayer1, Game.OtherPlayerCallback myCallback) {

        // create db reference listening to the other player
        DatabaseReference otherPlayerLocationRef;

        // if logged in user is player1, then create reference to player2
        if(isPlayer1) {
            otherPlayerLocationRef = RunBuddyApplication.getDatabase().getReference("games").child(ID).child("player2");
        }
        // else, create reference to player1
        else{
            otherPlayerLocationRef = RunBuddyApplication.getDatabase().getReference("games").child(ID).child("player1");
        }

        // add value event listener to the reference
        otherPlayerLocationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // when other player is read as non-null
                if (dataSnapshot.exists()) {

                    // convert data retrieved into RacePlayer object
                    RacePlayer r = dataSnapshot.getValue(RacePlayer.class);

                    // update game object so that the other player is correctly populated with db data
                    if(isPlayer1){
                        player2 = r;
                    }else{
                        player1 = r;
                    }

                    // execute callback function
                    myCallback.onCallback();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    // callback interface used so that data can be manipulated after it is retrieved
    // workaround for the async nature of data calls
    public interface OtherPlayerDoubleFieldCallback {
        void onCallback(Double value);
    }

    // helper function read other player data specifically for a double field from db
    // so logged in user has other player data locally
    public void readOtherPlayerDoubleField(boolean isPlayer1, String field, Game.OtherPlayerDoubleFieldCallback myCallback) {

        // create db reference listening to the other player
        DatabaseReference otherPlayerLocationRef;

        // if logged in user is player1, then create reference to player2
        if(isPlayer1) {
            otherPlayerLocationRef = RunBuddyApplication.getDatabase().getReference("games")
                    .child(ID).child("player2").child(field);
        }
        // else, create reference to player1
        else{
            otherPlayerLocationRef = RunBuddyApplication.getDatabase().getReference("games")
                    .child(ID).child("player1").child(field);
        }

        // add value event listener to the reference
        otherPlayerLocationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // if retrieved data is non-null
                if (dataSnapshot.exists()) {
                    // execute callback function passing in retrieved value
                    myCallback.onCallback(dataSnapshot.getValue(Double.class));
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    // override the compare function to sort games by their date
    // this is particularly used to sort the games in the list views throughout the app
    // in descending order for date
    @Override
    public int compareTo(Game game) {
        return game.date.intValue() - this.date.intValue();
    }

    // override the equals function for the Game object
    @Override
    public boolean equals(@Nullable Object obj) {
        // if two games have the same ID, they are equal
        Game other = new Game();
        if(obj instanceof Game){ other = (Game) obj; }
        return this.ID.equals(other.ID);
    }
}
