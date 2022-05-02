package com.example.cs501_runbuddy;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cs501_runbuddy.models.Game;
import com.example.cs501_runbuddy.models.RaceLocation;
import com.example.cs501_runbuddy.models.RacePlayer;
import com.example.cs501_runbuddy.models.User;
import com.fitbit.api.loaders.ResourceLoaderResult;
import com.fitbit.api.models.HeartRateContainer;
import com.fitbit.api.models.HeartRateData;
import com.fitbit.api.services.HeartRateService;
import com.fitbit.authentication.AuthenticationManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.skydoves.balloon.ArrowOrientation;
import com.skydoves.balloon.ArrowPositionRules;
import com.skydoves.balloon.Balloon;
import com.skydoves.balloon.BalloonAnimation;
import com.skydoves.balloon.BalloonSizeSpec;
import com.skydoves.balloon.overlay.BalloonOverlayAnimation;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;

public class ResultActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ResourceLoaderResult<HeartRateContainer>>, OnMapReadyCallback{

    private TextView tvResult;
    private TextView localNameTextView;
    private TextView otherNameTextView;
    private TextView tvGameDate;
    private TextView distanceLocal;
    private TextView paceLocal;
    private TextView timeRanLocal;
    private TextView distanceOther;
    private TextView paceOther;
    private TextView timeRanOther;
    private TextView localHeartRate;
    private TextView otherHeartRate;
    private TextView winnerLoser;

    private ImageView info1;

    private Button mapLocal;
    private Button mapOther;

    private Game game;

    private DatabaseReference otherPlayerFinishedRef;
    private DatabaseReference otherPlayerStartedRef;

    private ValueEventListener otherPlayerStartedListener;
    private ValueEventListener otherPlayerFinishedListener;

    private GoogleMap mapApi;
    private SupportMapFragment mapFragment;

    private boolean mapLocalActivated;
    private boolean mapOtherActivated;
    private boolean isPlayer1;

    private int colorSlowPace = Color.RED;
    private int colorMediumPace = Color.YELLOW;
    private int colorFastPace = Color.GREEN;

    private Bitmap startIcon;
    private Bitmap finishIcon;

    private ArrayList<RaceLocation> localRaceLocations;
    private ArrayList<RaceLocation> otherRaceLocations;
    private LatLngBounds.Builder localBounds = new LatLngBounds.Builder();;
    private LatLngBounds.Builder otherBounds = new LatLngBounds.Builder();;

    //Initializes views and sets onClick for both map buttons
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // To retrieve game object from Race Activity
        game = (Game) getIntent().getSerializableExtra("game");
        tvGameDate = (TextView) findViewById(R.id.tvGameDate);
        isPlayer1 = (game.player1.playerId.equals(GoogleSignIn.getLastSignedInAccount(this).getId()));
        localNameTextView = (TextView) findViewById(R.id.localName);
        otherNameTextView = (TextView) findViewById(R.id.otherName);
        distanceLocal = (TextView) findViewById(R.id.distanceLocalPlayer);
        distanceOther = (TextView) findViewById(R.id.distanceOtherPlayer);
        paceLocal = (TextView) findViewById(R.id.paceLocalPlayer);
        paceOther = (TextView) findViewById(R.id.paceOtherPlayer);
        timeRanLocal = (TextView) findViewById(R.id.finishTimeLocalPlayer);
        timeRanOther = (TextView) findViewById(R.id.finishTimeOtherPlayer);
        mapLocal = (Button) findViewById(R.id.localPlayerMapButton);
        mapOther = (Button) findViewById(R.id.otherPlayerMapButton);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.localMapAPI);
        mapFragment.getMapAsync(this);
        mapFragment.getView().setVisibility(View.INVISIBLE);
        localHeartRate = (TextView) findViewById(R.id.avgHeartRateLocal);
        otherHeartRate = (TextView) findViewById(R.id.avgHeartRateOther);
        winnerLoser = (TextView) findViewById(R.id.winnerLoserText);
        info1 = (ImageView) findViewById(R.id.heartRateInfo1);
        mapLocal.setTextColor(Color.GRAY);

        int height = 80;
        int width = 80;
        BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.start_image);
        Bitmap b=bitmapdraw.getBitmap();
        startIcon = Bitmap.createScaledBitmap(b, width, height, false);

        BitmapDrawable bitmapdraw2=(BitmapDrawable)getResources().getDrawable(R.drawable.finish_image);
        Bitmap b2=bitmapdraw2.getBitmap();
        finishIcon = Bitmap.createScaledBitmap(b2, width, height, false);


        //Used to help know if a map is on screen
        mapLocalActivated = false;
        mapOtherActivated = false;

        //Sets the game textView that indicates the date
        //the game was created on
        tvGameDate.setText(game.getStringDate());

        //This is an object that helps with our info icons
        //These icons create a nice way of displaying extra information
        //Such as instruction on what a setting does
        //in this case we use them to explain how to sync
        //your fitbit heart rate to app
        Balloon balloon = new Balloon.Builder(getApplicationContext())
                .setArrowSize(10)
                .setArrowOrientation(ArrowOrientation.TOP)
                .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
                .setArrowPosition(0.5f)
                .setWidth(BalloonSizeSpec.WRAP)
                .setHeight(BalloonSizeSpec.WRAP)
                .setTextSize(15f)
                .setCornerRadius(4f)
                .setAlpha(0.9f)
                .setIsVisibleOverlay(true)
                .setBalloonOverlayAnimation(BalloonOverlayAnimation.FADE)
                .setText("If signed in with fitbit, make sure to sync your device in the fitbit app. This will allow RunBuddy to retrieve and display heart rate data.")
                .setTextColor(Color.WHITE)
                .setOverlayPadding(6f)
                .setOverlayColor(Color.parseColor("#9900203F"))
                .setTextIsHtml(true)
                .setBackgroundColor(Color.parseColor("#242526"))
                .setMargin(10)
                .setPadding(10)
                .setBalloonAnimation(BalloonAnimation.FADE).build();

        //Sets an on click listener for the info image so it can use the ballon
        //object we just made
        info1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                balloon.showAlignBottom(info1);
            }
        });

        //Overall logic for mapLocal button
        //Checks if the other map is open or not
        //check if the local map is open already
        mapLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //used to determine what color to set the
                //Button text. Gray for deactivated
                //#00203F/Black like color for activated
                ColorStateList cl = mapLocal.getTextColors();
                if(cl.getDefaultColor() == (Color.parseColor("#00203F"))){
                    mapLocal.setTextColor(Color.GRAY);
                }else{
                    mapLocal.setTextColor(Color.parseColor("#00203F"));
                }
                //If the mapFragment is visible
                if(!mapFragment.isVisible()) {
                    if (isPlayer1 && game.player1.playerFinished) {
                        mapButtonsPressed(game.player1, true);
                        mapLocalActivated = true;
                       // mapLocal.setBackgroundColor(Color.GRAY);
                    }else if(!isPlayer1 && game.player2.playerFinished){ //If the the local user is player 2 and finished
                        mapButtonsPressed(game.player2, true);
                        mapLocalActivated = true;
                       // mapLocal.setBackgroundColor(Color.GRAY);
                    }
                }else{
                    if(mapLocalActivated){
                        mapLocalActivated = false;
                        mapApi.clear();
                        mapFragment.getView().setVisibility(View.INVISIBLE);
                    }else if(mapOtherActivated){
                        mapOtherActivated = false;
                        mapLocalActivated = true;
                        mapApi.clear();
                        if (isPlayer1 && game.player1.playerFinished){
                            mapButtonsPressed(game.player1, true);
                        }else if(!isPlayer1 && game.player2.playerFinished){
                            mapButtonsPressed(game.player2, true);
                        }
                    }
                }
            }
        });


        //Logic for the map button for the nonLocal player
        //Same as mapLocal, but deals with otherPlayer map
        mapOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mapFragment.isVisible()) {
                    if (isPlayer1 && game.player2.playerFinished) {
                        mapButtonsPressed(game.player2, false);
                        mapOtherActivated = true;
                        //mapOther.setBackgroundColor(Color.GRAY);
                    } else if (!isPlayer1 && game.player1.playerFinished) {
                        mapButtonsPressed(game.player1, false);
                        mapOtherActivated = true;
                        //mapOther.setBackgroundColor(Color.GRAY);
                    }
                } else {
                    if (mapOtherActivated) {
                        mapOtherActivated = false;
                        mapApi.clear();
                        mapFragment.getView().setVisibility(View.INVISIBLE);
                        //mapOther.setBackgroundColor(activateColor);
                    } else if (mapLocalActivated) {
                        mapLocalActivated = false;
                        mapOtherActivated = true;
                        mapApi.clear();
                        if (isPlayer1 && game.player2.playerFinished) {
                            mapButtonsPressed(game.player2, false);
                            //mapLocal.setBackgroundColor(activateColor);
                        } else if (!isPlayer1 && game.player1.playerFinished) {
                            mapButtonsPressed(game.player1, false);
                            //mapLocal.setBackgroundColor(activateColor);
                        }
                    }
                }
            }
        });

        //otherPlayerFinishedRef and otherPlayerStartedRef initalizers
        // depends on if logged in player is player 1 or 2 of the game
        if (isPlayer1) {
            otherPlayerFinishedRef = RunBuddyApplication.getDatabase().getReference("games").child(game.ID).child("player2").child("playerFinished");
            otherPlayerStartedRef = RunBuddyApplication.getDatabase().getReference("games").child(game.ID).child("player2").child("playerStarted");
        } else {
            otherPlayerFinishedRef = RunBuddyApplication.getDatabase().getReference("games").child(game.ID).child("player1").child("playerFinished");
            otherPlayerStartedRef = RunBuddyApplication.getDatabase().getReference("games").child(game.ID).child("player1").child("playerStarted");
        }

        //This is a helper function that
        //Sets all the UI
        setTextViews();

        //Listener to see if the otherplayer finished
        //if so we can set UI and determine the winner
        otherPlayerFinishedListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // if the other player has finished their race according to db
                    if (snapshot.getValue(Boolean.class)) {
                        // read all of their data from the db
                        game.readOtherPlayer(isPlayer1, new Game.OtherPlayerCallback() {
                            @Override
                            public void onCallback() {
                                // once all of other player's data is retrieved, the game object
                                // would be updated accordingly
                                // we then attempt to find a winner and then set the text views of the page
                                getWinner();
                                setTextViews();
                                mapOther.setVisibility(View.VISIBLE);
                                mapOther.setClickable(true);
                                mapOther.setTextColor(Color.GRAY);
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        otherPlayerFinishedRef.addValueEventListener(otherPlayerFinishedListener);

        //Other player start reference
        //Used to help in borderline case where the other player
        //Closes app on race
        //Will finish their race after they pass
        //The maximum allowed time for race
        otherPlayerStartedListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // if the other player has started according to db
                    if (snapshot.getValue(Boolean.class)) {
                        // read the other player start time
                        game.readOtherPlayerLongField(isPlayer1, "playerStartTime", new Game.OtherPlayerLongFieldCallback() {
                            @Override
                            public void onCallback(Long value) {
                                Long date = null;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    // get the current date and time
                                    date = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

                                    // if the difference between current time and the player start time
                                    // is greater than the max time allotted for the race depending on the
                                    // distance of it, fix the corrupt data by marking the other player as finished
                                    // for example, for a 1 mile race, max time allotted is 15 min.
                                    // if more than 15 min has passed, make sure to write to the db that the
                                    // other player is finished
                                    if (value.longValue() != 0 && date - value > game.totalDistance * 15 * 60) {
                                        if (isPlayer1) {
                                            game.player2.playerFinished = true;
                                            game.writeToDatabase("player2", "playerFinished");
                                        } else {
                                            game.player1.playerFinished = true;
                                            game.writeToDatabase("player1", "playerFinished");
                                        }
                                    }
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        otherPlayerStartedRef.addValueEventListener(otherPlayerStartedListener);

        tvResult = findViewById(R.id.tvResult);

        tvResult.setText("Game ID: " + game.ID);

        User.getUserNameFromID(game.player1.playerId, new User.MyCallback() {
            @Override
            public void onCallback(String value) {
                if(isPlayer1){
                    localNameTextView.setText(value);
                }else{
                    otherNameTextView.setText(value);
                }

            }
        });

        User.getUserNameFromID(game.player2.playerId, new User.MyCallback() {
            @Override
            public void onCallback(String value) {
                if(!isPlayer1){
                    localNameTextView.setText(value);
                }else{
                    otherNameTextView.setText(value);
                }
            }
        });

        // if the heart rate data isn't already set in the player object, and the user is logged into fitbit
        // make an api call requesting heart rate data for the time interval of the race for the
        // logged in player
        if(isPlayer1){
            if(game.player1.heartRate.equals(0.0) && AuthenticationManager.isLoggedIn()){
                getLoaderManager().initLoader(getLoaderId(), null, this).forceLoad();
            }
        }else{
            if(game.player2.heartRate.equals(0.0) && AuthenticationManager.isLoggedIn()){
                getLoaderManager().initLoader(getLoaderId(), null, this).forceLoad();
            }
        }
    }

    //When the android back button is pressed
    @Override
    public void onBackPressed() {
        //super.onBackPressed(); // do not call super, especially after a race since you should
        // never go back to the race activity
        otherPlayerFinishedRef.removeEventListener(otherPlayerFinishedListener);
        otherPlayerStartedRef.removeEventListener(otherPlayerStartedListener);
        Intent intent = new Intent(ResultActivity.this, HomeActivity.class);
        intent.putExtra("fragment", "History");
        startActivity(intent);
    }

    // id for heart rate loader (needs to be unique across the app)
    protected int getLoaderId()  {
        return 2;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapApi = googleMap;
    }

    //Helper Function for pressing buttons
    //Takes the raceplayer associated with button
    //And whether or not that player is the local or nonlocal player
    public void mapButtonsPressed(RacePlayer p, boolean isLocal){
        mapFragment.getView().setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            setMap(p, isLocal);
            if(isLocal){
                mapLocal.setTextColor(Color.parseColor("#00203F"));
                mapOther.setTextColor(Color.GRAY);
            }else{
                mapLocal.setTextColor(Color.GRAY);
                mapOther.setTextColor(Color.parseColor("#00203F"));
            }
        }
        double lat = 0;
        double lng = 0;

        if(!isLocal){
            if(otherRaceLocations.size() != 0) {
                lat = otherRaceLocations.get(otherRaceLocations.size() - 1).latLng.lat;
                lng = otherRaceLocations.get(otherRaceLocations.size() - 1).latLng.lng;
            }
        }else{
            if(localRaceLocations.size() != 0) {
                lat = localRaceLocations.get(localRaceLocations.size() - 1).latLng.lat;
                lng = localRaceLocations.get(localRaceLocations.size() - 1).latLng.lng;
            }
        }
    }

    // Todo: remove readOtherPlayerCall
    public void getWinner() {
        if (game.player1.playerFinished && game.player2.playerFinished) {
//            game.readOtherPlayer(isPlayer1, new Game.OtherPlayerCallback() {
//                @Override
//                public void onCallback() {
//                    if (!game.player1.totalDistanceRan.equals(game.totalDistance) && !game.player2.totalDistanceRan.equals(game.totalDistance)) {
//                        if (game.player1.totalDistanceRan >= game.player2.totalDistanceRan) {
//                            game.winner = game.player1.playerId;
//                        } else {
//                            game.winner = game.player2.playerId;
//                        }
//                    } else if (game.player1.totalDistanceRan.equals(game.totalDistance) && game.player2.totalDistanceRan.equals(game.totalDistance)) {
//                        if (game.player1.totalTimeRan < game.player2.totalTimeRan) {
//                            game.winner = game.player1.playerId;
//                        } else {
//                            game.winner = game.player2.playerId;
//                        }
//                    } else if (!game.player1.totalDistanceRan.equals(game.totalDistance)) {
//                        game.winner = game.player2.playerId;
//                    } else {
//                        game.winner = game.player1.playerId;
//                    }
//                    game.writeToDatabase("winner", "");
//                }
//            });
            if (!game.player1.totalDistanceRan.equals(game.totalDistance) && !game.player2.totalDistanceRan.equals(game.totalDistance)) {
                if (game.player1.totalDistanceRan >= game.player2.totalDistanceRan) {
                    game.winner = game.player1.playerId;
                } else {
                    game.winner = game.player2.playerId;
                }
            } else if (game.player1.totalDistanceRan.equals(game.totalDistance) && game.player2.totalDistanceRan.equals(game.totalDistance)) {
                if (game.player1.totalTimeRan < game.player2.totalTimeRan) {
                    game.winner = game.player1.playerId;
                } else {
                    game.winner = game.player2.playerId;
                }
            } else if (!game.player1.totalDistanceRan.equals(game.totalDistance)) {
                game.winner = game.player2.playerId;
            } else {
                game.winner = game.player1.playerId;
            }
            game.writeToDatabase("winner", "");
        }
    }

    //Winner is determined as follows
    //Whoever runs the most distance is the winner
    //if both players run the same distance
    //Than the one with less time wins
    //If both have the same stats it's a tie
    public void setWinner(){
        if(isPlayer1){
            if(game.player1.totalDistanceRan > game.player2.totalDistanceRan){
                winnerLoser.setText("You Won");
            }else if(game.player1.totalDistanceRan < game.player2.totalDistanceRan){
                winnerLoser.setText("You Lost");
            }else{
                if(game.player1.totalTimeRan < game.player2.totalTimeRan){
                    winnerLoser.setText("You Won");
                }else if(game.player1.totalTimeRan > game.player2.totalTimeRan){
                    winnerLoser.setText("You Lost");
                }else{
                    winnerLoser.setText("You Tied");
                }
            }
        }else{
            if(game.player2.totalDistanceRan > game.player1.totalDistanceRan){
                winnerLoser.setText("You Won");
            }else if(game.player2.totalDistanceRan < game.player1.totalDistanceRan){
                winnerLoser.setText("You Lost");
            }else{
                if(game.player2.totalTimeRan < game.player1.totalTimeRan){
                    winnerLoser.setText("You Won");
                }else if(game.player2.totalTimeRan > game.player1.totalTimeRan){
                    winnerLoser.setText("You Lost");
                }else{
                    winnerLoser.setText("You Tied");
                }
            }
        }
    }

    //Helper unction that sets all text views
    //In the UI
    public void setTextViews(){
        if(game.player1.playerFinished && game.player2.playerFinished){
            if(isPlayer1){
                setTextViewsLocal(game.player1);
                setTextViewsOther(game.player2);
                setWinner();
            }else{
                setTextViewsLocal(game.player2);
                setTextViewsOther(game.player1);
                setWinner();

            }
        }else if(game.player1.playerFinished){
            if(isPlayer1){
                setTextViewsLocal(game.player1);
                mapOther.setVisibility(View.INVISIBLE);
                winnerLoser.setText("Other Player Not Finished");
                mapOther.setClickable(false);
            }else{
                setTextViewsOther(game.player1);
                winnerLoser.setText("Other Player Not Finished");
            }
        }else{
            if(!isPlayer1){
                setTextViewsLocal(game.player2);
                mapOther.setVisibility(View.INVISIBLE);
                mapOther.setClickable(false);
                winnerLoser.setText("Other Player Not Finished");
            }else{
                setTextViewsOther(game.player2);
                winnerLoser.setText("Other Player Not Finished");
            }
        }
    }

    //Helper function that's sets the textViews for the local
    //player UI based off of passed Raceplayer
    public void setTextViewsLocal(RacePlayer player){
        DecimalFormat df = new DecimalFormat("#,###.##");
        double minutesDouble = player.totalTimeRan/60000;
        int minutes = (int) minutesDouble;
        int seconds = (int) (player.totalTimeRan % 60000)/1000;
        String secondString = Integer.toString(seconds);
        if (secondString.length() == 1) {
            secondString = "0" + secondString;
        }
        String timeElapsed = minutes + ":" + secondString;
        distanceLocal.setText("Distance: " + df.format(player.totalDistanceRan) + " miles");
        timeRanLocal.setText("Time: "+ timeElapsed);
        Double pace = player.totalDistanceRan/(minutesDouble/60);
        paceLocal.setText("Pace: " + df.format(pace) + "mph");
        //mapLocal.setBackgroundColor(activateColor);
        if(player.heartRate != null){
            int heartRate = (int) Math.round(player.heartRate);
            if(heartRate != 0) {
                localHeartRate.setText("BPM: " + heartRate + "bpm");
            }else{
                localHeartRate.setText("BPM: NA");
            }
        }
    }

    //Helper function that's sets the textViews for the nonLocal
    //player UI based off of passed Raceplayer
    public void setTextViewsOther(RacePlayer player){
        DecimalFormat df = new DecimalFormat("#,###.##");
        double minutesDouble = player.totalTimeRan/60000;
        int minutes = (int) minutesDouble;
        int seconds = (int) (player.totalTimeRan % 60000)/1000;
        String secondString = Integer.toString(seconds);
        if (secondString.length() == 1) {
            secondString = "0" + secondString;
        }
        String timeElapsed = minutes + ":" + secondString;
        distanceOther.setText("Distance: " + df.format(player.totalDistanceRan) + " miles");
        timeRanOther.setText("Time: "+ timeElapsed);
        Double pace = player.totalDistanceRan/(minutesDouble/60);
        paceOther.setText("Pace: " + df.format(pace) + "mph");
        //mapOther.setBackgroundColor(activateColor);
        if(player.heartRate != null){
            int heartRate = (int) Math.round(player.heartRate);
            if(heartRate != 0) {
                otherHeartRate.setText("BPM: " + heartRate + "bpm");
            }else{
                otherHeartRate.setText("BPM: NA");
            }
        }
    }

    //Used to draw map
    private void setMap(RacePlayer player, boolean isLocal){
        if(isLocal) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                localRaceLocations = populatePolyLists(player);
                reDrawPolyLines(localRaceLocations, true);
            }
        }else{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    otherRaceLocations = populatePolyLists(player);
                    reDrawPolyLines(otherRaceLocations, false);
                }
        }

    }

    //Helper function that creates polylines latlbg list
    @RequiresApi(api = Build.VERSION_CODES.N)
    private ArrayList<RaceLocation> populatePolyLists(RacePlayer p){
        try {
            ArrayList<RaceLocation> savedLocations = new ArrayList<RaceLocation>(p.playerLocation.values());
            savedLocations.sort(new Comparator<RaceLocation>() {
                @Override
                public int compare(RaceLocation l1, RaceLocation l2) {
                    return l1.compareTo(l2);
                }
            });
            return savedLocations;
        }catch(Exception e){

        }
        return new ArrayList<RaceLocation>();
    }

    //Helper function that draws the polylines on maps
    public void reDrawPolyLines(List<RaceLocation> savedLocations, Boolean isLocal){
        mapApi.clear();
        LatLng cur = new LatLng(0, 0);
        for(int i = 0; i < savedLocations.size() - 1; i+=1){

            // create polyline given two coordinates
            double prevLat = savedLocations.get(i).latLng.lat;
            double prevLng = savedLocations.get(i).latLng.lng;
            double curLat = savedLocations.get(i+1).latLng.lat;
            double curLng = savedLocations.get(i+1).latLng.lng;
            LatLng prev = new LatLng(prevLat, prevLng);
            cur = new LatLng(curLat, curLng);
            Polyline p = mapApi.addPolyline(new PolylineOptions()
                    .clickable(false)
                    .add(prev, cur));

            // if the start coordinate, place custom marker with start time and custom image
            if(i == 0){
                String time = getTime(savedLocations.get(i));
                mapApi.addMarker(new MarkerOptions().position(prev).title("Start time: "+time))
                        .setIcon(BitmapDescriptorFactory.fromBitmap(startIcon));
            }

            // if the last coordinate, place custom marker with end time and custom image
            if(i == savedLocations.size() - 2){
                String time = getTime(savedLocations.get(savedLocations.size()-1));
                mapApi.addMarker(new MarkerOptions().position(cur).title("Finish time: "+time))
                        .setIcon(BitmapDescriptorFactory.fromBitmap(finishIcon));
            }


            if(isLocal){
                localBounds.include(prev);
            }else{
                otherBounds.include(prev);
            }

            // calculate pace of two different coordinates using their timestamps
            double distance = distance(prevLat, prevLng, curLat, curLng);
            double t1 = savedLocations.get(i).time;
            double t2 = savedLocations.get(i+1).time;
            double timeBetween = Math.abs(t1 - t2);
            double timeBetweenHours = timeBetween / 60000 / 60.0;
            double curPace = distance/timeBetweenHours;

            // color the polyline depending on the pace value
            if(curPace < 5){
                p.setColor(colorSlowPace);
            }else if(curPace < 8){
                p.setColor(colorMediumPace);
            }else{
                p.setColor(colorFastPace);
            }
        }

        // update map camera dynamically to fit the whole race
        int padding = 75;
        if(isLocal){
            LatLngBounds b = localBounds.build();
            mapApi.animateCamera(CameraUpdateFactory.newLatLngBounds(b, padding));
            localBounds = new LatLngBounds.Builder();
        }else{
            LatLngBounds b = otherBounds.build();
            mapApi.animateCamera(CameraUpdateFactory.newLatLngBounds(b, padding));
            otherBounds = new LatLngBounds.Builder();

        }

    }

    // helper function to get the time of a race location. specifically used in the google map
    // route drawing with custom markers for start and end time of a race for a player
    public String getTime(RaceLocation l){
        Double d = l.time;
        ZonedDateTime dateTime;
        String formatted = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            dateTime = Instant.ofEpochMilli(d.longValue())
                    .atZone(ZoneId.of(TimeZone.getDefault().toZoneId().getId()));
            formatted = dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
            if(Integer.parseInt(formatted.substring(0,2)) == 12){
                formatted = formatted + " pm";
            }else if(Integer.parseInt(formatted.substring(0,2)) == 24){
                formatted = formatted + " am";
            } else if(Integer.parseInt(formatted.substring(0,2)) >= 13){
                formatted = Integer.parseInt(formatted.substring(0,2)) - 12 + formatted.substring(2, 5)+ " pm";
            }else{
                formatted = formatted + " am";
            }
        }
        return formatted;
    }

    //Calculates distance between points of data
    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;

        // filter out corrupt or insignificant distance calculations
        if(Double.isNaN(dist) || Double.isInfinite(dist) || dist <= .0001){
            return 0.0;
        }

        return (dist);
    }

    // distance calculation helper functions
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    //The next 4 functions are used to interact
    //with the fitbit api library

    //This gets the profile and retrieves the data
    @NonNull
    @Override
    public Loader<ResourceLoaderResult<HeartRateContainer>> onCreateLoader(int id, @Nullable Bundle args) {
        if (isPlayer1)
            return HeartRateService.getHeartRateSummaryLoader(ResultActivity.this,
                    game.player1.playerStartTime,
                    game.player1.playerStartTime + Double.valueOf(game.player1.totalTimeRan/1000 + 60).longValue());
        else
            return HeartRateService.getHeartRateSummaryLoader(ResultActivity.this,
                    game.player2.playerStartTime,
                    game.player2.playerStartTime + Double.valueOf(game.player2.totalTimeRan/1000 + 60).longValue());
    }

    //Once all the data is retrieved, if the data is successful then call bindProfilesInfo, display to ui
    @Override
    public void onLoadFinished(Loader<ResourceLoaderResult<HeartRateContainer>> loader, ResourceLoaderResult<HeartRateContainer> data) {
        if (data.isSuccessful()) {
            if(data.getResult().getActivitiesHeartIntraday().getDataset().size() > 0) {
                bindHeartbeatInfo(data.getResult().getActivitiesHeartIntraday().getDataset());
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<ResourceLoaderResult<HeartRateContainer>> loader) {

    }

    //Uses info obtained from fitBit and sets the appropriate
    //Views to display them
    public void bindHeartbeatInfo(List<HeartRateData> dataset) {

        // calculate average heart rate given per second heart rate data
        double sumRate = 0;

        for (HeartRateData data : dataset) {
            sumRate += data.getValue();
        }

        double averageHeartRate = sumRate / dataset.size();

        // write heart rate data to db so other player can view it in their UI
        if(isPlayer1){
            game.player1.heartRate = averageHeartRate;
            game.writeToDatabase("player1", "heartRate");
            setTextViews();
        }else{
            game.player2.heartRate = averageHeartRate;
            game.writeToDatabase("player2", "heartRate");
            setTextViews();
        }
    }
}