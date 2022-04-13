package com.example.cs501_runbuddy;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.cs501_runbuddy.models.Game;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


public class SearchFragment extends Fragment {

    private CheckBox mile1Box;
    private CheckBox mile5Box;
    private CheckBox mile10Box;

    private Button RSbtn;
    private Button SBIbtn;
    private EditText edtRoomID;

    private SearchGame listener;
    public SearchFragment() {
        // Required empty public constructor
    }


    public interface SearchGame{
        void searchGame();
        void joinGame(Game game);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_search, container, false);

        mile1Box = v.findViewById(R.id.mile1Box);
        mile5Box = v.findViewById(R.id.mile5Box);
        mile10Box = v.findViewById(R.id.mile10Box);

        RSbtn = v.findViewById(R.id.RSbtn);
        SBIbtn = v.findViewById(R.id.SBIbtn);
        edtRoomID = v.findViewById(R.id.edtRoomID);

        mile1Box.setChecked(true);

        //make user only choose one certain mile game
        mile1Box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mile1Box.setChecked(true);
                mile5Box.setChecked(false);
                mile10Box.setChecked(false);

            }
        });
        mile5Box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mile1Box.setChecked(false);
                mile5Box.setChecked(true);
                mile10Box.setChecked(false);

            }
        });
        mile10Box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mile1Box.setChecked(false);
                mile5Box.setChecked(false);
                mile10Box.setChecked(true);
            }
        });

        // Public Search Button and its corresponding reaction
        RSbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.searchGame();
            }
        });


        // Search By ID button and its corresponding reaction
        SBIbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String joinId = edtRoomID.getText().toString();

                Game.joinGameFromDB(joinId, listener, getActivity());
            }
        });

        return v;
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
        listener = null;
    }
}