package com.example.cs501_runbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cs501_runbuddy.models.Game;
import com.example.cs501_runbuddy.models.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class ResultActivity extends AppCompatActivity {

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
}