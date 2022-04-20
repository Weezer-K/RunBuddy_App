package com.example.cs501_runbuddy;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cs501_runbuddy.models.Game;
import com.example.cs501_runbuddy.models.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Collections;


public class MyRacesFragment extends Fragment {

    private ListView HistoryList;//The ListView for public games
    private ListView ActiveRaceList;
    private TextView tvHistoryList;//A hint for how to get in this game
    private Button activeRaceButton;
    private Button pastRaceButton;

    private ArrayList<Game> activeRaces;

    private ArrayList<Game> pastRaces;

    private DatabaseReference gamesRef;
    private ChildEventListener gameListener;

    private BackToLobby listener;

    public MyRacesFragment() {
        // Required empty public constructor
    }

    public interface BackToLobby{
        void backGame(Game game);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_my_races, container, false);

        HistoryList = v.findViewById(R.id.HistoryList);
        tvHistoryList = v.findViewById(R.id.tvHistoryList);
        ActiveRaceList = v.findViewById(R.id.ActiveRacesList);

        activeRaceButton = (Button) v.findViewById(R.id.myActiveRaceButton);
        pastRaceButton = (Button) v.findViewById(R.id.pastRacesButton);

        activeRaceButton.setBackgroundColor(Color.TRANSPARENT);
        pastRaceButton.setBackgroundColor(Color.TRANSPARENT);

        ActiveRaceList.setVisibility(View.INVISIBLE);

        pastRaceButton.setTextColor(Color.GREEN);
        activeRaceButton.setTextColor(Color.GRAY);


        pastRaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pastRaceButton.setTextColor(Color.GREEN);
                activeRaceButton.setTextColor(Color.GRAY);
                ActiveRaceList.setVisibility(View.INVISIBLE);
                HistoryList.setVisibility(View.VISIBLE);
            }
        });

        activeRaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pastRaceButton.setTextColor(Color.GRAY);
                activeRaceButton.setTextColor(Color.GREEN);
                ActiveRaceList.setVisibility(View.VISIBLE);
                HistoryList.setVisibility(View.INVISIBLE);
            }
        });



        ActiveRaceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                listener.backGame(activeRaces.get(i));
            }
        });



        HistoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(),ResultActivity.class);
                intent.putExtra("game", pastRaces.get(i));
                startActivity(intent);
            }

        });

        pastRaces = new ArrayList<Game>(){};

        activeRaces = new ArrayList<Game>(){};

        String userID = GoogleSignIn.getLastSignedInAccount(getActivity()).getId();
        gamesRef = RunBuddyApplication.getDatabase().getReference("games");



        gameListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Game g = snapshot.getValue(Game.class);
                if( userID.equals(g.player1.playerId) || (g.player2 != null && userID.equals(g.player2.playerId) )) {

                    if((userID.equals(g.player1.playerId) && !g.player1.playerStarted) || (userID.equals(g.player2.playerId) && !g.player2.playerStarted)){
                        activeRaces.add(g);
                        Collections.sort(activeRaces);
                        AdapterGame current = new AdapterGame(getContext(), activeRaces);
                        ActiveRaceList.setAdapter(current);
                    }else{
                        pastRaces.add(g);
                        Collections.sort(pastRaces);
                        AdapterGame current = new AdapterGame(getContext(), pastRaces);
                        HistoryList.setAdapter(current);

                    }
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        gamesRef.addChildEventListener(gameListener);

        return v;
    }



    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof MyRacesFragment.BackToLobby){
            listener = (MyRacesFragment.BackToLobby) context;
        }else{
            throw new RuntimeException(context.toString() + "must implement BackToLobby");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
        gamesRef.removeEventListener(gameListener);
    }
}

class AdapterGame extends ArrayAdapter<Game>{

    public AdapterGame(@NonNull Context context, ArrayList<Game> arrayList) {
        super(context, 0, arrayList );
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
        Game currentNumberPosition = getItem(position);



        // then according to the position of the view assign the desired TextView 1 for the same

        TextView textView1 = currentItemView.findViewById(R.id.tvGameInfo);

        User.getUserNameFromID(currentNumberPosition.player1.playerId, new User.MyCallback() {
            @Override
            public void onCallback(String value) {
                textView1.setText("Game: " + currentNumberPosition.ID
                        + ", Host: " + value
                        + ", Date: " + currentNumberPosition.getStringDate()
                        + ", Distance: " + currentNumberPosition.totalDistance
                );

            }
        });

        
        return currentItemView;
    }




}