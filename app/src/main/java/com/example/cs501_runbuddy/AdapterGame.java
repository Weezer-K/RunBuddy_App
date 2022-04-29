package com.example.cs501_runbuddy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.cs501_runbuddy.models.Game;
import com.example.cs501_runbuddy.models.User;

import java.util.ArrayList;

//Custom ArrayAdapter that accept an array of Object Game and display its content in a listview
class AdapterGame extends ArrayAdapter<Game> {

    // Default Constructor
    public AdapterGame(@NonNull Context context, ArrayList<Game> arrayList) {
        super(context, 0, arrayList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View currentItemView = convertView;

        // of the recyclable view is null then inflate the custom layout for the same
        if (currentItemView == null) {
            currentItemView = LayoutInflater.from(getContext()).inflate(R.layout.custom_list_view, parent, false);
        }

        // get the position of the view from the ArrayAdapter
        Game currentGame = getItem(position);

        // Binding views
        TextView tvLobbyID = currentItemView.findViewById(R.id.tvLobbyID);// Lobby ID
        TextView tvHost = currentItemView.findViewById(R.id.tvHost);// Name of the Host
        TextView tvDate = currentItemView.findViewById(R.id.tvDate);// Date of the game played
        TextView tvDistance = currentItemView.findViewById(R.id.tvDistance);// Distance of this game
        TextView tvAsync = currentItemView.findViewById(R.id.asyncIndicatorTextView);// Whether the game is synchronous or asynchronous

        //A call back just for getting the Name of the user to display in tvHost, instead of displaying their ID
        User.getUserNameFromID(currentGame.player1.playerId, new User.MyCallback() {
            @Override
            public void onCallback(String value) {
                tvLobbyID.setText("Game: " + currentGame.ID);// Lobby ID of the game in current array position
                tvDate.setText(currentGame.getStringDate());// Date of the game in current array position
                tvHost.setText("Host: " + value);// Name of the game host in current array postion return from call back
                tvDistance.setText("Distance: " + currentGame.totalDistance);// Distance of the game in current array position
                if(currentGame.isAsync){ tvAsync.setText("Async");// If it is Asynchronous game
                }else{ tvAsync.setText("Sync");}// If it is synchronous game
            }
        });

        return currentItemView;// Inflate the View
    }


}
