package com.example.cs501_runbuddy;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;

import com.example.cs501_runbuddy.models.Game;
import com.example.cs501_runbuddy.models.RaceLocation;
import com.example.cs501_runbuddy.models.RacePlayer;
import com.example.cs501_runbuddy.models.User;
import com.fitbit.api.loaders.ResourceLoaderResult;
import com.fitbit.api.models.HeartRateContainer;
import com.fitbit.api.models.HeartRateData;
import com.fitbit.api.services.HeartRateService;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ResultActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<ResourceLoaderResult<HeartRateContainer>>, OnMapReadyCallback{

    private TextView tvResult;
    private TextView localNameTextView;
    private TextView otherNameTextView;

    private TextView distanceLocal;
    private TextView paceLocal;
    private TextView timeRanLocal;
    private Button mapLocal;

    private TextView distanceOther;
    private TextView paceOther;
    private TextView timeRanOther;
    private Button mapOther;

    private Button btnDetail;
    private Game game;
    private boolean isPlayer1;
    private DatabaseReference otherPlayerRef;
    private ValueEventListener otherPlayerListener;
    private GoogleMap localMapApi;
    private GoogleMap otherMapApi;
    private boolean localPlayerMapButtonPressed = false;
    private GoogleMap mapApi;

    private int colorSlowPace = Color.RED;
    private int colorMediumPace = Color.YELLOW;
    private int colorFastPace = Color.GREEN;

    private ArrayList<RaceLocation> localRaceLocations;
    private ArrayList<RaceLocation> otherRaceLocations;

    private ArrayList<Integer> localPolyColors;
    private ArrayList<Integer> otherPolyColors;

    private SupportMapFragment mapFragment;
    private Button resultsFromMap;

    private TextView localHeartRate;
    private TextView otherHeartRate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        // To retrieve object in second Activity
        game = (Game) getIntent().getSerializableExtra("game");
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
        mapLocal.setBackgroundColor(Color.GRAY);
        mapOther.setBackgroundColor(Color.GRAY);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.localMapAPI);
        mapFragment.getMapAsync(this);
        mapFragment.getView().setVisibility(View.INVISIBLE);
        resultsFromMap = (Button) findViewById(R.id.backToResults);
        resultsFromMap.setVisibility(View.INVISIBLE);
        resultsFromMap.setClickable(false);
        localHeartRate = (TextView) findViewById(R.id.avgHeartRateLocalPlayer);
        otherHeartRate = (TextView) findViewById(R.id.avgHeartRateOtherPlayer);


        mapLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (isPlayer1 && game.player1.playerFinished) {
                        if (mapFragment.isVisible()) {
                            mapFragment.getView().setVisibility(View.INVISIBLE);
                            mapLocal.setBackgroundColor(Color.GREEN);
                        } else {
                            mapFragment.getView().setVisibility(View.VISIBLE);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                setMap(game.player1, true);
                            }

                            double lat = localRaceLocations.get(localRaceLocations.size() - 1).latLng.lat;
                            double lng = localRaceLocations.get(localRaceLocations.size() - 1).latLng.lng;
                            LatLng latLng = new LatLng(lat, lng);
                            mapApi.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));
                            resultsFromMap.setVisibility(View.VISIBLE);
                            resultsFromMap.setClickable(true);
                        }

                    } else if (!isPlayer1 && game.player2.playerFinished) {
                        if (mapFragment.isVisible()) {
                            mapFragment.getView().setVisibility(View.INVISIBLE);
                            mapOther.setBackgroundColor(Color.GREEN);
                        } else {
                            mapFragment.getView().setVisibility(View.VISIBLE);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                setMap(game.player2, true);
                            }
                            double lat = localRaceLocations.get(localRaceLocations.size() - 1).latLng.lat;
                            double lng = localRaceLocations.get(localRaceLocations.size() - 1).latLng.lng;
                            LatLng latLng = new LatLng(lat, lng);
                            mapApi.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));
                            resultsFromMap.setVisibility(View.VISIBLE);
                            resultsFromMap.setClickable(true);
                        }
                    }

                } catch (Exception e) {
                    Toast.makeText(ResultActivity.this, "You had no gps data Sorry, you can look at the other things though", Toast.LENGTH_SHORT).show();
                    mapFragment.getView().setVisibility(View.INVISIBLE);
                    mapLocal.setClickable(false);
                    mapLocal.setVisibility(View.INVISIBLE);
                }
            }
        });

        resultsFromMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapApi.clear();
                mapFragment.getView().setVisibility(View.INVISIBLE);
                mapLocal.setBackgroundColor(Color.GREEN);
                resultsFromMap.setVisibility(View.INVISIBLE);
                resultsFromMap.setClickable(false);
            }
        });

        mapOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isPlayer1 && game.player2.playerFinished){
                    if(mapFragment.isVisible()){
                        mapFragment.getView().setVisibility(View.INVISIBLE);
                        mapOther.setBackgroundColor(Color.GREEN);
                    }else{
                        mapFragment.getView().setVisibility(View.VISIBLE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            setMap(game.player2, false);
                        }
                        double lat = otherRaceLocations.get(otherRaceLocations.size() - 1).latLng.lat;
                        double lng = otherRaceLocations.get(otherRaceLocations.size() - 1).latLng.lng;
                        LatLng latLng = new LatLng(lat, lng);
                        mapApi.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));
                        resultsFromMap.setVisibility(View.VISIBLE);
                        resultsFromMap.setClickable(true);
                    }

                }else if(!isPlayer1 && game.player1.playerFinished ){
                    if(mapFragment.isVisible()){
                        mapFragment.getView().setVisibility(View.INVISIBLE);
                        mapLocal.setBackgroundColor(Color.GREEN);
                    }else{
                        mapFragment.getView().setVisibility(View.VISIBLE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            setMap(game.player1, false);
                        }
                        double lat = otherRaceLocations.get(otherRaceLocations.size() - 1).latLng.lat;
                        double lng = otherRaceLocations.get(otherRaceLocations.size() - 1).latLng.lng;
                        LatLng latLng = new LatLng(lat, lng);
                        mapApi.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));
                        resultsFromMap.setVisibility(View.VISIBLE);
                        resultsFromMap.setClickable(true);
                    }
                }
            }
        });







        if (isPlayer1) {
            otherPlayerRef = RunBuddyApplication.getDatabase().getReference("games").child(game.ID).child("player2").child("playerFinished");
        } else {
            otherPlayerRef = RunBuddyApplication.getDatabase().getReference("games").child(game.ID).child("player1").child("playerFinished");
        }

        setTextViews();

        otherPlayerListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.getValue(Boolean.class)) {
                        if (isPlayer1) {
                            game.player2.playerFinished = true;
                        } else {
                            game.player1.playerFinished = true;
                        }
                        getWinner();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        otherPlayerRef.addValueEventListener(otherPlayerListener);

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
        if(isPlayer1){
            if(game.player1.heartRate.equals(0.0)){
                getLoaderManager().initLoader(getLoaderId(), null, this).forceLoad();
            }
        }else{
            if(game.player2.heartRate.equals(0.0)){
                getLoaderManager().initLoader(getLoaderId(), null, this).forceLoad();
            }
        }
    }

    protected int getLoaderId() {
        return 2;
    }

    public void getWinner() {
        if (game.player1.playerFinished && game.player2.playerFinished) {
            game.readOtherPlayer(isPlayer1, new Game.MyCallback() {
                @Override
                public void onCallback() {
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
            });

        }
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

        double sumRate = 0;

        for (HeartRateData data : dataset) {
            sumRate += data.getValue();
        }

        double averageHeartRate = sumRate / dataset.size();

        if(isPlayer1){
            game.player1.heartRate = averageHeartRate;
            game.writeToDatabase("player1", "heartRate");
        }else{
            game.player2.heartRate = averageHeartRate;
            game.writeToDatabase("player2", "heartRate");
        }



        //TODO display avg heart rate
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed(); // do not call super, especially after a race since you should
        // never go back to the race activity
        otherPlayerRef.removeEventListener(otherPlayerListener);
        Intent intent = new Intent(ResultActivity.this, HomeActivity.class);
        intent.putExtra("fragment", "History");
        startActivity(intent);
    }


    public void setTextViews(){
        if(game.player1.playerFinished && game.player2.playerFinished){
            if(isPlayer1){
                setTextViewsLocal(game.player1);
                setTextViewsOther(game.player2);
            }else{
                setTextViewsLocal(game.player2);
                setTextViewsOther(game.player1);
            }
        }else if(game.player1.playerFinished){
            if(isPlayer1){
                setTextViewsLocal(game.player1);
            }else{
                setTextViewsOther(game.player1);
            }
        }else{
            if(!isPlayer1){
                setTextViewsLocal(game.player2);
            }else{
                setTextViewsOther(game.player2);
            }
        }
    }

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
        distanceLocal.setText("Distance: " + df.format(player.totalDistanceRan));
        timeRanLocal.setText("Time: "+ timeElapsed);
        Double pace = player.totalDistanceRan/(minutesDouble/60);
        paceLocal.setText("Pace: " + df.format(pace) + "mph");
        mapLocal.setBackgroundColor(Color.GREEN);
        if(player.heartRate != null){
            localHeartRate.setText("Heart Rate: " + player.heartRate.toString());
        }
    }

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
        distanceOther.setText("Distance: " + df.format(player.totalDistanceRan));
        timeRanOther.setText("Time: "+ timeElapsed);
        Double pace = player.totalDistanceRan/(minutesDouble/60);
        paceLocal.setText("Pace: " + df.format(pace) + "mph");
        mapOther.setBackgroundColor(Color.GREEN);
        if(player.heartRate != null){
            otherHeartRate.setText("Heart Rate: " + player.heartRate.toString());
        }
    }


    private void setMap(RacePlayer player, boolean isLocal){
        if(isLocal) {
            if (localRaceLocations == null|| otherRaceLocations.size() == 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    localRaceLocations = populatePolyLists(player);
                    reDrawPolyLines(localRaceLocations);
                }

            } else {
                reDrawPolyLines(localRaceLocations);
            }
        }else{
            if (otherRaceLocations == null || otherRaceLocations.size() == 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    otherRaceLocations = populatePolyLists(player);
                    reDrawPolyLines(otherRaceLocations);
                }

            } else {
                reDrawPolyLines(otherRaceLocations);
            }
        }




    }


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

    public void reDrawPolyLines(List<RaceLocation> savedLocations){
        //mapApi.clear();
        LatLng cur = new LatLng(0, 0);
        for(int i = 0; i < savedLocations.size() - 1; i+=1){
            double prevLat = savedLocations.get(i).latLng.lat;
            double prevLng = savedLocations.get(i).latLng.lng;
            double curLat = savedLocations.get(i+1).latLng.lat;
            double curLng = savedLocations.get(i+1).latLng.lng;
            LatLng prev = new LatLng(prevLat, prevLng);
            cur = new LatLng(curLat, curLng);
            Polyline p = mapApi.addPolyline(new PolylineOptions()
                    .clickable(false)
                    .add(prev, cur));
            double distance = distance(prevLat, prevLng, curLat, curLng);
            double t1 = savedLocations.get(i).time;
            double t2 = savedLocations.get(i+1).time;
            double timeBetween = Math.abs(t1 - t2);
            double timeBetweenHours = timeBetween / 60000 / 60.0;
            double curPace = distance/timeBetweenHours;

            if(curPace < 5){
                p.setColor(colorSlowPace);
            }else if(curPace < 8){
                p.setColor(colorMediumPace);
            }else{
                p.setColor(colorFastPace);
            }
        }
        mapApi.moveCamera(CameraUpdateFactory.newLatLng(cur));
    }

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





    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapApi = googleMap;
    }
}