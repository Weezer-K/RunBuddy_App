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

// Public Game List to display the public games
public class PublicGameListFragment extends Fragment implements SearchFragment.SearchGame {

    private ListView GameList;//The ListView for public games
    private ArrayList<Game> activeRaces;//The Arraylist of the games
    private ArrayList<Double> distFilters;//Arraylist that holds the information of chosen distance
    private SearchFragment.SearchGame listener;//SearchGame Object that is used to join the game using its joinGame function in the interface
    private DatabaseReference gamesRef;// Reference to access the data from firebase
    private ChildEventListener gameListener;// Event Listener to monitor if there is a change in the firebase database
    private TextView noGamesIndicator;// If there is no game to display, then use this textview to say that

    //Default Constructor
    public PublicGameListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Binding Views
        View v = inflater.inflate(R.layout.fragment_public_game_list, container, false);
        GameList = v.findViewById(R.id.GameList);
        noGamesIndicator = (TextView) v.findViewById(R.id.noGamesTextView);

        activeRaces = new ArrayList<Game>(){};// Instantiate Arraylist of Games

        //Join the game when clicked, using the SearchGame joinGame function
        GameList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) { Game.joinGameFromDB(activeRaces.get(i).ID, listener, getActivity());}
        });

        // Listening to data changing in the firebase regarding the games, if there is a change, this will be called
        gamesRef = RunBuddyApplication.getDatabase().getReference("games");
        gameListener = new ChildEventListener() {

            // WHen a new game child is added
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Game g = snapshot.getValue(Game.class);
                if(!g.isPrivate // The game obviously has to be private
                        && g.joinAble // The game must have a vacancy
                        && distFilters.contains(g.totalDistance)// It must match the searching preference from the player regarding distances
                        && !g.player1.playerId.equals(GoogleSignIn.getLastSignedInAccount(getActivity()).getId())// cannot see his own game obviously, he can find it in my races
                ) {
                    activeRaces.add(g);// add it to the arraylist of Games
                    Collections.sort(activeRaces);// Sort it in chronological order
                    AdapterGame current = new AdapterGame(getContext(), activeRaces);// Custom ArrayAdapter that takes a Arraylist of Games
                    GameList.setAdapter(current);// Link it to the GameList listview
                    noGamesIndicator.setVisibility(View.INVISIBLE);// we have games, we don't need this textview anymore.
                }
            }

            // When a game child is changed, in our application meaning it's been joined
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Game g = snapshot.getValue(Game.class);// Get the Game object from firebase
                activeRaces.remove(g);// Remove it from the Arraylist
                Collections.sort(activeRaces);// Sort it again in chronological order
                AdapterGame current = new AdapterGame(getContext(), activeRaces);// Custom ArrayAdapter that takes a Arraylist of Games
                GameList.setAdapter(current);// Link it to the GameList listview
                if(activeRaces.size() == 0){ noGamesIndicator.setVisibility(View.VISIBLE); }// If there is no Game after removed, then we need to display this textview
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) { }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        };

        // Inflate the View
        return v;
    }

    @Override
    public void searchGame(ArrayList<Double> d) {
        distFilters = d;// Arraylist of distance wanted for search passed by the SearchFragment
        gamesRef.addChildEventListener(gameListener);// Add the childEventListener we implement above
    }

    // We just need searchGame for this one, ignore this one
    @Override
    public void joinGame(Game game) {}

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof SearchFragment.SearchGame){
            listener = (SearchFragment.SearchGame) context; //Instantiate the interface
        }else{
            throw new RuntimeException(context.toString() + "must implement SearchGame");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        gamesRef.removeEventListener(gameListener);// Remove event listener when leaving
        listener = null; // Discard object when leaving
    }

}