package com.example.cs501_runbuddy;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.example.cs501_runbuddy.models.Game;
import com.example.cs501_runbuddy.models.RacePlayer;
import com.example.cs501_runbuddy.models.User;
import com.fitbit.api.loaders.ResourceLoaderResult;
import com.fitbit.api.models.HeartRateContainer;
import com.fitbit.api.models.HeartRateData;
import com.fitbit.api.services.HeartRateService;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.List;

public class ResultActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<ResourceLoaderResult<HeartRateContainer>>{

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
            game.readOtherPlayerDistanceRan(isPlayer1, new Game.MyCallback() {
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
            bindHeartbeatInfo(data.getResult().getActivitiesHeartIntraday().getDataset());
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

        distanceLocal.setText("Distance: " + df.format(player.totalDistanceRan));
        timeRanLocal.setText("Time: "+ df.format(player.totalTimeRan));
        Double pace = player.totalTimeRan/player.totalDistanceRan;
        paceLocal.setText("Pace: " + df.format(pace));
        setMap(true, player);
    }

    public void setTextViewsOther(RacePlayer player){
        DecimalFormat df = new DecimalFormat("#,###.##");
        distanceOther.setText("Distance: " + df.format(player.totalDistanceRan));
        timeRanOther.setText("Time: "+ df.format(player.totalTimeRan));
        Double pace = player.totalTimeRan/player.totalDistanceRan;
        paceOther.setText("Pace: " + df.format(pace));
        setMap(false, player);
    }

    public void setMap(boolean localPlayer, RacePlayer player){

    }
}