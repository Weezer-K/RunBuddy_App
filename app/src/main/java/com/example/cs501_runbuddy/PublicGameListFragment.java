package com.example.cs501_runbuddy;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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


public class PublicGameListFragment extends Fragment implements SearchFragment.SearchGame {

    private ListView GameList;//The ListView for public games
    private ArrayList<Game> activeRaces;
    private SearchFragment.SearchGame listener;
    private DatabaseReference gamesRef;
    private ArrayList<Double> distFilters;
    private ChildEventListener gameListener;
    private TextView noGamesIndicator;



    public PublicGameListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_public_game_list, container, false);
        GameList = v.findViewById(R.id.GameList);

        activeRaces = new ArrayList<Game>(){};

        noGamesIndicator = (TextView) v.findViewById(R.id.noGamesTextView);


        GameList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Game.joinGameFromDB(activeRaces.get(i).ID, listener, getActivity());
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
                    activeRaces.add(g);
                    Collections.sort(activeRaces);
                    AdapterGame current = new AdapterGame(getContext(), activeRaces);
                    GameList.setAdapter(current);
                    noGamesIndicator.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Game g = snapshot.getValue(Game.class);
                activeRaces.remove(g);
                Collections.sort(activeRaces);
                AdapterGame current = new AdapterGame(getContext(), activeRaces);
                GameList.setAdapter(current);
                if(activeRaces.size() == 0){
                    noGamesIndicator.setVisibility(View.VISIBLE);
                }
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