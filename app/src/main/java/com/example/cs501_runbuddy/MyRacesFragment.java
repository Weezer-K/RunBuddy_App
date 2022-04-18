package com.example.cs501_runbuddy;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cs501_runbuddy.models.Game;
import com.example.cs501_runbuddy.models.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;


public class MyRacesFragment extends Fragment {

    private ListView HistoryList;//The ListView for public games
    private ListView ActiveRaceList;
    private TextView tvHistoryList;//A hint for how to get in this game
    private Button activeRaceButton;
    private Button pastRaceButton;

    private ArrayList<String> activeSummaries; //Arraylist that have the games info
    private ArrayList<Game> activeRaces;

    private ArrayList<String> pastSummaries; //Arraylist that have the games info
    private ArrayList<Game> pastRaces;

    private DatabaseReference gamesRef;
    private ChildEventListener gameListener;

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

        HistoryList = v.findViewById(R.id.HistoryList);
        tvHistoryList = v.findViewById(R.id.tvHistoryList);
        ActiveRaceList = v.findViewById(R.id.ActiveRacesList);

        activeRaceButton = (Button) v.findViewById(R.id.myActiveRaceButton);
        pastRaceButton = (Button) v.findViewById(R.id.pastRacesButton);

        activeRaceButton.setBackgroundColor(Color.TRANSPARENT);
        pastRaceButton.setBackgroundColor(Color.TRANSPARENT);

        ActiveRaceList.setVisibility(View.INVISIBLE);

        pastRaceButton.setTextColor(Color.GREEN);
        activeRaceButton.setTextColor(Color.GRAY);


        pastRaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pastRaceButton.setTextColor(Color.GREEN);
                activeRaceButton.setTextColor(Color.GRAY);
                ActiveRaceList.setVisibility(View.INVISIBLE);
                HistoryList.setVisibility(View.VISIBLE);
            }
        });

        activeRaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pastRaceButton.setTextColor(Color.GRAY);
                activeRaceButton.setTextColor(Color.GREEN);
                ActiveRaceList.setVisibility(View.VISIBLE);
                HistoryList.setVisibility(View.INVISIBLE);
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
                Toast.makeText(getActivity(), pastSummaries.get(i), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(),ResultActivity.class);
                intent.putExtra("game", pastRaces.get(i));
                startActivity(intent);
            }

        });

        pastSummaries = new ArrayList<String>(){};
        pastRaces = new ArrayList<Game>(){};

        activeSummaries = new ArrayList<String>(){};
        activeRaces = new ArrayList<Game>(){};

        String userID = GoogleSignIn.getLastSignedInAccount(getActivity()).getId();
        gamesRef = RunBuddyApplication.getDatabase().getReference("games");

        gameListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Game g = snapshot.getValue(Game.class);
                if( userID.equals(g.player1.playerId) || (g.player2 != null && userID.equals(g.player2.playerId) )) {
                    User.getUserNameFromID(g.player1.playerId, new User.MyCallback() {
                        @Override
                        public void onCallback(String value) {
                            String summary = "Game: " + g.ID + ", Date: " + g.getStringDate() + ", Host: " + value + ", Distance: " + g.totalDistance;

                            if((userID.equals(g.player1.playerId) && !g.player1.playerStarted) || (userID.equals(g.player2.playerId) && !g.player2.playerStarted)){
                                activeSummaries.add(summary);
                                activeRaces.add(g);
                                ArrayAdapter arrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, activeSummaries);
                                ActiveRaceList.setAdapter(arrayAdapter);
                            }else{
                                pastSummaries.add(summary);
                                pastRaces.add(g);
                                ArrayAdapter arrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1 , pastSummaries);
                                HistoryList.setAdapter(arrayAdapter);
                            }
                        }
                    });
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

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
        gamesRef.removeEventListener(gameListener);
    }
}