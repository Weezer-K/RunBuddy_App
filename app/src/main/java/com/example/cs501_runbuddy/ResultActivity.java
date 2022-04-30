package com.example.cs501_runbuddy;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.LatLng;
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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ResultActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ResourceLoaderResult<HeartRateContainer>>, OnMapReadyCallback{

    private TextView tvResult;
    private TextView localNameTextView;
    private TextView otherNameTextView;
    private TextView tvGameDate;

    private TextView distanceLocal;
    private TextView paceLocal;
    private TextView timeRanLocal;
    private Button mapLocal;

    private TextView distanceOther;
    private TextView paceOther;
    private TextView timeRanOther;
    private Button mapOther;

    private Game game;
    private boolean isPlayer1;
    private DatabaseReference otherPlayerFinishedRef;
    private ValueEventListener otherPlayerFinishedListener;
    private DatabaseReference otherPlayerStartedRef;
    private ValueEventListener otherPlayerStartedListener;
    private GoogleMap mapApi;

    private int colorSlowPace = Color.RED;
    private int colorMediumPace = Color.YELLOW;
    private int colorFastPace = Color.GREEN;

    private ArrayList<RaceLocation> localRaceLocations;
    private ArrayList<RaceLocation> otherRaceLocations;

    private SupportMapFragment mapFragment;

    private TextView localHeartRate;
    private TextView otherHeartRate;
    private boolean mapLocalActivated;
    private boolean mapOtherActivated;
    private TextView winnerLoser;
    private int activateColor = Color.parseColor("#00203F");
    private ImageView info1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // To retrieve object in second Activity
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
        //mapLocal.setBackgroundColor(Color.GRAY);
        //mapOther.setBackgroundColor(Color.GRAY);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.localMapAPI);
        mapFragment.getMapAsync(this);
        mapFragment.getView().setVisibility(View.INVISIBLE);
        localHeartRate = (TextView) findViewById(R.id.avgHeartRateLocal);
        otherHeartRate = (TextView) findViewById(R.id.avgHeartRateOther);
        winnerLoser = (TextView) findViewById(R.id.winnerLoserText);
        info1 = (ImageView) findViewById(R.id.heartRateInfo1);
        mapLocal.setTextColor(Color.GRAY);
        mapLocalActivated = false;
        mapOtherActivated = false;

        tvGameDate.setText(game.getStringDate());

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


        info1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                balloon.showAlignBottom(info1);
            }
        });

        mapLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ColorStateList cl = mapLocal.getTextColors();
                //relative.setBackgroundColor(cl.getDefaultColor());
                if(cl.getDefaultColor() == (Color.parseColor("#00203F"))){
                    mapLocal.setTextColor(Color.GRAY);
                }else{
                    mapLocal.setTextColor(Color.parseColor("#00203F"));
                }
                if(!mapFragment.isVisible()) {
                    if (isPlayer1 && game.player1.playerFinished) {
                        mapButtonsPressed(game.player1, true);
                        mapLocalActivated = true;
                       // mapLocal.setBackgroundColor(Color.GRAY);
                    }else if(!isPlayer1 && game.player2.playerFinished){
                        mapButtonsPressed(game.player2, true);
                        mapLocalActivated = true;
                       // mapLocal.setBackgroundColor(Color.GRAY);
                    }
                }else{
                    if(mapLocalActivated){
                        mapLocalActivated = false;
                        mapApi.clear();
                        mapFragment.getView().setVisibility(View.INVISIBLE);
                        //mapLocal.setBackgroundColor(activateColor);
                    }else if(mapOtherActivated){
                        mapOtherActivated = false;
                        mapLocalActivated = true;
                        mapApi.clear();
                        if (isPlayer1 && game.player1.playerFinished){
                            mapButtonsPressed(game.player1, true);
                            //mapLocal.setBackgroundColor(Color.GRAY);
                            //mapOther.setBackgroundColor(activateColor);
                        }else if(!isPlayer1 && game.player2.playerFinished){
                            mapButtonsPressed(game.player2, true);
                            //mapOther.setBackgroundColor(activateColor);
                        }
                    }
                }
            }
        });

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

        if (isPlayer1) {
            otherPlayerFinishedRef = RunBuddyApplication.getDatabase().getReference("games").child(game.ID).child("player2").child("playerFinished");
            otherPlayerStartedRef = RunBuddyApplication.getDatabase().getReference("games").child(game.ID).child("player2").child("playerStarted");
        } else {
            otherPlayerFinishedRef = RunBuddyApplication.getDatabase().getReference("games").child(game.ID).child("player1").child("playerFinished");
            otherPlayerStartedRef = RunBuddyApplication.getDatabase().getReference("games").child(game.ID).child("player1").child("playerStarted");
        }

        setTextViews();

        otherPlayerFinishedListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.getValue(Boolean.class)) {
                        game.readOtherPlayer(isPlayer1, new Game.OtherPlayerCallback() {
                            @Override
                            public void onCallback() {
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

        otherPlayerStartedListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.getValue(Boolean.class)) {
                        game.readOtherPlayerLongField(isPlayer1, "playerStartTime", new Game.OtherPlayerLongFieldCallback() {
                            @Override
                            public void onCallback(Long value) {
                                Long date = null;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    date = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

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

    protected int getLoaderId()  {
        return 2;
    }


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
            lat = otherRaceLocations.get(otherRaceLocations.size() - 1).latLng.lat;
            lng = otherRaceLocations.get(otherRaceLocations.size() - 1).latLng.lng;
        }else{
            lat = localRaceLocations.get(localRaceLocations.size() - 1).latLng.lat;
            lng = localRaceLocations.get(localRaceLocations.size() - 1).latLng.lng;
        }
        LatLng latLng = new LatLng(lat, lng);
        mapApi.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13.25f));
    }

    public void getWinner() {
        if (game.player1.playerFinished && game.player2.playerFinished) {
            game.readOtherPlayer(isPlayer1, new Game.OtherPlayerCallback() {
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
    }

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
                }
            }
        }
    }


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


    private void setMap(RacePlayer player, boolean isLocal){
        if(isLocal) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                localRaceLocations = populatePolyLists(player);
                reDrawPolyLines(localRaceLocations);
            }
        }else{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    otherRaceLocations = populatePolyLists(player);
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