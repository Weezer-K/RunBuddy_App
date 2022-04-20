package com.example.cs501_runbuddy;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class LobbyFragment extends Fragment {


    private TextView LIDtv;
    private TextView player1tv;
    private TextView player2tv;
    private Button startBtn;

    private Spinner player1Color;
    private Spinner player2Color;

    private Integer color1;
    private Integer color2;

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
        player1Color = (Spinner) v.findViewById(R.id.player1Spinner);
        player2Color = (Spinner) v.findViewById(R.id.player2Spinner);

        List<String> colorsList = new ArrayList<String>();
        //{"Red", "Blue", "Green", "Black", "Yellow", "Cyan"};
        colorsList.add("Red");
        colorsList.add("Blue");
        colorsList.add("Green");
        colorsList.add("Yellow");
        colorsList.add("Cyan");
        colorsList.add("Gray");

        List<String> colorsList2 = new ArrayList<String>();
        //{"Red", "Blue", "Green", "Black", "Yellow", "Cyan"};
        colorsList2.add("Blue");
        colorsList2.add("Red");
        colorsList2.add("Green");
        colorsList2.add("Yellow");
        colorsList2.add("Cyan");
        colorsList2.add("Gray");


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, colorsList);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, colorsList2);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        player1Color.setAdapter(adapter);
        player2Color.setAdapter(adapter2);
        player1Color.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String colorPicked = player1Color.getItemAtPosition(i).toString();
                color1 = colorValFinder(colorPicked);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                color1 = Color.RED;
            }
        });

        player2Color.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String colorPicked = player2Color.getItemAtPosition(i).toString();
                color2 = colorValFinder(colorPicked);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                color2 = Color.RED;
            }
        });

        if (savedInstanceState != null) {
            game = (Game) savedInstanceState.getSerializable("game");
            initializePlayer2Ref();
        }

        return v;
    }

    public void createGame(String ID, boolean isPrivate, double totalDistance) {

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getActivity());
        String player1Id = acct.getId();

        RacePlayer player1 = new RacePlayer(player1Id, new HashMap<String, RaceLocation>(), false, false);
        RacePlayer player2 = new RacePlayer();

        Long date = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            date = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        }

        //Initialize the gaming object
        game = new Game(ID,
                isPrivate,
                totalDistance,
                true,
                player1,
                player2,
                null,
                date);

        game.writeToDatabase("",  "");

        LIDtv.setText("Game Lobby ID: " + ID);
        player1tv.setText("Player 1: " + acct.getGivenName());
        player2tv.setText("Player 2: ");

        initializePlayer2Ref();

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (color1.equals(color2)) {
                    Toast.makeText(getActivity(), "Please pick different colors for players 1 and 2", Toast.LENGTH_SHORT).show();
                }else if(game.joinAble){
                    Toast.makeText(getActivity(), "Cannot start race with just 1 player", Toast.LENGTH_SHORT).show();
                }else{
                    Intent intent = new Intent(getActivity(), RaceActivity.class);
                    game.player1.playerStarted = true;
                    game.writeToDatabase("player1", "playerStarted");
                    intent.putExtra("game", game);
                    intent.putExtra("localPlayerColor", color1);
                    intent.putExtra("onlinePlayerColor", color2);
                    startActivity(intent);
                }
            }
        });

    }

    public void joinGame(Game game) {
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getActivity());

        game.player2 = new RacePlayer(acct.getId(), new HashMap<String, RaceLocation>(), false, false);
        game.joinAble = false;

        game.writeToDatabase("", "");

        LIDtv.setText("Game Lobby: " + game.ID);
        player1tv.setText("Player 1: " + game.player1.playerId);
        player2tv.setText("Player 2: " + acct.getGivenName());

        User.getUserNameFromID(game.player1.playerId, new User.MyCallback() {
            @Override
            public void onCallback(String value) {
                player1tv.setText(value);
            }
        });

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (color1.equals(color2)) {
                    Toast.makeText(getActivity(), "Please pick different colors for players 1 and 2", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(getActivity(), RaceActivity.class);
                    game.player2.playerStarted = true;
                    game.writeToDatabase("player2", "playerStarted");
                    intent.putExtra("localPlayerColor", color2);
                    intent.putExtra("onlinePlayerColor", color1);
                    intent.putExtra("game", game);
                    startActivity(intent);
                }
            }
        });
    }

    public int colorValFinder(String c) {
        if (c.equals("Blue")) {
            return Color.BLUE;
        } else if (c.equals("Red")) {
            return Color.RED;
        } else if (c.equals("Green")) {
            return Color.GREEN;
        } else if (c.equals("Yellow")) {
            return Color.YELLOW;
        } else if (c.equals("Cyan")) {
            return Color.CYAN;
        } else if (c.equals("Black")) {
            return Color.BLACK;
        } else {
            return Color.GRAY;
        }
    }
        public void rejoinGame (Game g) {

            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getActivity());
            String pId = acct.getId();

            game = g;

            LIDtv.setText("Game Lobby: " + game.ID);

            if (!game.joinAble && pId.equals(game.player1.playerId)) {
                player1tv.setText(acct.getGivenName());
                User.getUserNameFromID(game.player2.playerId, new User.MyCallback() {
                    @Override
                    public void onCallback(String value) {
                        player2tv.setText(value);
                    }
                });
            } else if (pId.equals(game.player1.playerId)) {
                player1tv.setText(acct.getGivenName());
                player2tv.setText("Not Yet Joined");
                initializePlayer2Ref();
            } else {
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
                    Intent intent = new Intent(getActivity(), RaceActivity.class);
                    if (color1.equals(color2)) {
                        Toast.makeText(getActivity(), "Please pick different colors for players 1 and 2", Toast.LENGTH_SHORT).show();
                    }else if (game.joinAble) {
                        Toast.makeText(getActivity(), "Cannot start race with just 1 player", Toast.LENGTH_SHORT).show();
                    } else {
                        if (pId.equals(game.player1.playerId)) {
                            game.player1.playerStarted = true;
                            game.writeToDatabase("player1", "playerStarted");
                        } else {
                            game.player2.playerStarted = true;
                            game.writeToDatabase("player2", "playerStarted");
                        }
                        intent.putExtra("localPlayerColor", color2);
                        intent.putExtra("onlinePlayerColor", color1);
                        intent.putExtra("game", game);
                        startActivity(intent);
                    }
                }
            });
        }

        public void initializePlayer2Ref () {
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
                                game.player2 = p2;
                                game.writeToDatabase("player2", "");
                                player2Ref.removeEventListener(player2Listener);
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
        public void onDetach () {
            super.onDetach();
            if (player2Ref != null)
                player2Ref.removeEventListener(player2Listener);
        }
    }

