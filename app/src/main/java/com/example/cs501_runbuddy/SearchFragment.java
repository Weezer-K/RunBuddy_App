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


public class SearchFragment extends Fragment {

    private CheckBox mile1Box;
    private CheckBox mile5Box;
    private CheckBox mile10Box;

    private Button RSbtn;
    private Button SBIbtn;
    private EditText edtRoomID;

    private ArrayList<Double> distFilters;

    private SearchGame listener;
    public SearchFragment() {
        // Required empty public constructor
    }


    public interface SearchGame{
        void searchGame(ArrayList<Double> distFilter);
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

        distFilters = new ArrayList<Double>();


        // Public Search Button and its corresponding reaction
        RSbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mile1Box.isChecked()){
                    distFilters.add(1.0);
                }

                if(mile5Box.isChecked()){
                    distFilters.add(5.0);
                }

                if(mile10Box.isChecked()){
                    distFilters.add(10.0);
                }

                if(distFilters.size() == 0){
                    Toast.makeText(getActivity(), "Please select a race distance", Toast.LENGTH_SHORT).show();
                }else{
                    listener.searchGame(distFilters);
                }


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