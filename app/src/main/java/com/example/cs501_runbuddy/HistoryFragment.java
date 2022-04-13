package com.example.cs501_runbuddy;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class HistoryFragment extends Fragment {

    private ListView HistoryList;//The ListView for public games
    private TextView tvHistoryList;//A hint for how to get in this game
    private ArrayList<String> history; //Arraylist that have the games info

    public HistoryFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_history, container, false);

        HistoryList = v.findViewById(R.id.HistoryList);
        tvHistoryList = v.findViewById(R.id.tvHistoryList);

        history = new ArrayList<String>(){};

        history.add("Game: 000000, Time: 4/13/2022");
        history.add("Game: 000001, Time: 4/13/2022");

        ArrayAdapter arrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1 ,history);
        HistoryList.setAdapter(arrayAdapter);
        HistoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getActivity(), history.get(i), Toast.LENGTH_SHORT).show();
            }

        });
        return v;
    }
}