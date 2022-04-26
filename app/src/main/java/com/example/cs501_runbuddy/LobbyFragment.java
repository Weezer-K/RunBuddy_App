package com.example.cs501_runbuddy;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.cs501_runbuddy.models.Game;
import com.example.cs501_runbuddy.models.RaceLocation;
import com.example.cs501_runbuddy.models.RacePlayer;
import com.example.cs501_runbuddy.models.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class LobbyFragment extends Fragment {

    private Toast mToastToShow;
    private TextView LIDtv;
    private TextView player1tv;
    private TextView player2tv;
    private Button startBtn;

    private Spinner player1Color;
    private Spinner player2Color;

    private Integer color1;
    private Integer color2;

    private Game game;
    private DatabaseReference player2Ref;
    private ValueEventListener player2Listener;

    private DatabaseReference otherPlayerStartedRef;
    private ValueEventListener otherPlayerStartedListener;

    private DatabaseReference otherPlayerReadyRef;
    private ValueEventListener otherPlayerReadyListener;
    private MediaPlayer startSounds;
    private AudioManager audio;
    private fragmentListener f;

    public LobbyFragment() {
        // Required empty public constructor
    }

    public interface fragmentListener{
        AudioManager getAudioManager();
       // void setTimerText(String s);
        //void setBackgroundOn();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof fragmentListener) {
            f = (fragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement FragmentAListener");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_lobby, container, false);
        LIDtv = (TextView) v.findViewById(R.id.LIDtv);
        player1tv = v.findViewById(R.id.player1tv);
        player2tv = v.findViewById(R.id.player2tv);
        startBtn = v.findViewById(R.id.startBtn);
        player1Color = (Spinner) v.findViewById(R.id.player1Spinner);
        player2Color = (Spinner) v.findViewById(R.id.player2Spinner);
        startSounds = MediaPlayer.create(getActivity(), R.raw.start_sound_effect);
        audio = f.getAudioManager();
        f = (fragmentListener) getActivity();

        List<String> colorsList = new ArrayList<String>();
        //{"Red", "Blue", "Green", "Black", "Yellow", "Cyan"};
        colorsList.add("Red");
        colorsList.add("Blue");
        colorsList.add("Green");
        colorsList.add("Yellow");
        colorsList.add("Cyan");
        color1 = Color.RED;

        List<String> colorsList2 = new ArrayList<String>();
        //{"Red", "Blue", "Green", "Black", "Yellow", "Cyan"};
        colorsList2.add("Green");
        colorsList2.add("Blue");
        colorsList2.add("Red");
        colorsList2.add("Yellow");
        colorsList2.add("Cyan");
        color1 = Color.GREEN;




        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),R.layout.spinner_white_colors, colorsList);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(getActivity(), R.layout.spinner_white_colors, colorsList2);
        adapter.setDropDownViewResource(R.layout.spinner_white_colors);
        adapter2.setDropDownViewResource(R.layout.spinner_white_colors);
        player1Color.setAdapter(adapter);
        player2Color.setAdapter(adapter2);
        player1Color.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String colorPicked = player1Color.getItemAtPosition(i).toString();
                color1 = colorValFinder(colorPicked);
                try {
                    ((TextView) adapterView.getChildAt(0)).setTextColor(color1);
                }catch(Exception e){

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                color1 = Color.RED;
            }
        });

        player2Color.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String colorPicked = player2Color.getItemAtPosition(i).toString();
                color2 = colorValFinder(colorPicked);
                try {
                    ((TextView) adapterView.getChildAt(0)).setTextColor(color2);
                }catch(Exception e){

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                color2 = Color.BLUE;
            }
        });

        return v;
    }


    public void createGame(String ID, boolean isPrivate, boolean isAsync, double totalDistance) {

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getActivity());
        String player1Id = acct.getId();

        RacePlayer player1 = new RacePlayer(player1Id, new HashMap<String, RaceLocation>(),
                false, false, false, 0.0,
                0.0, 0L, 0.0);
        RacePlayer player2 = new RacePlayer();

        Long date = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            date = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        }

        //Initialize the gaming object
        game = new Game(ID,
                isPrivate,
                totalDistance,
                true,
                isAsync,
                player1,
                player2,
                null,
                date);

        game.writeToDatabase("",  "");

        LIDtv.setText("Game Lobby ID: " + ID);
        player1tv.setText("Name: " + acct.getGivenName());
        player2tv.setText("Name: Not Joined Yet");

        initializePlayer2Ref();

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (color1.equals(color2)) {
                    Toast.makeText(getActivity(), "Please pick different colors for players 1 and 2", Toast.LENGTH_SHORT).show();
                }else if(game.joinAble){
                    Toast.makeText(getActivity(), "Cannot start race with just 1 player", Toast.LENGTH_SHORT).show();
                } else if (!game.isAsync) {
                    game.player1.playerReady = !game.player1.playerReady;
                    game.writeToDatabase("player1", "playerReady");
                    setTextColorForPlayer(player1tv, game.player1.playerReady);
                    if (game.player1.playerReady && game.player2.playerReady) {

                        try {
                            TimeUnit.SECONDS.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        startRace(color1, color2);
                    }
                } else{
                    startRace(color1, color2);
                }
            }
        });

    }

    public void joinGame(Game g) {
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getActivity());
        game = g;
        game.player2 = new RacePlayer(acct.getId(), new HashMap<String, RaceLocation>(),
                false, false, false, 0.0, 0.0, 0L, 0.0);
        game.joinAble = false;

        game.writeToDatabase("", "");
        initializePlayer2Ref();
        LIDtv.setText("Game Lobby: " + game.ID);
        player1tv.setText("Name: " + game.player1.playerId);
        player2tv.setText("Name: " + acct.getGivenName());

        User.getUserNameFromID(game.player1.playerId, new User.MyCallback() {
            @Override
            public void onCallback(String value) {
                player1tv.setText("Name: " + value);
            }
        });

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (color1.equals(color2)) {
                    Toast.makeText(getActivity(), "Please pick different colors for players 1 and 2", Toast.LENGTH_SHORT).show();
                } else if (!game.isAsync) {
                    game.player2.playerReady = !game.player2.playerReady;
                    game.writeToDatabase("player2", "playerReady");
                    setTextColorForPlayer(player2tv, game.player2.playerReady);
                    if (game.player1.playerReady && game.player2.playerReady) {
                        startRace(color2, color1);
                    }
                } else {
                    startRace(color2, color1);
                }
            }
        });
    }

    public int colorValFinder(String c) {
       int colorPicked = Color.RED;
        if (c.equals("Blue")) {
            colorPicked = Color.BLUE;
        } else if (c.equals("Red")) {
            colorPicked = Color.RED;
        } else if (c.equals("Green")) {
            colorPicked = Color.GREEN;
        } else if (c.equals("Yellow")) {
            colorPicked = Color.YELLOW;
        } else if (c.equals("Cyan")) {
            colorPicked = Color.CYAN;
        } else if (c.equals("Black")) {
            colorPicked = Color.BLACK;
        }

        return colorPicked;
    }

    public void rejoinGame (Game g) {

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getActivity());
        String pId = acct.getId();

        game = g;

        LIDtv.setText("Game Lobby: " + game.ID);

        if (!game.joinAble && pId.equals(game.player1.playerId)) {
            player1tv.setText("Name: " + acct.getGivenName());
            User.getUserNameFromID(game.player2.playerId, new User.MyCallback() {
                @Override
                public void onCallback(String value) {
                    player2tv.setText("Name: "+ value);
                }
            });
        } else if (pId.equals(game.player1.playerId)) {
            player1tv.setText("Name: " + acct.getGivenName());
            player2tv.setText("Name: Not Joined Yet");
            initializePlayer2Ref();
        } else {
            player2tv.setText("Name: " +acct.getGivenName());
            User.getUserNameFromID(game.player1.playerId, new User.MyCallback() {
                @Override
                public void onCallback(String value) {
                    player1tv.setText("Name: " + value);
                }
            });
        }

        initializePlayer2Ref();
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (color1.equals(color2)) {
                    Toast.makeText(getActivity(), "Please pick different colors for players 1 and 2", Toast.LENGTH_SHORT).show();
                }else if (game.joinAble) {
                    Toast.makeText(getActivity(), "Cannot start race with just 1 player", Toast.LENGTH_SHORT).show();
                }  else if (!game.isAsync) {
                    if (pId.equals(game.player1.playerId)) {
                        game.player1.playerReady = !game.player1.playerReady;
                        game.writeToDatabase("player1", "playerReady");
                        setTextColorForPlayer(player1tv, game.player1.playerReady);
                    } else {
                        game.player2.playerReady = !game.player2.playerReady;
                        game.writeToDatabase("player2", "playerReady");
                        setTextColorForPlayer(player2tv, game.player2.playerReady);
                    }
                    if (game.player1.playerReady && game.player2.playerReady) {
                        startRace(color2, color1);
                    }
                } else {
                    startRace(color2, color1);
                }
            }
        });
    }

    public void startRace(Integer localColor, Integer onlineColor) {
        //startCountDown();

        if(audio.getRingerMode() != AudioManager.RINGER_MODE_VIBRATE && audio.getRingerMode() != AudioManager.RINGER_MODE_SILENT){
            startSounds.start();
        }
        new CountDownTimer(4000, 1000) {
            int counter = 0;
            public void onTick(long millisUntilFinished) {
                LIDtv.setText("Game Start In: " + ((millisUntilFinished / 1000)));
            }

            public void onFinish() {
                boolean isPlayer1 = (game.player1.playerId.equals(GoogleSignIn.getLastSignedInAccount(getActivity()).getId()));
                if(isPlayer1){
                    game.player1.playerStarted = true;
                    game.writeToDatabase("player1", "playerStarted");
                }else{
                    game.player2.playerStarted = true;
                    game.writeToDatabase("player2", "playerStarted");
                }
                Intent intent = new Intent(getActivity(), RaceActivity.class);
                intent.putExtra("localPlayerColor", localColor);
                intent.putExtra("onlinePlayerColor", onlineColor);
                intent.putExtra("game", game);
                startActivity(intent);
            }
        }.start();

    }

    public void initializePlayer2Ref() {
        player2Ref = RunBuddyApplication.getDatabase().getReference("games").child(game.ID).child("player2");
        player2Listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // dataSnapshot is the "game" node with all children with id equal to joinId
                    RacePlayer p2 = snapshot.getValue(RacePlayer.class);
                    User.getUserNameFromID(p2.playerId, new User.MyCallback() {
                        @Override
                        public void onCallback(String value) {
                            player2tv.setText("Name: " + value);
                            game.joinAble = false;
                            game.player2 = p2;
                            game.writeToDatabase("player2", "");
                            player2Ref.removeEventListener(player2Listener);
                            if (!game.isAsync) {
                                initializeOtherPlayerReadyRef();
                            } else {
                                initializeOtherPlayerStartedRef();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        player2Ref.addValueEventListener(player2Listener);
    }

    public void initializeOtherPlayerReadyRef() {

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getActivity());
        String pId = acct.getId();

        if (pId.equals(game.player1.playerId)) {
            otherPlayerReadyRef = RunBuddyApplication.getDatabase().getReference("games")
                    .child(game.ID).child("player2").child("playerReady");
        } else {
            otherPlayerReadyRef = RunBuddyApplication.getDatabase().getReference("games")
                    .child(game.ID).child("player1").child("playerReady");
        }

        otherPlayerReadyListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (pId.equals(game.player1.playerId)) {
                        game.player2.playerReady = snapshot.getValue(Boolean.class);
                        setTextColorForPlayer(player2tv, game.player2.playerReady);
                        if (game.player1.playerReady && game.player2.playerReady) {
                            otherPlayerReadyRef.removeEventListener(otherPlayerReadyListener);
                            startRace(color1, color2);
                        }
                    } else {
                        game.player1.playerReady = snapshot.getValue(Boolean.class);
                        setTextColorForPlayer(player1tv, game.player1.playerReady);
                        if (game.player1.playerReady && game.player2.playerReady) {
                            otherPlayerReadyRef.removeEventListener(otherPlayerReadyListener);
                            startRace(color2, color1);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        otherPlayerReadyRef.addValueEventListener(otherPlayerReadyListener);
    }

    public void initializeOtherPlayerStartedRef() {

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getActivity());
        String pId = acct.getId();

        if (pId.equals(game.player1.playerId)) {
            otherPlayerStartedRef = RunBuddyApplication.getDatabase().getReference("games")
                    .child(game.ID).child("player2").child("playerStarted");
        } else {
            otherPlayerStartedRef = RunBuddyApplication.getDatabase().getReference("games")
                    .child(game.ID).child("player1").child("playerStarted");
        }

        otherPlayerStartedListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.getValue(Boolean.class)) {
                        if (pId.equals(game.player1.playerId)) {
                            player2tv.setTextColor(Color.GREEN);
                        } else {
                            player1tv.setTextColor(Color.GREEN);
                        }
                        otherPlayerStartedRef.removeEventListener(otherPlayerStartedListener);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        otherPlayerStartedRef.addValueEventListener(otherPlayerStartedListener);
    }

    public void setTextColorForPlayer(TextView tv, boolean isReady) {
        if (isReady)
            tv.setTextColor(Color.GREEN);
        else
            tv.setTextColor(Color.WHITE);
    }

    @Override
    public void onDetach () {
        super.onDetach();
        if (player2Ref != null)
            player2Ref.removeEventListener(player2Listener);
        f = null;
    }


    private void startCountDown(){
        //f.setBackgroundOn();

        int toastDurationInMilliSeconds = 10000;
        Integer i = 3;

        // Set the countdown to display the toast


        if(audio.getRingerMode() != AudioManager.RINGER_MODE_VIBRATE && audio.getRingerMode() != AudioManager.RINGER_MODE_SILENT){
            startSounds.start();
        }
        try {
            Toast.makeText(getActivity(), "3", Toast.LENGTH_SHORT).show();
            TimeUnit.SECONDS.sleep(1);
            Toast.makeText(getActivity(), "2", Toast.LENGTH_SHORT).show();
            TimeUnit.SECONDS.sleep(1);
            Toast.makeText(getActivity(), "1", Toast.LENGTH_SHORT).show();
            TimeUnit.SECONDS.sleep(1);
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

