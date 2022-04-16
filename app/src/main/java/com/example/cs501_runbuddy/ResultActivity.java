package com.example.cs501_runbuddy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.cs501_runbuddy.models.Game;

public class ResultActivity extends AppCompatActivity {

    private TextView tvResult;
    private TextView tvPlayer1;
    private TextView tvPlayer2;
    private Button btnDetail;
    private Button btnBackHome;
    private Game game;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        // To retrieve object in second Activity
        game = (Game) getIntent().getSerializableExtra("game");

        tvResult = findViewById(R.id.tvResult);
        tvPlayer1 = findViewById(R.id.tvPlayer1);
        tvPlayer2 = findViewById(R.id.tvPlayer2);
        btnDetail = findViewById(R.id.btnDetail);
        btnBackHome = findViewById(R.id.btnBackHome);

        tvResult.setText(game.ID);
        tvPlayer1.setText(game.player1.playerId);
        tvPlayer2.setText(game.player2.playerId);



        btnDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        btnBackHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ResultActivity.this ,HomeActivity.class);
                startActivity(intent);
            }
        });

    }
}