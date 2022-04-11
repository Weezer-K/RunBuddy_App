package com.example.cs501_runbuddy;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


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

    public void createGame(String ID, String type, int totalDistance){
        LIDtv.setText("Game Lobby: " + ID);
        game = new Game(ID, type, totalDistance);
    }
}