package com.example.cs501_runbuddy;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cs501_runbuddy.models.Game;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Collections;


public class MyRacesFragment extends Fragment {

    private ListView HistoryList;//The ListView for public games
    private ListView ActiveRaceList;
    private TextView tvHistoryList;//A hint for how to get in this game
    private Button activeRaceButton;
    private Button pastRaceButton;

    private ArrayList<Game> activeRaces;

    private ArrayList<Game> pastRaces;

    private DatabaseReference gamesRef;
    private ChildEventListener gameListener;
    private TextView instructions;

    private String activeText;
    private String finishedText;

    private TextView noGamesIndicator;

    private BackToLobby listener;

    public MyRacesFragment() {
        // Required empty public constructor
    }

    public interface BackToLobby{
        void backGame(Game game);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_my_races, container, false);

        noGamesIndicator = (TextView) v.findViewById(R.id.noFinishedGamesTextView);

        HistoryList = v.findViewById(R.id.HistoryList);
        ActiveRaceList = v.findViewById(R.id.ActiveRacesList);

        activeRaceButton = (Button) v.findViewById(R.id.myActiveRaceButton);
        pastRaceButton = (Button) v.findViewById(R.id.pastRacesButton);

        instructions = (TextView) v.findViewById(R.id.instructionTextView);

        activeRaceButton.setBackgroundColor(Color.TRANSPARENT);
        pastRaceButton.setBackgroundColor(Color.TRANSPARENT);

        ActiveRaceList.setVisibility(View.INVISIBLE);

        finishedText = "Select a race for results";
        activeText = "Select a race to continue";


        //Displays that finished Races is on and
        //Active Races is off
        pastRaceButton.setTextColor(Color.parseColor("#00203F"));
        activeRaceButton.setTextColor(Color.GRAY);
        instructions.setText(finishedText);



        pastRaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pastRaceButton.setTextColor(Color.parseColor("#00203F"));
                activeRaceButton.setTextColor(Color.GRAY);
                ActiveRaceList.setVisibility(View.INVISIBLE);
                HistoryList.setVisibility(View.VISIBLE);
                instructions.setText(finishedText);
                displayNoGames();
            }
        });

        activeRaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pastRaceButton.setTextColor(Color.GRAY);
                activeRaceButton.setTextColor(Color.parseColor("#00203F"));
                ActiveRaceList.setVisibility(View.VISIBLE);
                HistoryList.setVisibility(View.INVISIBLE);
                instructions.setText(activeText);
                displayNoGames();
            }
        });



        ActiveRaceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                listener.backGame(activeRaces.get(i));
            }
        });



        HistoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(),ResultActivity.class);
                intent.putExtra("game", pastRaces.get(i));
                startActivity(intent);
            }

        });

        pastRaces = new ArrayList<Game>(){};

        activeRaces = new ArrayList<Game>(){};

        String userID = GoogleSignIn.getLastSignedInAccount(getActivity()).getId();
        gamesRef = RunBuddyApplication.getDatabase().getReference("games");



        gameListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Game g = snapshot.getValue(Game.class);
                if( userID.equals(g.player1.playerId) || (g.player2 != null && userID.equals(g.player2.playerId) )) {

                    if((userID.equals(g.player1.playerId) && !g.player1.playerStarted) || (userID.equals(g.player2.playerId) && !g.player2.playerStarted)){
                        activeRaces.add(g);
                        Collections.sort(activeRaces);
                        AdapterGame current = new AdapterGame(getContext(), activeRaces);
                        ActiveRaceList.setAdapter(current);
                    }else{
                        pastRaces.add(g);
                        Collections.sort(pastRaces);
                        AdapterGame current = new AdapterGame(getContext(), pastRaces);
                        HistoryList.setAdapter(current);
                    }
                    displayNoGames();
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        gamesRef.addChildEventListener(gameListener);

        return v;
    }



    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof MyRacesFragment.BackToLobby){
            listener = (MyRacesFragment.BackToLobby) context;
        }else{
            throw new RuntimeException(context.toString() + "must implement BackToLobby");
        }
    }

    public void displayNoGames(){
        if(pastRaceButton.getTextColors().getDefaultColor() == Color.parseColor("#00203F")){
            if(pastRaces.size() > 0){
                noGamesIndicator.setVisibility(View.INVISIBLE);
            }else{
                noGamesIndicator.setVisibility(View.VISIBLE);
            }
        }else{
            if(activeRaces.size() > 0){
                noGamesIndicator.setVisibility(View.INVISIBLE);
            }else{
                noGamesIndicator.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
        gamesRef.removeEventListener(gameListener);
    }
}

