package com.example.cs501_runbuddy;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.cs501_runbuddy.models.Game;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.maps.model.LatLng;
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

    public void createGame(String ID, boolean isPrivate, double totalDistance){

        List<LatLng> locs1 = Arrays.asList();
        List<LatLng> locs2 = Arrays.asList();

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getActivity());
        String player1Id = acct.getId();

        //Initialize the gaming object
        game = new Game(ID,
                isPrivate,
                totalDistance,
                true,
                player1Id,
                "",
                locs1,
                locs2);

        game.writeToDatabase("");

        LIDtv.setText("Game Lobby: " + ID);
        player1tv.setText(acct.getGivenName());
        player2tv.setText("Not Yet Joined");

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),RaceActivity.class);
                intent.putExtra("game", game);
                startActivity(intent);
            }
        });
    }

    public void joinGame(Game game) {
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getActivity());

        game.playerTwoId = acct.getId();
        game.joinAble = false;

        game.writeToDatabase("");

        LIDtv.setText("Game Lobby: " + game.ID);
        player1tv.setText(game.playerOneId);
        player2tv.setText(acct.getGivenName());
    }
}