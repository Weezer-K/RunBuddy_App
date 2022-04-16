package com.example.cs501_runbuddy;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cs501_runbuddy.models.Game;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;


public class HistoryFragment extends Fragment {

    private ListView HistoryList;//The ListView for public games
    private TextView tvHistoryList;//A hint for how to get in this game

    private ArrayList<String> gameSummaries; //Arraylist that have the games info
    private ArrayList<String> gameIds;

    private DatabaseReference gamesRef;
    private ChildEventListener gameListener;

    public HistoryFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_history, container, false);

        HistoryList = v.findViewById(R.id.HistoryList);
        tvHistoryList = v.findViewById(R.id.tvHistoryList);


        HistoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getActivity(), gameSummaries.get(i), Toast.LENGTH_SHORT).show();
            }

        });

        String userID = GoogleSignIn.getLastSignedInAccount(getActivity()).getId();
        gamesRef = RunBuddyApplication.getDatabase().getReference("games");

        gameListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Game g = snapshot.getValue(Game.class);
                if( userID.equals(g.player1.playerId)  ||  userID.equals(g.player2.playerId) ) {
                    String summary = "Game: " + g.ID + ", Host: " + g.player1.playerId + ", Distance: " + g.totalDistance;
                    gameSummaries.add(summary);
                    gameIds.add(g.ID);
                    ArrayAdapter arrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1 ,gameSummaries);
                    HistoryList.setAdapter(arrayAdapter);
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


        return v;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        gamesRef.addChildEventListener(gameListener);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        gamesRef.removeEventListener(gameListener);
    }
}