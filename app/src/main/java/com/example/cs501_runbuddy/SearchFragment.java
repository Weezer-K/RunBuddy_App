package com.example.cs501_runbuddy;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;


public class SearchFragment extends Fragment {

    private CheckBox mile1Box;
    private CheckBox mile5Box;
    private CheckBox mile10Box;

    private Button RSbtn;
    private Button SBIbtn;
    private EditText edtRoomID;


    public SearchFragment() {
        // Required empty public constructor
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
        return v;
    }
}