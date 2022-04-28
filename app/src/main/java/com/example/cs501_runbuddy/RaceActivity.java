package com.example.cs501_runbuddy;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.cs501_runbuddy.models.Game;
import com.example.cs501_runbuddy.models.LatLngDB;
import com.example.cs501_runbuddy.models.RaceLocation;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;



public class RaceActivity extends FragmentActivity implements SpotifyFragment.spotifyInterface, OnMapReadyCallback {

    private LocationRequest locationRequest;

    //This is the gps sensor
    private FusedLocationProviderClient fusedLocationProviderClient;

    private LocationCallback locationCallBack;


    private final int DEFAULT_UPDATE_INTERVAL = 10;
    private final int FASTEST_UPDATE_INTERVAL = 5;
    private final int PERMISSIONS_FINE_LOCATION = 99;

    private GoogleMap mapAPI;
    private SupportMapFragment mapFragment;
    private LatLng currentLocation;
    private ArrayList<LatLng> savedLocations = new ArrayList<LatLng>();
    private ArrayList<Integer> savedPolyColors = new ArrayList<>();
    private Game game;

    private Polyline poly;
    private TextView tv_pace;
    private TextView tv_distance;
    private TextView tv_time;
    private TextView tv_weight;
    private Button spotifyButton;
    private boolean isSpotifyOnScreen;
    private Button mapButton;
    private CircularSeekBar localPlayerTrack;
    private CircularSeekBar otherPlayerTrack;
    private Button quitButton;

    private double currentPace;

    private TextView localColorIndicator;
    private TextView onlineColorIndicator;
    private String localPlayerName;
    private String onlinePlayerName;

    private Double previousTimeOther;


    private TextView gap;

    private SpotifyFragment spotifyApp;

    // Variable necessary for calculating running data
    private Instant startTime;
    private double totalDistance;
    private double totalTimeRan;
    private double totalTimeRan2;
    private double prevMilis;

    private RaceLocation currentLocationOtherPlayer;

    private double totalDistanceOtherPlayer;


    // Used to indicate if timer is on or off
    private boolean timerOn = true;
    private Boolean timerOn2 = true;

    private int maxDistance; //divide by 100 to get distance in miles

    private DatabaseReference otherPlayerRef;
    private ChildEventListener otherPlayerListener;

    private DatabaseReference otherPlayerFinishedRef;
    private ValueEventListener otherPlayerFinishedListener;

    private int localColor;
    private int onlineColor;

    private boolean isPlayer1;

    private double otherPlayerStartTime;

    private double otherPlayerThreadTime;

    private double raceStartTime;

    private int otherPlayerLocationIndex;

    private ArrayList<RaceLocation> otherRaceLocations;

    private int colorSlowPace = Color.RED;
    private int colorMediumPace = Color.YELLOW;
    private int colorFastPace = Color.GREEN;

    private TextView tv_otherPlayerDistance;
    private TextView tv_otherPlayerTimer;
    private TextView tv_otherPlayerPace;
    private TextView tvOtherStatus;
    private LatLngDB threadStopper;

    private double startTimeOther;



    //1 used to set up the UI elements and overall logic of the google map
    //And spotify
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_race);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        tv_pace = findViewById(R.id.tv_pace);
        tv_otherPlayerDistance = (TextView) findViewById(R.id.tv_distanceOther);
        tv_otherPlayerPace = (TextView) findViewById(R.id.tv_paceOther);
        tv_otherPlayerTimer = (TextView) findViewById(R.id.tv_timeOther);
        tvOtherStatus = (TextView) findViewById(R.id.tvOtherStatus);
        tv_distance = findViewById(R.id.tv_distance);
        threadStopper = new LatLngDB(10.0, 10.0);
        tv_time = findViewById(R.id.tv_time);
        spotifyButton = (Button) findViewById(R.id.spotify);
        spotifyButton.setBackgroundColor(Color.LTGRAY);
        mapButton = (Button) findViewById(R.id.googleMapsButton);
        mapButton.setBackgroundColor(Color.LTGRAY);
        localPlayerTrack = (CircularSeekBar) findViewById(R.id.localPlayerTrack);
        otherPlayerTrack = (CircularSeekBar) findViewById(R.id.otherPlayerTrack);
        quitButton = (Button) findViewById(R.id.quitButton);
        gap = (TextView) findViewById(R.id.distancebetween);
        gap.setTextColor(Color.WHITE);
        localColorIndicator = (TextView) findViewById(R.id.localColorIndicator);
        onlineColorIndicator = (TextView) findViewById(R.id.onlineColorIndicator);
        otherRaceLocations = new ArrayList<RaceLocation>();
        otherPlayerLocationIndex = 0;
        isSpotifyOnScreen = false;
        totalDistance = 0;
        totalDistanceOtherPlayer = 0;
        previousTimeOther = 0.0;

        // To retrieve object in second Activity
        game = (Game) getIntent().getSerializableExtra("game");
        isPlayer1 = (game.player1.playerId.equals(GoogleSignIn.getLastSignedInAccount(this).getId()));
        if(isPlayer1){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                game.player1.playerStartTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
                game.writeToDatabase("player1", "playerStartTime");
            }
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                game.player2.playerStartTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
                game.writeToDatabase("player2", "playerStartTime");
            }
        }

        localColor = (Integer) getIntent().getExtras().get("localPlayerColor");
        onlineColor = (Integer) getIntent().getExtras().get("onlinePlayerColor");
        maxDistance = (int) (game.totalDistance * 100);

        localColorIndicator.setTextColor(localColor);

        onlineColorIndicator.setTextColor(onlineColor);


        spotifyApp = new SpotifyFragment();


        //Spawning the spotify fragment
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                .replace(R.id.spotifyUi, spotifyApp)
                .commit();
        //Hiding the spotify fragment
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                .hide(spotifyApp)
                .commit();




        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startTime = Instant.now();
        }


        totalDistance = 0.0;

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapAPI);
        mapFragment.getMapAsync(this);
        mapFragment.getView().setVisibility(View.INVISIBLE);


        //This defines how often and precise we will request
        //data from the gps sensor
        locationRequest = LocationRequest.create()
                .setInterval(1000 * DEFAULT_UPDATE_INTERVAL)
                .setFastestInterval(1000 * FASTEST_UPDATE_INTERVAL)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(1000 * DEFAULT_UPDATE_INTERVAL);

        //This is the callback function that is called everytime
        //the gps sensor returns new gps data
        locationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                updateUIWithLocation(locationResult.getLastLocation());
            }
        };



        //Used to display/hide spotify fragment
        spotifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    if(mapFragment.getView().getVisibility() == View.VISIBLE){
                        mapFragment.getView().setVisibility(View.INVISIBLE);
                        mapButton.setBackgroundColor(Color.LTGRAY);
                    }
                    if (!spotifyApp.isVisible()) {
                        getSupportFragmentManager().beginTransaction()
                                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                                .show(spotifyApp)
                                .commit();
                        spotifyButton.setBackgroundColor(Color.GREEN);
                        gap.setVisibility(View.INVISIBLE);
                        localPlayerTrack.setVisibility(View.INVISIBLE);
                        otherPlayerTrack.setVisibility(View.INVISIBLE);
                        localColorIndicator.setVisibility(View.INVISIBLE);
                        onlineColorIndicator.setVisibility(View.INVISIBLE);

                    }else{
                        getSupportFragmentManager().beginTransaction()
                                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                                .hide(spotifyApp)
                                .commit();
                        spotifyButton.setBackgroundColor(Color.LTGRAY);
                        gap.setVisibility(View.VISIBLE);
                        localPlayerTrack.setVisibility(View.VISIBLE);
                        otherPlayerTrack.setVisibility(View.VISIBLE);
                        localColorIndicator.setVisibility(View.VISIBLE);
                        onlineColorIndicator.setVisibility(View.VISIBLE);
                    }
                }catch (Exception e){
                    Toast.makeText(RaceActivity.this, "Please make sure spotify is on in the background", Toast.LENGTH_SHORT).show();
                }

            }
        });

        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (spotifyApp.isVisible()) {
                        getSupportFragmentManager().beginTransaction()
                                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                                .hide(spotifyApp)
                                .commit();
                        spotifyButton.setBackgroundColor(Color.LTGRAY);
                    }
                    if (mapFragment.getView().getVisibility() == View.INVISIBLE) {
                        mapFragment.getView().setVisibility(View.VISIBLE);
                        mapAPI.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15.0f));
                        mapButton.setBackgroundColor(Color.GREEN);
                        gap.setVisibility(View.INVISIBLE);
                        localPlayerTrack.setVisibility(View.INVISIBLE);
                        otherPlayerTrack.setVisibility(View.INVISIBLE);
                        localColorIndicator.setVisibility(View.INVISIBLE);
                        onlineColorIndicator.setVisibility(View.INVISIBLE);
                    } else {
                        mapFragment.getView().setVisibility(View.INVISIBLE);
                        mapButton.setBackgroundColor(Color.LTGRAY);
                        gap.setVisibility(View.VISIBLE);
                        localPlayerTrack.setVisibility(View.VISIBLE);
                        otherPlayerTrack.setVisibility(View.VISIBLE);
                        localColorIndicator.setVisibility(View.VISIBLE);
                        onlineColorIndicator.setVisibility(View.VISIBLE);
                    }
                }catch(Exception e){
                    Toast.makeText(RaceActivity.this, "Your gps is faulty no map data", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Initalize player1Track
        localPlayerTrack.setClickable(false);
        otherPlayerTrack.setClickable(false);
        localPlayerTrack.setMainColor(localColor);
        otherPlayerTrack.setMainColor(onlineColor);
        otherPlayerTrack.setVisibility(View.INVISIBLE);
        makeTrack(localPlayerTrack, localColor);
        makeTrack(otherPlayerTrack, onlineColor);

        localPlayerTrack.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quitGame();
            }
        });
        if(isPlayer1){
            otherPlayerRef = RunBuddyApplication.getDatabase().getReference("games").child(game.ID).child("player2").child("playerLocation");
            otherPlayerFinishedRef = RunBuddyApplication.getDatabase().getReference("games").child(game.ID).child("player2").child("playerFinished");
        }else{
            otherPlayerRef = RunBuddyApplication.getDatabase().getReference("games").child(game.ID).child("player1").child("playerLocation");
            otherPlayerFinishedRef = RunBuddyApplication.getDatabase().getReference("games").child(game.ID).child("player1").child("playerFinished");
        }
        otherPlayerListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                RaceLocation r = snapshot.getValue(RaceLocation.class);
                otherRaceLocations.add(r);
            }


            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        otherPlayerFinishedListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.getValue(Boolean.class)) {
                        game.readOtherPlayerDoubleField(isPlayer1, "totalDistanceRan", new Game.OtherPlayerDoubleFieldCallback(){
                            @Override
                            public void onCallback(Double value) {
                                if (!value.equals(game.totalDistance)) {
                                    otherPlayerRef.removeEventListener(otherPlayerListener);
                                    if (isPlayer1)
                                        game.player2.playerFinished = true;
                                    else
                                        game.player1.playerFinished = true;
                                    if(!game.isAsync) {
                                        timerOn2 = false;
                                        Toast.makeText(RaceActivity.this, "Other player quit their race", Toast.LENGTH_SHORT).show();
                                        tvOtherStatus.setText("Status: Quit");
                                    }
                                }
                                //stops time for other player on screen
                                //threadStopper = null;
                                otherPlayerFinishedRef.removeEventListener(otherPlayerFinishedListener);
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        com.example.cs501_runbuddy.models.User.getUserNameFromID(game.player1.playerId, new com.example.cs501_runbuddy.models.User.MyCallback() {
            @Override
            public void onCallback(String value) {
                if(isPlayer1){
                    localColorIndicator.setText(value);
                }else{
                    onlineColorIndicator.setText(value);
                }
            }
        });

        com.example.cs501_runbuddy.models.User.getUserNameFromID(game.player2.playerId, new com.example.cs501_runbuddy.models.User.MyCallback() {
            @Override
            public void onCallback(String value) {
                if(isPlayer1){
                    onlineColorIndicator.setText(value);
                }else{
                    localColorIndicator.setText(value);
                }
            }
        });


        otherPlayerRef.addChildEventListener(otherPlayerListener);
        otherPlayerFinishedRef.addValueEventListener(otherPlayerFinishedListener);
        //Used to update movement data based of a specific interval
        updateGPS();
    }

    private void quitGame(){
        if(isPlayer1){
            game.player1.totalDistanceRan = totalDistance;
            game.writeToDatabase("player1", "totalDistanceRan");
            game.player1.totalTimeRan = totalTimeRan;
            game.writeToDatabase("player1", "totalTimeRan");
            game.player1.playerFinished = true;
            game.writeToDatabase("player1", "playerFinished");
        }else{
            game.player2.totalDistanceRan = totalDistance;
            game.writeToDatabase("player2", "totalDistanceRan");
            game.player2.totalTimeRan = totalTimeRan2;
            game.writeToDatabase("player2", "totalTimeRan");
            game.player2.playerFinished = true;
            game.writeToDatabase("player2", "playerFinished");
        }
        otherPlayerRef.removeEventListener(otherPlayerListener);
        stopLocationUpdates();
        Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
        intent.putExtra("game", game);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }




    public void makeTrack(CircularSeekBar circ, int color){
        circ.initPaints(color);
        circ.setProgress(0);
        //Sets entire track color
        circ.setCircleColor(Color.WHITE);
        //This is for inside circle so useless
        //circ.setCircleFillColor(Color.TRANSPARENT);
        //Set behind color
        circ.setCircleProgressColor(color);
        circ.setPointerHaloColor(color);
        circ.setPointerAlpha(0);
        circ.setPointerAlphaOnTouch(0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            circ.setOutlineAmbientShadowColor(Color.TRANSPARENT);
            circ.setOutlineSpotShadowColor(color);
            circ.setBackgroundColor(Color.TRANSPARENT);
        }
        circ.setMax(maxDistance);
        circ.setProgress(0);
        circ.setClickable(false);
    }


    //initializes the gps sensor manager
    private void updateGPS() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(
                RaceActivity.this);
        //If permission is granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(
                    RaceActivity.this);
            //Instantiate gps sensor manager
            //Get last known location if successfully instantiated
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this,
                    new OnSuccessListener<Location>() {
                        @SuppressLint("MissingPermission")
                        @Override
                        public void onSuccess(Location location) {
                            updateUIWithLocation(location);
                        }
                    });
        }
        //Request permission via helper function below
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_FINE_LOCATION);
            }
        }
    }

    //Requested for gps permissions
    //If permission is granted or has been granted carry on
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateGPS();
                } else {

                    finish();
                }
                break;
        }
    }




    //These are used in the location request switch to start and stop
    //Listening for gps updates
    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
        //Creates a thread to make the timer change every second
        //Hence why it was not included in the last function
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            raceStartTime = Instant.now().toEpochMilli();
        }
        updateTime();
        if (!game.isAsync) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                otherPlayerThreadTime= Instant.now().toEpochMilli();
            }
            Toast.makeText(this, "Other player started their race", Toast.LENGTH_SHORT).show();
            tvOtherStatus.setText("Status: Running");
            updateTimeOther();
        }
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
    }

    //Helper function for updateGPS()
    //Helps with updating the location, path drawn, and overall pace
    //Every time a new location is retrieved from the gps sensor
    private void updateUIWithLocation(Location location) {
        //if retrieved location is valid
        if(location!=null){
            //Set the current location variable
            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

            //add current location to saved location list
            //Which is a list that saves every point of the run
            savedLocations.add(currentLocation);

            updateOtherPlayerUI();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                double curTime = Instant.now().toEpochMilli();
                LatLngDB latLngDB = new LatLngDB(currentLocation.latitude, currentLocation.longitude);
                game.addLocData(isPlayer1, latLngDB, curTime);
            }

            //Clear all markers and polylines from google map
            mapAPI.clear();

            //Create polyline and draw on map
            /*
            poly = mapAPI.addPolyline(new PolylineOptions().add(savedLocations.get(0)));
            poly.setPoints(savedLocations);
            poly.setVisible(true);
            */


            double currentMilis = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                currentMilis = Instant.now().toEpochMilli();
            }

            if(savedLocations.size()>1) {
                double temp =  currentMilis - prevMilis;
                double hours = temp / 60000 / 60.0;
                LatLng secondToLast = savedLocations.get(savedLocations.size() - 2);
                currentPace = distance(currentLocation.latitude, currentLocation.longitude, secondToLast.latitude, secondToLast.longitude) / hours;
                paceColorAdder();
                reDrawPolyLines();
                prevMilis = currentMilis;
            }else{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    prevMilis = Instant.now().toEpochMilli();
                }
            }

            //Add marker for current location to map
            mapAPI.addMarker(new MarkerOptions().position(currentLocation).title("TestPoint"));

            //If there is a path to be drawn and distance to be calculated
            if(savedLocations.size()>1) {

                //Moves camera and updates distance used to move runner location
                mapAPI.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));

                //Calculate distance using 2nd to last location
                //and current location
                LatLng secondToLast = savedLocations.get(savedLocations.size() - 2);
                totalDistance += distance(currentLocation.latitude, currentLocation.longitude, secondToLast.latitude, secondToLast.longitude);
                if(totalDistance >= totalDistanceOtherPlayer){
                    playerAhead(otherPlayerTrack, localPlayerTrack);
                }else{
                    playerAhead(localPlayerTrack, otherPlayerTrack);
                }



                if(totalDistance >= maxDistance/100){
                    localPlayerTrack.setProgress((int) maxDistance);
                    if(isPlayer1){
                        game.player1.totalDistanceRan = game.totalDistance;
                        game.writeToDatabase("player1", "totalDistanceRan");
                        game.player1.totalTimeRan = totalTimeRan;
                        game.writeToDatabase("player1", "totalTimeRan");
                        game.player1.playerFinished = true;
                        game.writeToDatabase("player1", "playerFinished");
                        if (!game.player2.playerFinished)
                            otherPlayerRef.removeEventListener(otherPlayerListener);
                    }else{
                        game.player2.totalDistanceRan = game.totalDistance;
                        game.writeToDatabase("player2", "totalDistanceRan");
                        game.player2.totalTimeRan = totalTimeRan;
                        game.writeToDatabase("player2", "totalTimeRan");
                        game.player2.playerFinished = true;
                        game.writeToDatabase("player2", "playerFinished");
                        if (!game.player1.playerFinished)
                            otherPlayerRef.removeEventListener(otherPlayerListener);
                    }
                    Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
                    intent.putExtra("game", game);
                    startActivity(intent);
                    totalDistance = 0;
                    stopLocationUpdates();
                }else {
                    double between = Math.abs(totalDistance - totalDistanceOtherPlayer);
                    String milesGap = new DecimalFormat("#.##").format(between);
                    String metersGap = new DecimalFormat("#.##").format(between*1609.34);
                    if(totalDistance >= totalDistanceOtherPlayer){
                        if(Math.abs(between) < 0.5){
                            gap.setText(metersGap + " meters ahead");
                        }else{
                            gap.setText(milesGap + " miles ahead");
                        }

                    }else{
                        if(Math.abs(between) < 0.5){
                            gap.setText(metersGap + " meters behind");
                        }else{
                            gap.setText(milesGap + " miles behind");
                        }

                    }
                    localPlayerTrack.setProgress((int) (totalDistance * 100));
                }

                DecimalFormat df = new DecimalFormat("0.00");
                tv_distance.setText("Distance: " + df.format(totalDistance) + " mi");

                //Pace calculation
                double temp = 0;
                double hours = 0;
                double startTimeMilis = 0;
                double endTimeMilis = 0;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startTimeMilis = startTime.toEpochMilli();
                    endTimeMilis = Instant.now().toEpochMilli();
                    temp = endTimeMilis - startTimeMilis;
                    int seconds = (int) (temp % 60000) / 1000;
                    String secondString = Integer.toString(seconds);
                    //Used when seconds are single digits
                    //So we don't get a time reading like 2:9 instead of 2:09
                    if (secondString.length() == 1) {
                        secondString = "0" + secondString;
                    }

                    hours = temp/60000/60.0;
                    double pace = totalDistance/hours;
                    tv_pace.setText("Pace: " + df.format(currentPace) + " mph");
                }

            }
            //This happens when the page first launches and
            //there is only one save location i.e. the current location
            else{
                mapAPI.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15.0f));
            }
        }
    }

    public void paceColorAdder(){
        if(currentPace < 5){
            savedPolyColors.add(colorSlowPace);
        }else if(currentPace < 8){
            savedPolyColors.add(colorMediumPace);
        }else{
            savedPolyColors.add(colorFastPace);
        }
    }

    public void updateOtherPlayerUI(){
        double localElapsedTime = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            localElapsedTime = Instant.now().toEpochMilli() - raceStartTime;

        }
        if (otherRaceLocations.size()>0){
            if (currentLocationOtherPlayer == null) {
                currentLocationOtherPlayer = otherRaceLocations.get(otherPlayerLocationIndex);
                otherPlayerStartTime = otherRaceLocations.get(otherPlayerLocationIndex).time;
                otherPlayerLocationIndex++;
                if (game.isAsync) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        otherPlayerThreadTime = Instant.now().toEpochMilli();
                    }
                    updateTimeOther();
                    Toast.makeText(this, "Other player started their race", Toast.LENGTH_SHORT).show();
                    tvOtherStatus.setText("Status: Running");
                    otherPlayerTrack.setVisibility(View.VISIBLE);
                }
            }
            else if (otherRaceLocations.size() > otherPlayerLocationIndex){
                currentLocationOtherPlayer = otherRaceLocations.get(otherPlayerLocationIndex);
                RaceLocation secondToLast = otherRaceLocations.get(otherPlayerLocationIndex - 1);
                while (currentLocationOtherPlayer.time - otherPlayerStartTime < localElapsedTime){
//                    double curTimeOther = 0;
//                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//                        curTimeOther= Instant.now().toEpochMilli();
//                    }
                    DecimalFormat df = new DecimalFormat("#,###.##");
                    double hours = (currentLocationOtherPlayer.time - secondToLast.time)/60000/60;
//                    double hours = (curTimeOther - previousTimeOther)/60000/60;
//                    previousTimeOther = curTimeOther;
                    double previousDistance = totalDistanceOtherPlayer;
                    totalDistanceOtherPlayer += distance(currentLocationOtherPlayer.latLng.lat, currentLocationOtherPlayer.latLng.lng, secondToLast.latLng.lat, secondToLast.latLng.lng);
                    Double paceOtherPlayer = (totalDistanceOtherPlayer - previousDistance)/hours;
                    tv_otherPlayerPace.setText("Pace : " + df.format(paceOtherPlayer)+ "mph");
                    if (totalDistanceOtherPlayer < maxDistance / 100) {
                        if (totalDistanceOtherPlayer > totalDistance) {
                            playerAhead(localPlayerTrack, otherPlayerTrack);
                        } else {
                            playerAhead(otherPlayerTrack, localPlayerTrack);
                        }
                        double d = totalDistanceOtherPlayer;

                        tv_otherPlayerDistance.setText("Distance: " + df.format(d) + " mi");
                        otherPlayerTrack.setVisibility(View.VISIBLE);
                        otherPlayerTrack.setProgress((int) (totalDistanceOtherPlayer * 100));

                        if (otherRaceLocations.size() - 1 == otherPlayerLocationIndex) {
                            if(isPlayer1){
                                if (game.player2.playerFinished) {
                                    timerOn2 = false;
                                    Toast.makeText(RaceActivity.this, "Other player quit their race", Toast.LENGTH_SHORT).show();
                                    tvOtherStatus.setText("Status: Quit");
                                }
                            }else{
                                if (game.player1.playerFinished) {
                                    timerOn2 = false;
                                    Toast.makeText(RaceActivity.this, "Other player quit their race", Toast.LENGTH_SHORT).show();
                                    tvOtherStatus.setText("Status: Quit");
                                }
                            }
                        }
                    }else {
                        otherPlayerTrack.setProgress((int) (maxDistance));
                        timerOn2 = false;
                        otherPlayerRef.removeEventListener(otherPlayerListener);
                        if (isPlayer1)
                            game.player2.playerFinished = true;
                        else
                            game.player1.playerFinished = true;
                        Toast.makeText(RaceActivity.this, "Other player finished their race", Toast.LENGTH_SHORT).show();
                        tvOtherStatus.setText("Status: Finished");
                    }
                    otherPlayerLocationIndex++;
                    if(otherRaceLocations.size() > otherPlayerLocationIndex){
                        currentLocationOtherPlayer = otherRaceLocations.get(otherPlayerLocationIndex);
                        secondToLast = otherRaceLocations.get(otherPlayerLocationIndex - 1);
                    }else{
                        break;
                    }
                }
            }
        }
    }

    //Instantiate google map
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapAPI = googleMap;

    }
    //Used to update the time every second
    public void updateTime(){
        Thread t = new Thread(() -> {
            while(timerOn){
                if(game.totalDistance * 15 < totalTimeRan/60000){
                    quitGame();
                }
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    double startTimeMilis = startTime.toEpochMilli();
                    double endTimeMilis = Instant.now().toEpochMilli();
                    totalTimeRan = endTimeMilis - startTimeMilis;
                    int minutes = (int) totalTimeRan / 60000;
                    int seconds = (int) (totalTimeRan % 60000) / 1000;
                    String secondString = Integer.toString(seconds);
                    if (secondString.length() == 1) {
                        secondString = "0" + secondString;
                    }
                    String timeElapsed = minutes + ":" + secondString;

                    //tv_time.setText("Time: " + timeElapsed);
                    setTime(timeElapsed);
                }
            }
        });
        t.start();
    }

    public void updateTimeOther(){
        Thread t = new Thread(() -> {
            while(timerOn2){
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                /*
                if(isPlayer1){
                    if(game.player2.playerFinished){
                        return;
                    }
                }else{
                    if(game.player1.playerFinished){
                        return;
                    }
                }
                 */

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    double startTimeMilis = otherPlayerThreadTime;
                    double endTimeMilis = Instant.now().toEpochMilli();
                    totalTimeRan2 = endTimeMilis - startTimeMilis;
                    int minutes = (int) totalTimeRan2 / 60000;
                    int seconds = (int) (totalTimeRan2 % 60000) / 1000;
                    String secondString = Integer.toString(seconds);
                    if (secondString.length() == 1) {
                        secondString = "0" + secondString;
                    }
                    String timeElapsed = minutes + ":" + secondString;
                    //tv_time.setText("Time: " + timeElapsed);
                    setTimeOther(timeElapsed);
                }
            }
        });
        t.start();
    }

    //Used to set UI for updateTime function
    public void setTime(String time){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_time.setText("Time: " + time);
            }
        });
    }

    public void setTimeOther(String time){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_otherPlayerTimer.setText("Time: " + time);
            }
        });
    }






    //given two coordinates, calculate the distance in miles
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
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }



    //Used to make toast to indicate
    //Spotify isn't connected
    @Override
    public void spotifyNotOpen() {
        Toast.makeText(RaceActivity.this, "You didn't connect", Toast.LENGTH_SHORT).show();
    }


    public void playerAhead(CircularSeekBar behind, CircularSeekBar ahead){
        behind.bringToFront();
        behind.setCircleColor(Color.TRANSPARENT);
        //Track Black
        ahead.setCircleColor(Color.WHITE);
        ahead.setCircleProgressColor(ahead.mainColor);
        behind.setClickable(false);
        behind.setIsTouchEnabled(false);
        ahead.setClickable(false);
        mapFragment.getView().bringToFront();
        spotifyApp.getView().bringToFront();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed(); // do not call super during a race
    }

    public void reDrawPolyLines(){
        int colorCounter = 0;
        for(int i = 0; i < savedLocations.size() - 1; i+=1){
            Polyline p = mapAPI.addPolyline(new PolylineOptions()
                    .clickable(true)
                    .add(savedLocations.get(i), savedLocations.get(i+1)));
            p.setColor(savedPolyColors.get(colorCounter));
            p.setVisible(true);
            colorCounter++;
        }

    }
}
