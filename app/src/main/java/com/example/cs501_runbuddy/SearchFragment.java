package com.example.cs501_runbuddy;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.cs501_runbuddy.models.Game;

import java.util.ArrayList;

// Fragment for searching Games
public class SearchFragment extends Fragment {

    private CheckBox mile1Box;// CheckBox to see if player want to find 1 mile game
    private CheckBox mile5Box;// CheckBox to see if player want to find 5 miles game
    private CheckBox mile10Box;// CheckBox to see if player want to find 10 miles game

    private Button RSbtn;// Button for searching public Game
    private Button SBIbtn;// Button for joining game by ID

    private EditText edtRoomID;// input box for player to input the game lobby id to join private game

    private ArrayList<Double> distFilters;// ArrayList holding the choice of player of the miles he want to search

    private SearchGame listener;// SearchGame Interface that wait for implementation in HomeActivity
    public SearchFragment() {}// Default Constructor

    // Interface to join the private game or search public game
    public interface SearchGame{
        void searchGame(ArrayList<Double> distFilter);
        void joinGame(Game game);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Binding Views
        View v =  inflater.inflate(R.layout.fragment_search, container, false);
        mile1Box = v.findViewById(R.id.mile1Box);
        mile5Box = v.findViewById(R.id.mile5Box);
        mile10Box = v.findViewById(R.id.mile10Box);
        RSbtn = v.findViewById(R.id.RSbtn);
        SBIbtn = v.findViewById(R.id.SBIbtn);
        edtRoomID = v.findViewById(R.id.edtRoomID);

        // Set mile 1 to be checked at the beginning
        mile1Box.setChecked(true);
        distFilters = new ArrayList<Double>();// Instantiate the ArrayList that holds the choice of player of the miles he want to search

        // Public Search Button and its corresponding reaction
        RSbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mile1Box.isChecked()){ distFilters.add(1.0); }// Add 1 to the ArrayList if 1 mile checkbox is checked
                if(mile5Box.isChecked()){ distFilters.add(5.0); }// Add 5 to the ArrayList if 5 miles checkbox is checked
                if(mile10Box.isChecked()){ distFilters.add(10.0); }// Add 10 to the ArrayList if 10 miles checkbox is checked
                if(distFilters.size() == 0){ Toast.makeText(getActivity(), "Please select a race distance", Toast.LENGTH_SHORT).show(); }// Toast them they have to make a choice for random search
                else{ listener.searchGame(distFilters); }// Begin the search, send information to HomeActivity
            }
        });

        // Search By ID button and its corresponding reaction
        SBIbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String joinId = edtRoomID.getText().toString();// Get the room ID from the input text
                Game.joinGameFromDB(joinId, listener, getActivity());// Join the room, sending information to HomeActivity
            }
        });

        // Inflate the View
        return v;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof SearchFragment.SearchGame){
            listener = (SearchFragment.SearchGame) context;// Instantiate the interface
        }else{
            throw new RuntimeException(context.toString() + "must implement SearchGame");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null; // Discard the object when leaving
    }

}