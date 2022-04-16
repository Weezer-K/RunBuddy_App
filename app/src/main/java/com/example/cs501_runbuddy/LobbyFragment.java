package com.example.cs501_runbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.cs501_runbuddy.models.Game;
import com.example.cs501_runbuddy.models.RaceLocation;
import com.example.cs501_runbuddy.models.RacePlayer;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.util.ArrayList;


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



        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getActivity());
        String player1Id = acct.getId();

        RacePlayer player1 = new RacePlayer(player1Id, new ArrayList<RaceLocation>(), false);
        RacePlayer player2 = new RacePlayer();




        //Initialize the gaming object
        game = new Game(ID,
                isPrivate,
                totalDistance,
                true,
                player1,
                player2);

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

        game.player2 = new RacePlayer(acct.getId(), new ArrayList<RaceLocation>(), false);
        game.joinAble = false;

        game.writeToDatabase("");

        LIDtv.setText("Game Lobby: " + game.ID);
        player1tv.setText(game.player1.playerId);
        player2tv.setText(acct.getGivenName());

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),RaceActivity.class);
                intent.putExtra("game", game);
                startActivity(intent);
            }
        });
    }
}