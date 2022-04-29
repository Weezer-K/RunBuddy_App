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

// Display the Races that you can join back because they are active and the games that have result because they are finished
public class MyRacesFragment extends Fragment {

    private ListView HistoryList;//The Listview for Finished Games
    private ListView ActiveRaceList;//The Listview for Active Games
    private Button activeRaceButton;//The Button for switching to Active Game list
    private Button pastRaceButton;// The Button for switching to Finished game list
    private ArrayList<Game> activeRaces;// Arraylist of games that are active
    private ArrayList<Game> pastRaces;// Arraylist of games that are finished

    private DatabaseReference gamesRef;// Reference to access the data from firebase
    private ChildEventListener gameListener;// Event Listener to monitor if there is a change in the firebase database
    private TextView instructions;// Instructions for how to interact with this page on the phone

    private String activeText;//Instructions for what to do with active game
    private String finishedText;//Instructions for what to do with finished game

    private TextView noGamesIndicator;//If there is no games, this will be used

    private BackToLobby listener;// interface that let player go back to the active game that awaits implementation in HomeActivity

    public MyRacesFragment() {}//Default constructor

    // interface that let player go back to the active game that awaits implementation in HomeActivity
    public interface BackToLobby{
        void backGame(Game game);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Binding Views
        View v = inflater.inflate(R.layout.fragment_my_races, container, false);
        noGamesIndicator = (TextView) v.findViewById(R.id.noFinishedGamesTextView);
        HistoryList = v.findViewById(R.id.HistoryList);
        ActiveRaceList = v.findViewById(R.id.ActiveRacesList);
        activeRaceButton = (Button) v.findViewById(R.id.myActiveRaceButton);
        pastRaceButton = (Button) v.findViewById(R.id.pastRacesButton);
        instructions = (TextView) v.findViewById(R.id.instructionTextView);

        // Style for the button, make them look like not a button
        activeRaceButton.setBackgroundColor(Color.TRANSPARENT);
        pastRaceButton.setBackgroundColor(Color.TRANSPARENT);

        // By default when you get to MyRaces, show Finished Game first, so ActiveRace is invisible
        ActiveRaceList.setVisibility(View.INVISIBLE);

        // Instructions for Finished Games and Active Games, tell them what to do
        finishedText = "Select a race for results";
        activeText = "Select a race to continue";


        //Displays that finished Races is on and Active Races is off, Style of the button
        pastRaceButton.setTextColor(Color.parseColor("#00203F"));
        activeRaceButton.setTextColor(Color.GRAY);
        instructions.setText(finishedText);

        //Switch to Finished Game list and modify two buttons style to indicate you ARE on finished game list
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

        //Switch to Active Game list and modify two buttons style to indicate you ARE on active game list
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

        // Go back to the active game, implemented in HomeActivity
        ActiveRaceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) { listener.backGame(activeRaces.get(i)); }
        });

        // Get you to the result activity to display a finished game result
        HistoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(),ResultActivity.class);
                intent.putExtra("game", pastRaces.get(i)); // Pass the game to the result activity using Bundle
                startActivity(intent);
            }
        });

        // Instantiate the ArrayList of Active and Finished Games
        pastRaces = new ArrayList<Game>(){};
        activeRaces = new ArrayList<Game>(){};

        // Get users ID and access games from Firebase by reference
        String userID = GoogleSignIn.getLastSignedInAccount(getActivity()).getId();
        gamesRef = RunBuddyApplication.getDatabase().getReference("games");

        // Listening to data changing in the firebase regarding the games, if there is a change, this will be called
        gameListener = new ChildEventListener() {

            // WHen a new game child is added
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Game g = snapshot.getValue(Game.class);// Acquire the game

                //Find all the games that the users are in, either player 1 or player 2, player 1 means he's the host
                if( userID.equals(g.player1.playerId) || (g.player2 != null && userID.equals(g.player2.playerId) )) {

                    // If he is in a active game and the other player hasn't started the game, meaning the game is active but not started (by our application logic, a game start, it will have result)
                    if((userID.equals(g.player1.playerId) && !g.player1.playerStarted) || (userID.equals(g.player2.playerId) && !g.player2.playerStarted)){
                        activeRaces.add(g); // add the game to the active game ArrayList
                        Collections.sort(activeRaces);// Sorted chronologically
                        AdapterGame current = new AdapterGame(getContext(), activeRaces);//Custom ArrayList adapter for object Game
                        ActiveRaceList.setAdapter(current);// Link it to the lsitview
                    }
                    // Then the game now must have a result, belongs to a Finished game
                    else{
                        pastRaces.add(g);// add the game to the finished game ArrayList
                        Collections.sort(pastRaces);// Sorted chronologically
                        AdapterGame current = new AdapterGame(getContext(), pastRaces);//Custom ArrayList adapter for object Game
                        HistoryList.setAdapter(current);// Link it to the lsitview
                    }
                    displayNoGames();//Indicate there is no games, if there is no games, see below
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) { }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        };
        gamesRef.addChildEventListener(gameListener); //apply the childEventListner we define above to the Database Reference
        return v;// Inflate the view
    }



    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof MyRacesFragment.BackToLobby){
            listener = (MyRacesFragment.BackToLobby) context;// Instantiate the Interface
        }else{
            throw new RuntimeException(context.toString() + "must implement BackToLobby");
        }
    }

    // Indicate there is no games
    public void displayNoGames(){
        if(pastRaceButton.getTextColors().getDefaultColor() == Color.parseColor("#00203F")){
            if(pastRaces.size() > 0){ noGamesIndicator.setVisibility(View.INVISIBLE); }//No Finished Games
            else{ noGamesIndicator.setVisibility(View.VISIBLE); }
        }
        else{//No Active Games
            if(activeRaces.size() > 0){ noGamesIndicator.setVisibility(View.INVISIBLE); }
            else{ noGamesIndicator.setVisibility(View.VISIBLE);}
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;// Discard object when leaving
        gamesRef.removeEventListener(gameListener);// Remove childListener when leaving
    }
}

