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


public class PublicGameListFragment extends Fragment {

    private ListView GameList;//The ListView for public games
    private TextView tvGameList;//A hint for how to get in this game
    private ArrayList<String> games; //Arraylist that have the games info

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

        games = new ArrayList<String>(){};

        games.add("Game: 000000, Host: Tanky, Distance: 1 mile");
        games.add("Game: 000000, Host: Squishy, Distance: 10 miles");


        ArrayAdapter arrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1 ,games);
        GameList.setAdapter(arrayAdapter);
        GameList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getActivity(), games.get(i), Toast.LENGTH_SHORT).show();
            }

        });
        return v;
    }
}