package com.example.cs501_runbuddy;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.content.Loader;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.fitbit.api.loaders.ResourceLoaderResult;
import com.fitbit.api.models.User;
import com.fitbit.api.models.UserContainer;
import com.fitbit.api.services.UserService;
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

import java.text.DecimalFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

//import androidx.loader.app.LoaderManager;
//import androidx.loader.content.Loader;



public class DashboardActivity extends FragmentActivity implements SpotifyFragment.spotifyInterface, OnMapReadyCallback, LoaderManager.LoaderCallbacks<ResourceLoaderResult<UserContainer>> {

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

    private Polyline poly;
    private TextView tv_pace;
    private TextView tv_distance;
    private TextView tv_time;
    private TextView tv_gender;
    private TextView tv_weight;
    private TextView tv_age;
    private ImageView profilePic;
    private Button spotifyButton;
    private boolean isSpotifyOnScreen = false;

    private SpotifyFragment spotifyApp;

    // Variable necessary for calculating running data
    private Instant startTime;
    private double totalDistance;

    // Used to indicate if timer is on or off
    private boolean timerOn = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        tv_pace = findViewById(R.id.tv_pace);
        tv_distance = findViewById(R.id.tv_distance);
        tv_time = findViewById(R.id.tv_time);
        tv_gender = (TextView) findViewById(R.id.tv_gender);
        profilePic = (ImageView) findViewById(R.id.profilePicImageView);
        tv_age = (TextView) findViewById(R.id.tv_age);
        spotifyButton = (Button) findViewById(R.id.spotify);

        spotifyButton.setBackgroundColor(Color.GREEN);

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


        //This defines how often and precise we will request
        //data from the gps sensor
        locationRequest = LocationRequest.create()
                .setInterval(1000 * DEFAULT_UPDATE_INTERVAL)
                .setFastestInterval(1000 * FASTEST_UPDATE_INTERVAL)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
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

        Switch sw_gps = findViewById(R.id.sw_gps);

        //This switch turns on high/low accuracy
        //by changing the location request
        sw_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sw_gps.isChecked()) {
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                } else {
                    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                }
            }
        });

        //Used to display/hide spotify fragment
        spotifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    if (!isSpotifyOnScreen) {
                        getSupportFragmentManager().beginTransaction()
                                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                                .show(spotifyApp)
                                .commit();
                        isSpotifyOnScreen = true;
                    }else{
                        getSupportFragmentManager().beginTransaction()
                                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                                .hide(spotifyApp)
                                .commit();
                        isSpotifyOnScreen = false;
                    }
                }catch (Exception e){
                    Toast.makeText(DashboardActivity.this, "Please make sure spotify is on in the background", Toast.LENGTH_SHORT).show();
                }

            }
        });

        Switch sw_locationUpdates = findViewById(R.id.sw_locationUpdates);
        //Switches on/off requesting data from the gps
        sw_locationUpdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sw_locationUpdates.isChecked()) {
                    startLocationUpdates();
                } else {
                    stopLocationUpdates();
                }
            }
        });
        //Used to update movement data based of a specific interval
        updateGPS();
        //Creates a thread to make the timer change every second
        //Hence why it was not included in the last function
        updateTime();
        getLoaderManager().initLoader(1, null, this).forceLoad();


    }


    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        updateGPS();
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            //Requested for gps permissions
            case PERMISSIONS_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateGPS();
                } else {

                    finish();
                }
                break;
        }
    }


    //Used to update the latitude and longitude of the GPS based off the phone's gps
    private void updateGPS() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(
                DashboardActivity.this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //fusedLocationProviderClient.setMockMode(false);
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this,
                    new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {

                            updateUIWithLocation(location);
                        }
                    });
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_FINE_LOCATION);
            }
        }
    }

    //Helper function for updateGPS()
    //Helps with updating the location, path drawn, and overall pace
    private void updateUIWithLocation(Location location) {

        if(location!=null){

            //Draws marker and polyline and clears old marker
            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            savedLocations.add(currentLocation);
            mapAPI.clear();
            poly = mapAPI.addPolyline(new PolylineOptions().add(savedLocations.get(0)));
            poly.setPoints(savedLocations);
            poly.setVisible(true);
            mapAPI.addMarker(new MarkerOptions().position(currentLocation).title("TestPoint"));

            //Moves camera and updates distance
            if(savedLocations.size()>1) {
                //Used to move runner location
                mapAPI.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
                LatLng secondToLast = savedLocations.get(savedLocations.size() - 2);
                totalDistance += distance(currentLocation.latitude, currentLocation.longitude, secondToLast.latitude, secondToLast.longitude);
                DecimalFormat df = new DecimalFormat("0.00");
                tv_distance.setText("Distance: " + df.format(totalDistance) + " mi");

                //Pace calculation
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    double startTimeMilis = startTime.toEpochMilli();
                    double endTimeMilis = Instant.now().toEpochMilli();
                    double temp = endTimeMilis - startTimeMilis;
                    int seconds = (int) (temp % 60000) / 1000;
                    String secondString = Integer.toString(seconds);
                    //Used when seconds are single digits
                    //So we don't get a time reading like 2:9 instead of 2:09
                    if (secondString.length() == 1) {
                        secondString = "0" + secondString;
                    }

                    double hours = temp/60000/60.0;
                    double pace = totalDistance/hours;
                    tv_pace.setText("Pace: " + df.format(pace) + " mi/h");
                }
            }//This happens when the page first launches and
             // there is only one save location IE the current location
            else{
                mapAPI.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15.0f));
            }
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapAPI = googleMap;

    }
    //Used to update the time every second
    public void updateTime(){
        Thread t = new Thread(() -> {
            while(timerOn){
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    double startTimeMilis = startTime.toEpochMilli();
                    double endTimeMilis = Instant.now().toEpochMilli();
                    double temp = endTimeMilis - startTimeMilis;
                    int minutes = (int) temp / 60000;
                    int seconds = (int) (temp % 60000) / 1000;
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

    //Used to set UI for updateTime function
    public void setTime(String time){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_time.setText("Time: " + time);
            }
        });
    }

    // given two coordinates, calculate the distance in miles
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




    //This gets the profile and retrieves the data
    @NonNull
    @Override
    public Loader<ResourceLoaderResult<UserContainer>> onCreateLoader(int id, @Nullable Bundle args) {
        return UserService.getLoggedInUserLoader(DashboardActivity.this);
    }

    //Once all the data is retrieved, if the data is successful then call bindProfilesInfo, display to ui
    @Override
    public void onLoadFinished(Loader<ResourceLoaderResult<UserContainer>> loader, ResourceLoaderResult<UserContainer> data) {
        if (data.isSuccessful()) {
            bindProfileInfo(data.getResult().getUser());
        }


    }

    @Override
    public void onLoaderReset(Loader<ResourceLoaderResult<UserContainer>> loader) {

    }

    //Uses info obtained from fitBit and sets the appropriate
    //Views to display them
    public void bindProfileInfo(User user) {
        String age = "Age: "+user.getAge().toString();
        String avatar = user.getAvatar(); //Profile picture
        String gender = "Sex: "+ user.getGender(); //Gender

        tv_gender.setText(gender);
        tv_age.setText(age);
        //Avatar returns a url to an image so I made a helper class to set the imageView to it
        new DownloadImageTask((ImageView) findViewById(R.id.profilePicImageView)).execute(avatar);
    }



    @Override
    public void spotifyNotOpen() {
        Toast.makeText(DashboardActivity.this, "You didn't connect", Toast.LENGTH_SHORT).show();
    }
}