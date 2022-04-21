package com.example.cs501_runbuddy;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import java.util.Collections;


public class PublicGameListFragment extends Fragment implements SearchFragment.SearchGame {

    private ListView GameList;//The ListView for public games
    private TextView tvGameList;//A hint for how to get in this game
    private ArrayList<String> gameIds;
    private ArrayList<Game> activeRaces;
    private SearchFragment.SearchGame listener;
    private DatabaseReference gamesRef;
    private ArrayList<Double> distFilters;
    private ChildEventListener gameListener;


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

        gameIds = new ArrayList<String>(){};
        activeRaces = new ArrayList<Game>(){};


        GameList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Game.joinGameFromDB(gameIds.get(i), listener, getActivity());
            }

        });

        gamesRef = RunBuddyApplication.getDatabase().getReference("games");
        gameListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Game g = snapshot.getValue(Game.class);
                if(!g.isPrivate
                        && g.joinAble
                        && distFilters.contains(g.totalDistance)
                        && !g.player1.playerId.equals(GoogleSignIn.getLastSignedInAccount(getActivity()).getId())) {
                    gameIds.add(g.ID);
                    activeRaces.add(g);
                    Collections.sort(activeRaces);
                    AdapterGame current = new AdapterGame(getContext(), activeRaces);
                    GameList.setAdapter(current);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Game g = snapshot.getValue(Game.class);
                gameIds.remove(g.ID);
                activeRaces.remove(g);
                Collections.sort(activeRaces);
                AdapterGame current = new AdapterGame(getContext(), activeRaces);
                GameList.setAdapter(current);
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


        /*
        gamesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
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
                    ArrayAdapter arrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1 ,gameSummaries);
                    GameList.setAdapter(arrayAdapter);
                    gamesRef.addChildEventListener(gameListener2);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
*/
        /*
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
*/
        return v;
    }

    @Override
    public void searchGame(ArrayList<Double> d) {
        distFilters = d;
        gamesRef.addChildEventListener(gameListener);


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