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
import com.example.cs501_runbuddy.models.User;
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

    // game wide variables
    private Game game; // game object
    private int maxDistance; // distance to be run in units of a progress bar (miles * 100)
    private TextView raceTypeIndicator; // text view for distance of game
    private TextView gap; // distance between two players
    private boolean isPlayer1; // variable to determine if local player is player 1 or 2

    // gps sensor and associated request and callback
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallBack;

    // gps request params and permission request code
    private final int DEFAULT_UPDATE_INTERVAL = 5;
    private final int FASTEST_UPDATE_INTERVAL = 3;
    private final int PERMISSIONS_FINE_LOCATION = 99;

    // polyline colors for google map drawing
    private int colorSlowPace = Color.RED;
    private int colorMediumPace = Color.YELLOW;
    private int colorFastPace = Color.GREEN;

    // google map and spotify fragment related variables
    private GoogleMap mapAPI;
    private ArrayList<Integer> savedPolyColors = new ArrayList<>();
    private SupportMapFragment mapFragment;
    private SpotifyFragment spotifyApp;
    private Button spotifyButton;
    private Button mapButton;
    private Button quitButton;

    // local player location variables
    private LatLng currentLocation;
    private ArrayList<LatLng> savedLocations = new ArrayList<LatLng>();
    private double totalDistance;
    private double currentPace;

    // local player text views
    private TextView tvPace;
    private TextView tvDistance;
    private TextView tvTime;

    // local player track variables
    private CircularSeekBar localPlayerTrack;
    private TextView localColorIndicator;
    private int localColor;

    // local player timing variables
    private Instant startTime;
    private double raceStartTime;
    private double totalTimeRan;
    private Boolean timerOn = true; // Used to indicate if timer is on or off
    private double prevMillis;

    // other player text views
    private TextView tvOtherPlayerDistance;
    private TextView tvOtherPlayerTimer;
    private TextView tvOtherPlayerPace;
    private TextView tvOtherStatus;

    // other player track variables
    private CircularSeekBar otherPlayerTrack;
    private TextView onlineColorIndicator;
    private int onlineColor;

    // other player location variables
    private ArrayList<RaceLocation> otherRaceLocations;
    private RaceLocation currentLocationOtherPlayer;
    private int otherPlayerLocationIndex;
    private double totalDistanceOtherPlayer;

    // other player timing variables
    private double otherPlayerStartTime;
    private double otherPlayerThreadTime;
    private Boolean timerOn2 = true;
    private double totalTimeRan2;

    // other player db ref and listeners
    private DatabaseReference otherPlayerRef;
    private ChildEventListener otherPlayerListener;
    private DatabaseReference otherPlayerFinishedRef;
    private ValueEventListener otherPlayerFinishedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_race);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // initialize text view objects
        tvPace = findViewById(R.id.tv_pace);
        tvOtherPlayerDistance = (TextView) findViewById(R.id.tv_distanceOther);
        tvOtherPlayerPace = (TextView) findViewById(R.id.tv_paceOther);
        tvOtherPlayerTimer = (TextView) findViewById(R.id.tv_timeOther);
        tvOtherStatus = (TextView) findViewById(R.id.tvOtherStatus);
        tvDistance = findViewById(R.id.tv_distance);
        tvTime = findViewById(R.id.tv_time);
        spotifyButton = (Button) findViewById(R.id.spotify);
        spotifyButton.setBackgroundColor(Color.LTGRAY);
        mapButton = (Button) findViewById(R.id.googleMapsButton);
        mapButton.setBackgroundColor(Color.LTGRAY);
        localPlayerTrack = (CircularSeekBar) findViewById(R.id.localPlayerTrack);
        otherPlayerTrack = (CircularSeekBar) findViewById(R.id.otherPlayerTrack);
        quitButton = (Button) findViewById(R.id.quitButton);
        gap = (TextView) findViewById(R.id.distancebetween);
        raceTypeIndicator = (TextView) findViewById(R.id.raceDistanceText);
        localColorIndicator = (TextView) findViewById(R.id.localColorIndicator);
        onlineColorIndicator = (TextView) findViewById(R.id.onlineColorIndicator);
        otherRaceLocations = new ArrayList<RaceLocation>();
        otherPlayerLocationIndex = 0;
        totalDistance = 0;
        totalDistanceOtherPlayer = 0;

        // To retrieve game object from lobby fragment
        game = (Game) getIntent().getSerializableExtra("game");

        // set initial values for game wide text views and variables
        gap.setTextColor(Color.WHITE);
        DecimalFormat df = new DecimalFormat("####");
        raceTypeIndicator.setText(df.format(game.totalDistance)+" Mile Race");

        // check if local player is player 1 or 2
        isPlayer1 = (game.player1.playerId.equals(GoogleSignIn.getLastSignedInAccount(this).getId()));

        // if player 1, initialize the game's player 1 start time and write to database
        if(isPlayer1){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                game.player1.playerStartTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
                game.writeToDatabase("player1", "playerStartTime");
            }
        }
        // else player 2, initialize the game's player 2 start time and write to database
        else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                game.player2.playerStartTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
                game.writeToDatabase("player2", "playerStartTime");
            }
        }

        // get track colors to display from previous activity's intent
        localColor = (Integer) getIntent().getExtras().get("localPlayerColor");
        onlineColor = (Integer) getIntent().getExtras().get("onlinePlayerColor");

        // get the max distance value used in progress bars from the game object
        maxDistance = (int) (game.totalDistance * 100);

        // set color of player indicators
        localColorIndicator.setTextColor(localColor);
        onlineColorIndicator.setTextColor(onlineColor);

        // initialize spotify fragment
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

        //Initialize the map fragment and set it to invisible
        //As it should not be on screen on start
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

        //Sets the spotify on/off UI button
        //Used to display/hide spotify fragment
        //Used to display/hide everything surrounding the spotify fragment area
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
                        raceTypeIndicator.setVisibility(View.INVISIBLE);

                    }else{
                        getSupportFragmentManager().beginTransaction()
                                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                                .hide(spotifyApp)
                                .commit();
                        spotifyButton.setBackgroundColor(Color.LTGRAY);
                        gap.setVisibility(View.VISIBLE);
                        localPlayerTrack.setVisibility(View.VISIBLE);
                        otherPlayerTrack.setVisibility(View.VISIBLE);
                        raceTypeIndicator.setVisibility(View.VISIBLE);

                    }
                }catch (Exception e){
                    Toast.makeText(RaceActivity.this, "Please make sure spotify is on in the background", Toast.LENGTH_SHORT).show();
                }

            }
        });


        //Works like the spotify button
        //except this is for the map fragment
        //Shows/hides mapfragment
        //Show/hides components near mapfragment
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
                        raceTypeIndicator.setVisibility(View.INVISIBLE);
                    } else {
                        mapFragment.getView().setVisibility(View.INVISIBLE);
                        mapButton.setBackgroundColor(Color.LTGRAY);
                        gap.setVisibility(View.VISIBLE);
                        localPlayerTrack.setVisibility(View.VISIBLE);
                        otherPlayerTrack.setVisibility(View.VISIBLE);
                        raceTypeIndicator.setVisibility(View.VISIBLE);
                    }
                }catch(Exception e){
                    Toast.makeText(RaceActivity.this, "Your gps is faulty no map data", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // initialize the local player's location and start time for their timer
        totalDistance = 0.0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startTime = Instant.now();
        }

        // initialize both player tracks
        localPlayerTrack.setClickable(false);
        otherPlayerTrack.setClickable(false);
        localPlayerTrack.setMainColor(localColor);
        otherPlayerTrack.setMainColor(onlineColor);
        otherPlayerTrack.setVisibility(View.INVISIBLE);
        makeTrack(localPlayerTrack, localColor);
        makeTrack(otherPlayerTrack, onlineColor);

        // add function call to quit button click listener
        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quitGame();
            }
        });

        // initialize other player references depending on if local player is player 1 or 2
        if(isPlayer1){
            otherPlayerRef = RunBuddyApplication.getDatabase().getReference("games").child(game.ID).child("player2").child("playerLocation");
            otherPlayerFinishedRef = RunBuddyApplication.getDatabase().getReference("games").child(game.ID).child("player2").child("playerFinished");
        }else{
            otherPlayerRef = RunBuddyApplication.getDatabase().getReference("games").child(game.ID).child("player1").child("playerLocation");
            otherPlayerFinishedRef = RunBuddyApplication.getDatabase().getReference("games").child(game.ID).child("player1").child("playerFinished");
        }

        // initialize listener for listening to other player's location data
        otherPlayerListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // when new location datapoint is retrieved, add it to the other race location array
                RaceLocation r = snapshot.getValue(RaceLocation.class);
                otherRaceLocations.add(r);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) { }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        };

        // initialize other player finished listener
        otherPlayerFinishedListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // if the other player finished
                    if (snapshot.getValue(Boolean.class)) {
                        // read their distance ran value from the database
                        game.readOtherPlayerDoubleField(isPlayer1, "totalDistanceRan", new Game.OtherPlayerDoubleFieldCallback(){
                            @Override
                            public void onCallback(Double value) {
                                // if the player did not run the full distance
                                if (!value.equals(game.totalDistance)) {
                                    // remove the listener for location data
                                    otherPlayerRef.removeEventListener(otherPlayerListener);
                                    // mark them as finished on the local game object
                                    if (isPlayer1)
                                        game.player2.playerFinished = true;
                                    else
                                        game.player1.playerFinished = true;
                                    // if the game is synchronous, immediately toast to loccal player
                                    // that the other player quit and update status
                                    if(!game.isAsync) {
                                        timerOn2 = false;
                                        Toast.makeText(RaceActivity.this, "Other player quit their race", Toast.LENGTH_SHORT).show();
                                        tvOtherStatus.setText("Status: Quit");
                                    }
                                }
                                //stops time for other player on screen
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

        // get first name for player 1 from the db and then display it
        User.getUserNameFromID(game.player1.playerId, new com.example.cs501_runbuddy.models.User.MyCallback() {
            @Override
            public void onCallback(String value) {
                if(isPlayer1){
                    localColorIndicator.setText(value);
                }else{
                    onlineColorIndicator.setText(value);
                }
            }
        });

        // get first name for player 2 from the db and then display it
        User.getUserNameFromID(game.player2.playerId, new com.example.cs501_runbuddy.models.User.MyCallback() {
            @Override
            public void onCallback(String value) {
                if(isPlayer1){
                    onlineColorIndicator.setText(value);
                }else{
                    localColorIndicator.setText(value);
                }
            }
        });

        // add the event listeners for the other player
        otherPlayerRef.addChildEventListener(otherPlayerListener);
        otherPlayerFinishedRef.addValueEventListener(otherPlayerFinishedListener);

        // initialize fused location provider and start listening for gps data
        updateGPS();
    }

    private void quitGame(){
        // check if local player is player 1 or 2, then update database with current values
        // and mark them as finished
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
            game.player2.totalTimeRan = totalTimeRan;
            game.writeToDatabase("player2", "totalTimeRan");
            game.player2.playerFinished = true;
            game.writeToDatabase("player2", "playerFinished");
        }

        // remove the other player listener
        otherPlayerRef.removeEventListener(otherPlayerListener);

        // stop listening for gps data
        stopLocationUpdates();

        // turn timer off
        timerOn = false;

        // go to result activity
        Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
        intent.putExtra("game", game);
        startActivity(intent);
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

    // start listening for gps updates
    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        // initialize gps sensor
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);

        //Creates a thread to make the timer change every second
        //Hence why it was not included in the last function
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            raceStartTime = Instant.now().toEpochMilli();
        }
        updateTime();

        // if game is synchronous, start the other player UI (timer and start toast) to give the
        // illusion they are running at the exact same second
        if (!game.isAsync) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                otherPlayerThreadTime= Instant.now().toEpochMilli();
            }
            Toast.makeText(this, "Other player started their race", Toast.LENGTH_SHORT).show();
            tvOtherStatus.setText("Status: Running");
            updateTimeOther();
        }
    }

    // stop listening for gps data
    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
    }

    //Instantiate google map
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapAPI = googleMap;
    }

    //Helper function that draws polylines on map
    //Need to make each 2 points a seprate polyline
    //In order to show speed colors
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

    //This initalizes the player track seen on the top
    //of the screen when running the app
    public void makeTrack(CircularSeekBar circ, int color){
        circ.initPaints(color);
        circ.setProgress(0);
        //Sets entire track color
        circ.setCircleColor(Color.WHITE);
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

    //Used to properly set the tracks UI
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

    //Helper function to fill an arraylist
    //that stores the pace color for each poly object
    public void paceColorAdder(){
        if(currentPace < 5){
            savedPolyColors.add(colorSlowPace);
        }else if(currentPace < 8){
            savedPolyColors.add(colorMediumPace);
        }else{
            savedPolyColors.add(colorFastPace);
        }
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

        // make sure corrupt or insignificant distance calculations are ignored
        if(Double.isNaN(dist) || Double.isInfinite(dist) || dist <= .0001){
            return 0.0;
        }

        return (dist);
    }

    // distance helper functions
    private double deg2rad(double deg) { return (deg * Math.PI / 180.0); }

    private double rad2deg(double rad) { return (rad * 180.0 / Math.PI); }

    //Helper function for updateGPS()
    //Helps with updating the location, path drawn, and overall pace for the local player
    //Every time a new location is retrieved from the gps sensor
    private void updateUIWithLocation(Location location) {
        //if retrieved location is valid
        if(location!=null){
            //Set the current location variable
            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

            //add current location to saved location list
            //Which is a list that saves every point of the run
            savedLocations.add(currentLocation);

            // update other player UI to be in sync with their gps data we have retrieved so far
            updateOtherPlayerUI();

            // add location data to the game object with lat lng and timestamp
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                double curTime = Instant.now().toEpochMilli();
                LatLngDB latLngDB = new LatLngDB(currentLocation.latitude, currentLocation.longitude);
                game.addLocData(isPlayer1, latLngDB, curTime);
            }

            //Clear all markers and poly lines from google map
            mapAPI.clear();

            // gets the current time in milliseconds
            double currentMilis = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                currentMilis = Instant.now().toEpochMilli();
            }

            // if local location list has multiple points
            if(savedLocations.size()>1) {
                // calculate current pace
                double temp =  currentMilis - prevMillis;
                double hours = temp / 60000 / 60.0;
                LatLng secondToLast = savedLocations.get(savedLocations.size() - 2);
                currentPace = distance(currentLocation.latitude, currentLocation.longitude, secondToLast.latitude, secondToLast.longitude) / hours;

                // update google map drawing with new poly line that is color coded acccording to pacce
                paceColorAdder();
                reDrawPolyLines();

                prevMillis = currentMilis;
            }else{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    prevMillis = Instant.now().toEpochMilli();
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

                // if the local player has finished the race
                if(totalDistance >= maxDistance/100){
                    // set their progress to 100%
                    localPlayerTrack.setProgress((int) maxDistance);
                    // write to database final race data depending on if player 1 or 2
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

                    // set timer off for local player
                    timerOn = false;

                    // turn off listening for gps data
                    stopLocationUpdates();

                    // go to the result activity
                    Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
                    intent.putExtra("game", game);
                    startActivity(intent);
                }else {
                    // get distance between players and format it as a string
                    double between = Math.abs(totalDistance - totalDistanceOtherPlayer);
                    String milesGap = new DecimalFormat("#.##").format(between);
                    String metersGap = new DecimalFormat("#.##").format(between*1609.34);

                    // if local player ahead, display correct distance ahead
                    if(totalDistance >= totalDistanceOtherPlayer){
                        if(Math.abs(between) < 0.5){
                            gap.setText(metersGap + " meters ahead");
                        }else{
                            gap.setText(milesGap + " miles ahead");
                        }

                    }
                    // else local player is behind, display correct distance behind
                    else{
                        if(Math.abs(between) < 0.5){
                            gap.setText(metersGap + " meters behind");
                        }else{
                            gap.setText(milesGap + " miles behind");
                        }
                    }
                    // set local player progress to up to date value
                    localPlayerTrack.setProgress((int) (totalDistance * 100));
                }

                // display current distance and pace to UI
                DecimalFormat df = new DecimalFormat("0.00");
                tvDistance.setText("Distance: " + df.format(totalDistance) + " mi");
                tvPace.setText("Pace: " + df.format(currentPace) + " mph");

            }
            //This happens when the page first launches and
            //there is only one save location i.e. the current location
            else{
                mapAPI.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15.0f));
            }
        }
    }

    public void updateOtherPlayerUI(){
        // get time elapsed locally
        double localElapsedTime = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            localElapsedTime = Instant.now().toEpochMilli() - raceStartTime;
        }

        // check if other player location data has ever been retrieved
        if (otherRaceLocations.size()>0){
            // if not current location is set for other player, this is the first point we are processing
            if (currentLocationOtherPlayer == null) {
                // populate other player data given location coordinate and timestamp
                currentLocationOtherPlayer = otherRaceLocations.get(otherPlayerLocationIndex);
                otherPlayerStartTime = otherRaceLocations.get(otherPlayerLocationIndex).time;

                // increment index we use to access other player's location list
                otherPlayerLocationIndex++;

                // if game is asynchronous, now start the other player's timer and toast started status
                // so that UI updates are in sync
                if (game.isAsync) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        otherPlayerThreadTime = Instant.now().toEpochMilli();
                    }
                    updateTimeOther();
                    Toast.makeText(this, "Other player started their race", Toast.LENGTH_SHORT).show();
                    tvOtherStatus.setText("Status: Running");
                }

                // set other player's track as visible now that they are a part of the race
                otherPlayerTrack.setVisibility(View.VISIBLE);
                spotifyApp.getView().bringToFront();
            }
            // if this is not the first location data of the other player
            else if (otherRaceLocations.size() > otherPlayerLocationIndex){
                // update other player coordinate and timestamp
                currentLocationOtherPlayer = otherRaceLocations.get(otherPlayerLocationIndex);
                RaceLocation secondToLast = otherRaceLocations.get(otherPlayerLocationIndex - 1);

                // check if local time elapsed is greater than other player's time elapsed
                // if so, only then display the data
                while (currentLocationOtherPlayer.time - otherPlayerStartTime < localElapsedTime){
                    // get pace of other player and set UI
                    DecimalFormat df = new DecimalFormat("#,###.##");
                    double hours = (currentLocationOtherPlayer.time - secondToLast.time)/60000/60;
                    double previousDistance = totalDistanceOtherPlayer;
                    Double paceOtherPlayer = (totalDistanceOtherPlayer - previousDistance)/hours;
                    tvOtherPlayerPace.setText("Pace : " + df.format(paceOtherPlayer)+ "mph");

                    // find new total distance ran by other player
                    totalDistanceOtherPlayer += distance(currentLocationOtherPlayer.latLng.lat, currentLocationOtherPlayer.latLng.lng, secondToLast.latLng.lat, secondToLast.latLng.lng);

                    // if the other player has not finished
                    if (totalDistanceOtherPlayer < maxDistance / 100) {
                        // check with new location data who is ahead and update UI accordingly
                        if (totalDistanceOtherPlayer > totalDistance) {
                            playerAhead(localPlayerTrack, otherPlayerTrack);
                        } else {
                            playerAhead(otherPlayerTrack, localPlayerTrack);
                        }

                        // update UI with new total distance
                        double d = totalDistanceOtherPlayer;
                        tvOtherPlayerDistance.setText("Distance: " + df.format(d) + " mi");
                        otherPlayerTrack.setProgress((int) (totalDistanceOtherPlayer * 100));

                        // if this is the last location objecct of the array
                        if (otherRaceLocations.size() - 1 == otherPlayerLocationIndex) {
                            // check if the other player is labeled as finish
                            // if so, they must have quit. update UI accordingly
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
                    }
                    // other player has completed the full distance of the racce
                    else {
                        // set their progress to complete
                        otherPlayerTrack.setProgress((int) (maxDistance));

                        // turn of their timer and remove the listener for their location data
                        timerOn2 = false;
                        otherPlayerRef.removeEventListener(otherPlayerListener);

                        // label the other player locally as finished
                        if (isPlayer1)
                            game.player2.playerFinished = true;
                        else
                            game.player1.playerFinished = true;

                        // update UI with finished notficiation and status text changed
                        Toast.makeText(RaceActivity.this, "Other player finished their race", Toast.LENGTH_SHORT).show();
                        tvOtherStatus.setText("Status: Finished");
                    }

                    // increment index we use to access other player's location list
                    otherPlayerLocationIndex++;

                    // if there are more points backlogged from the other user, set current values
                    // equal to them
                    if(otherRaceLocations.size() > otherPlayerLocationIndex){
                        currentLocationOtherPlayer = otherRaceLocations.get(otherPlayerLocationIndex);
                        secondToLast = otherRaceLocations.get(otherPlayerLocationIndex - 1);
                    }
                    // else break from while loop to continue with rest of code
                    else{
                        break;
                    }
                }
            }
        }
    }

    //Function that updates time every second
    //Uses a thread that uses the boolean timerOn to
    //indicate when to stop
    //Used to update the time every second
    public void updateTime(){
        Thread t = new Thread(() -> {
            while(timerOn){
                //If you ran for more than 15 minutes * game race distance
                //Than the game quits because you were going
                //Extremely slow, 15 minutes per mile pace
                if(game.totalDistance * 15 < totalTimeRan/60000){
                    timerOn = false;
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
                    setTime(timeElapsed, tvTime);
                }
            }
        });
        t.start();
    }

    //Same as the the provious timer function
    //But for the other/online player
    //Uses boolean timerOn2 to indicate when to stop
    public void updateTimeOther(){
        Thread t = new Thread(() -> {
            while(timerOn2){
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }

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
                    setTime(timeElapsed, tvOtherPlayerTimer);
                }
            }
        });
        t.start();
    }

    //Used to set UI for updateTime function
    public void setTime(String time, TextView tv){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv.setText("Time: " + time);
            }
        });
    }

    //Used to make toast to indicate
    //Spotify isn't connected
    @Override
    public void spotifyNotOpen() {
        Toast.makeText(RaceActivity.this, "You didn't connect", Toast.LENGTH_SHORT).show();
    }

    // override back button to execute quit functionality
    @Override
    public void onBackPressed() {
        //super.onBackPressed(); // do not call super during a race
        quitGame();
    }

    @Override
    protected void onDestroy() {
        // if player hasn't already quited and we are destroying the activity
        // attempt to quit the game for them
        if (isPlayer1) {
            if (!game.player1.playerFinished) {
                quitGame();
            }
        } else {
            if (!game.player2.playerFinished) {
                quitGame();
            }
        }
        super.onDestroy();
    }
}

