package com.example.cs501_runbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.cs501_runbuddy.models.Game;
import com.example.cs501_runbuddy.models.RaceLocation;
import com.example.cs501_runbuddy.models.RacePlayer;
import com.example.cs501_runbuddy.models.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class LobbyFragment extends Fragment {


    private TextView LIDtv;
    private TextView player1tv;
    private TextView player2tv;
    private Button startBtn;

    private Game game;
    private DatabaseReference player2Ref;
    private ValueEventListener player2Listener;

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

        if (savedInstanceState != null) {
            game = (Game) savedInstanceState.getSerializable("game");
            initializePlayer2Ref();
        }

        return v;
    }

    public void createGame(String ID, boolean isPrivate, double totalDistance){

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getActivity());
        String player1Id = acct.getId();

        RacePlayer player1 = new RacePlayer(player1Id, new ArrayList<RaceLocation>(), false, false);
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

        initializePlayer2Ref();

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Todo: don't let him start unless player 2 join in
                if (game.joinAble) {
                    Toast.makeText(getActivity(), "Cannot start race with just 1 player", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(getActivity(),RaceActivity.class);
                    game.player1.playerStarted = true;
                    game.writeToDatabase("player1");
                    intent.putExtra("game", game);
                    startActivity(intent);
                }
            }
        });

    }

    public void joinGame(Game game) {
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getActivity());

        game.player2 = new RacePlayer(acct.getId(), new ArrayList<RaceLocation>(), false, false);
        game.joinAble = false;

        game.writeToDatabase("");

        LIDtv.setText("Game Lobby: " + game.ID);
        player1tv.setText(game.player1.playerId);
        player2tv.setText(acct.getGivenName());

        User.getUserNameFromID(game.player1.playerId, new User.MyCallback() {
            @Override
            public void onCallback(String value) {
                player1tv.setText(value);
            }
        });

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),RaceActivity.class);
                game.player2.playerStarted = true;
                game.writeToDatabase("player2");
                intent.putExtra("game", game);
                startActivity(intent);
            }
        });
    }

    public void rejoinGame(Game g){

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getActivity());
        String pId = acct.getId();

        game = g;

        LIDtv.setText("Game Lobby: " + game.ID);

        if(!game.joinAble && pId.equals(game.player1.playerId)){
            player1tv.setText(acct.getGivenName());
            User.getUserNameFromID(game.player2.playerId, new User.MyCallback() {
                @Override
                public void onCallback(String value) {
                    player2tv.setText(value);
                }
            });
        } else if (pId.equals(game.player1.playerId)){
                player1tv.setText(acct.getGivenName());
                player2tv.setText("Not Yet Joined");
                initializePlayer2Ref();
        }
        else{
            player2tv.setText(acct.getGivenName());
            User.getUserNameFromID(game.player1.playerId, new User.MyCallback() {
                @Override
                public void onCallback(String value) {
                    player1tv.setText(value);
                }
            });
        }


        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Todo: don't let him start unless player 2 join in
                Intent intent = new Intent(getActivity(),RaceActivity.class);
                if(game.joinAble){
                    Toast.makeText(getActivity(), "Cannot start race with just 1 player", Toast.LENGTH_SHORT).show();
                } else {
                    if (pId.equals(game.player1.playerId)) {
                        game.player1.playerStarted = true;
                        game.writeToDatabase("player1");
                    } else {
                        game.player2.playerStarted = true;
                        game.writeToDatabase("player2");
                    }
                    intent.putExtra("game", game);
                    startActivity(intent);
                }
            }
        });
    }

    public void initializePlayer2Ref() {
        player2Ref = RunBuddyApplication.getDatabase().getReference("games").child(game.ID).child("player2");
        player2Listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // dataSnapshot is the "game" node with all children with id equal to joinId
                    RacePlayer p2 = snapshot.getValue(RacePlayer.class);
                    User.getUserNameFromID(p2.playerId, new User.MyCallback() {
                        @Override
                        public void onCallback(String value) {
                            player2tv.setText(value);
                            game.joinAble = false;
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        player2Ref.addValueEventListener(player2Listener);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (player2Ref != null)
            player2Ref.removeEventListener(player2Listener);
    }
}