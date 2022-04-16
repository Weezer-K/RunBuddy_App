package com.example.cs501_runbuddy;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.cs501_runbuddy.models.Game;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class PublicGameListFragment extends Fragment implements SearchFragment.SearchGame {

    private ListView GameList;//The ListView for public games
    private TextView tvGameList;//A hint for how to get in this game
    private ArrayList<String> gameSummaries; //Arraylist that have the games info
    private ArrayList<String> gameIds;
    private SearchFragment.SearchGame listener;
    private DatabaseReference gamesRef;
    private ValueEventListener gameListener;
    private ArrayList<Double> distFilters;

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

        gameSummaries = new ArrayList<String>(){};
        gameIds = new ArrayList<String>(){};

        gamesRef = RunBuddyApplication.getDatabase().getReference("games");
        GameList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getActivity(), gameSummaries.get(i), Toast.LENGTH_SHORT).show();
                Game.joinGameFromDB(gameIds.get(i), listener, getActivity());
            }

        });

        gameListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Game object and use the values to update the UI
                if (dataSnapshot.exists()) {
                    gameSummaries = new ArrayList<String>(){};
                    // dataSnapshot is the "game" node with all children with id equal to joinId
                    for (DataSnapshot game : dataSnapshot.getChildren()) {
                        Game g = game.getValue(Game.class);
                        if(!g.isPrivate
                                && g.joinAble
                                && distFilters.contains(g.totalDistance)
                                && !g.player1.playerId.equals(GoogleSignIn.getLastSignedInAccount(getActivity()).getId())) {
                            String summary = "Game: " + g.ID + ", Host: " + g.player1.playerId + ", Distance: " + g.totalDistance;
                            gameSummaries.add(summary);
                            gameIds.add(g.ID);
                        }
                    }
                    try{
                        ArrayAdapter arrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1 ,gameSummaries);
                        GameList.setAdapter(arrayAdapter);
                    }catch (NullPointerException e){

                    }

                }

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };

        return v;
    }

    @Override
    public void searchGame(ArrayList<Double> d) {
        distFilters = d;
        gamesRef.addValueEventListener(gameListener);


    }

    @Override
    public void joinGame(Game game) {


    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof SearchFragment.SearchGame){
            listener = (SearchFragment.SearchGame) context;
        }else{
            throw new RuntimeException(context.toString() + "must implement SearchGame");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        gamesRef.removeEventListener(gameListener);
        listener = null;
    }

}