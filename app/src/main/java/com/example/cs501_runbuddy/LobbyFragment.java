package com.example.cs501_runbuddy;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.cs501_runbuddy.models.Game;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LobbyFragment extends Fragment {


    private TextView LIDtv;
    private TextView player1tv;
    private TextView player2tv;
    private Button startBtn;

    private Game game;

    public LobbyFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_lobby, container, false);
        LIDtv = (TextView) v.findViewById(R.id.LIDtv);
        player1tv = v.findViewById(R.id.player1tv);
        player2tv = v.findViewById(R.id.player2tv);
        startBtn = v.findViewById(R.id.startBtn);

        return v;
    }

    public void createGame(String ID, boolean type, double totalDistance){
        FirebaseDatabase db = RunBuddyApplication.getDatabase();
        DatabaseReference gameRef = db.getReference("games");

        List<Double> locs = Arrays.asList(1.0, 2.0);

        game = new Game(ID, type, totalDistance, false, locs);
        Map<String, Object> gameValues = game.toMap();

        // Write a message to the database
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + ID, gameValues);
        gameRef.updateChildren(childUpdates);

        LIDtv.setText("Game Lobby: " + ID);
    }
}