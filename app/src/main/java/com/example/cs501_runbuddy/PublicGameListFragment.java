package com.example.cs501_runbuddy;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.cs501_runbuddy.models.Game;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class PublicGameListFragment extends Fragment {

    private ListView GameList;//The ListView for public games
    private TextView tvGameList;//A hint for how to get in this game
    private ArrayList<String> games; //Arraylist that have the games info

    public PublicGameListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_public_game_list, container, false);
        GameList = v.findViewById(R.id.GameList);
        tvGameList = v.findViewById(R.id.tvGameList);

        games = new ArrayList<String>(){};

        ValueEventListener gameListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Game object and use the values to update the UI
                if (dataSnapshot.exists()) {
                    games = new ArrayList<String>(){};
                    // dataSnapshot is the "game" node with all children with id equal to joinId
                    for (DataSnapshot game : dataSnapshot.getChildren()) {
                        Game g = game.getValue(Game.class);
                        String summary = "Game: " + g.ID + ", Host: " + g.playerOneId + ", Distance: " + g.totalDistance;
                        games.add(summary);
                    }

                    ArrayAdapter arrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1 ,games);
                    GameList.setAdapter(arrayAdapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };

        DatabaseReference gamesRef = RunBuddyApplication.getDatabase().getReference("games");
        gamesRef.addValueEventListener(gameListener);

        GameList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getActivity(), games.get(i), Toast.LENGTH_SHORT).show();
            }

        });
        return v;
    }
}