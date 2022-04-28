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

class AdapterGame extends ArrayAdapter<Game> {

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


        // then according to the position of the view assign the desired TextView 1 for the same

        TextView tvLobbyID = currentItemView.findViewById(R.id.tvLobbyID);
        TextView tvHost = currentItemView.findViewById(R.id.tvHost);
        TextView tvDate = currentItemView.findViewById(R.id.tvDate);
        TextView tvDistance = currentItemView.findViewById(R.id.tvDistance);
        TextView tvAsync = currentItemView.findViewById(R.id.asyncIndicatorTextView);


        User.getUserNameFromID(currentGame.player1.playerId, new User.MyCallback() {
            @Override
            public void onCallback(String value) {
                tvLobbyID.setText("Game: " + currentGame.ID);
                tvDate.setText(currentGame.getStringDate());
                tvHost.setText("Host: " + value);
                tvDistance.setText("Distance: " + currentGame.totalDistance);
                if(currentGame.isAsync){
                    tvAsync.setText("Async");
                }else{
                    tvAsync.setText("Sync");
                }
            }
        });


        return currentItemView;
    }


}
