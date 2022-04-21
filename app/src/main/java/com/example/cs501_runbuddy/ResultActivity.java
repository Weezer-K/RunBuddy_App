package com.example.cs501_runbuddy;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.example.cs501_runbuddy.models.Game;
import com.example.cs501_runbuddy.models.User;
import com.fitbit.api.loaders.ResourceLoaderResult;
import com.fitbit.api.models.HeartRateContainer;
import com.fitbit.api.models.HeartRateData;
import com.fitbit.api.models.HeartRateInfo;
import com.fitbit.api.models.UserContainer;
import com.fitbit.api.services.HeartRateService;
import com.fitbit.api.services.UserService;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.List;

public class ResultActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<ResourceLoaderResult<HeartRateContainer>>{

    private TextView tvResult;
    private TextView tvPlayer1;
    private TextView tvPlayer2;
    private Button btnDetail;
    private Button btnBackHome;
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

        if (isPlayer1) {
            otherPlayerRef = RunBuddyApplication.getDatabase().getReference("games").child(game.ID).child("player2").child("playerFinished");
        } else {
            otherPlayerRef = RunBuddyApplication.getDatabase().getReference("games").child(game.ID).child("player1").child("playerFinished");
        }

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
        tvPlayer1 = findViewById(R.id.tvPlayer1);
        tvPlayer2 = findViewById(R.id.tvPlayer2);
        btnDetail = findViewById(R.id.btnDetail);
        btnBackHome = findViewById(R.id.btnBackHome);

        tvResult.setText("Game ID: " + game.ID);
        tvPlayer1.setText("Player 1: " + game.player1.playerId);
        tvPlayer2.setText("Player 2: " + game.player2.playerId);

        User.getUserNameFromID(game.player1.playerId, new User.MyCallback() {
            @Override
            public void onCallback(String value) {
                tvPlayer1.setText("Player 1: " + value);
            }
        });

        User.getUserNameFromID(game.player2.playerId, new User.MyCallback() {
            @Override
            public void onCallback(String value) {
                tvPlayer2.setText("Player 2: " + value);
            }
        });


        btnDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        btnBackHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                otherPlayerRef.removeEventListener(otherPlayerListener);
                Intent intent = new Intent(ResultActivity.this, HomeActivity.class);
                intent.putExtra("fragment", "History");
                startActivity(intent);
            }
        });

        getLoaderManager().initLoader(getLoaderId(), null, this).forceLoad();
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
                    game.date, game.player1.playerStartTime,
                    game.player1.playerStartTime + Double.valueOf(game.player1.totalTimeRan).longValue());
        else
            return HeartRateService.getHeartRateSummaryLoader(ResultActivity.this,
                    game.date, game.player2.playerStartTime,
                    game.player2.playerStartTime + Double.valueOf(game.player2.totalTimeRan).longValue());
    }

    //Once all the data is retrieved, if the data is successful then call bindProfilesInfo, display to ui
    @Override
    public void onLoadFinished(Loader<ResourceLoaderResult<HeartRateContainer>> loader, ResourceLoaderResult<HeartRateContainer> data) {
        if (data.isSuccessful()) {
//            bindHeartbeatInfo(data.getResult().getActivitiesHeartIntraday().getDataset());
        }
    }

    @Override
    public void onLoaderReset(Loader<ResourceLoaderResult<HeartRateContainer>> loader) {

    }

    //Uses info obtained from fitBit and sets the appropriate
    //Views to display them
    public void bindHeartbeatInfo(List<HeartRateData> dataset) {

        for (HeartRateData data : dataset) {

        }

    }
}