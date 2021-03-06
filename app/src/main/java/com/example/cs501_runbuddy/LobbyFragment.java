package com.example.cs501_runbuddy;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
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
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.cs501_runbuddy.models.Game;
import com.example.cs501_runbuddy.models.RaceLocation;
import com.example.cs501_runbuddy.models.RacePlayer;
import com.example.cs501_runbuddy.models.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
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

    private TextView LIDtv; // Indicate the Lobby ID, aka Game ID
    private TextView player1tv;// The name of the Host
    private TextView player2tv;// The name of the second player
    private Button startBtn;// Button to start the Game
    private TextView player1ReadyText;// Indicating whether the host is ready
    private TextView player2ReadyText;// Indicating whether player 2 is ready

    private Spinner player1Color;// Dropbox for choosing the color for the Host's marker and track in the game
    private Spinner player2Color;// Dropbox for choosing the color for the player2's marker and track in the game

    private Integer color1;// Color of the Host
    private Integer color2;// Color of the player 2

    private Game game;// Game Object that stores the current game information that is later sent to Game Activity

    // This is the database reference and listener for player2
    private DatabaseReference player2Ref;
    private ValueEventListener player2Listener;

    // This is the database reference and listener for other player start condition check
    private DatabaseReference otherPlayerStartedRef;
    private ValueEventListener otherPlayerStartedListener;

    // This is the database reference and listener for other player ready condition check
    private DatabaseReference otherPlayerReadyRef;
    private ValueEventListener otherPlayerReadyListener;

    // Audio for count down
    private MediaPlayer startSounds;
    private AudioManager audio;
    private fragmentListener f;

    private final int PERMISSIONS_FINE_LOCATION = 99;
    private boolean gpsAccess;

    public LobbyFragment() {
    }

    //In order for this fragment to use audio for the countdown,
    //We need to use an interface that connects to the homeactivity
    public interface fragmentListener {
        //Returns the audio manager
        AudioManager getAudioManager();
    }


    //Used to attach the fragmentListener
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
        player1ReadyText = (TextView) v.findViewById(R.id.player1ReadyStatus);
        player2ReadyText = (TextView) v.findViewById(R.id.player2ReadyStatus);
        audio = f.getAudioManager(); //Call to initialize audio player
        f = (fragmentListener) getActivity();
        gpsAccess = false;

        //Used for the player 1 Color Spinner
        List<String> colorsList = new ArrayList<String>();
        colorsList.add("Red");
        colorsList.add("Blue");
        colorsList.add("Green");
        colorsList.add("Yellow");
        colorsList.add("Orange");
        color1 = Color.RED; //Since the first choice is red initialize color1 as red

        //Used for the player 2 Color Spinner
        List<String> colorsList2 = new ArrayList<String>();
        colorsList2.add("Green");
        colorsList2.add("Blue");
        colorsList2.add("Red");
        colorsList2.add("Yellow");
        colorsList2.add("Orange");
        color2 = Color.GREEN; //Since the first choice is green initialize color2 as green


        //Adapters for spinners that use a custom xml that makes all the
        // text white instead of black in the dropdown menu
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_white_colors, colorsList);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(getActivity(), R.layout.spinner_white_colors, colorsList2);
        adapter.setDropDownViewResource(R.layout.spinner_white_colors);
        adapter2.setDropDownViewResource(R.layout.spinner_white_colors);
        player1Color.setAdapter(adapter);
        player2Color.setAdapter(adapter2);


        //Used to set color of chosen text in spinner to it's matching color
        //Ex the text "Red" will appear red
        player1Color.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String colorPicked = player1Color.getItemAtPosition(i).toString();
                color1 = colorValFinder(colorPicked);
                try {
                    ((TextView) adapterView.getChildAt(0)).setTextColor(color1);
                } catch (Exception e) {

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                color1 = Color.RED;
                try {
                    ((TextView) adapterView.getChildAt(0)).setTextColor(color1);
                } catch (Exception e) {

                }
            }
        });

        //Same as the other setOnItemSelectedListener
        //But for player2 dropdown
        player2Color.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String colorPicked = player2Color.getItemAtPosition(i).toString();
                color2 = colorValFinder(colorPicked);
                try {
                    ((TextView) adapterView.getChildAt(0)).setTextColor(color2);
                } catch (Exception e) {

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                color2 = Color.GREEN;
                try {
                    ((TextView) adapterView.getChildAt(0)).setTextColor(color2);
                } catch (Exception e) {

                }
            }
        });

        getGPSPermission();

        return v;
    }


    //Function in charge of creating games
    //String ID: Represents ID of game
    //boolean isPrivate: If true game will not appear on public search and need to use join by ID
    //boolean isAsync: If true players can start at different times
    //double totalDistance: The distance of the race, EX value would be 5 if a 5 mile race
    public void createGame(String ID, boolean isPrivate, boolean isAsync, double totalDistance) {
        //Used to get current user to help create the game object
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getActivity());
        String player1Id = acct.getId();

        //Used to initialize RacePlayer Objects in games
        RacePlayer player1 = new RacePlayer(player1Id, new HashMap<String, RaceLocation>(),
                false, false, false, 0.0,
                0.0, 0L, 0.0);
        RacePlayer player2 = new RacePlayer();

        //Used to store date of game
        //Specifically when it was created
        Long date = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            date = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        }

        //Initialize the gaming object
        //Game joinable is set to true as game was just created
        game = new Game(ID,
                isPrivate,
                totalDistance,
                true,
                isAsync,
                player1,
                player2,
                null,
                date);

        // write the entire game object to the database in its initial state
        game.writeToDatabase("", "");

        //Sets views of class to be their appropriate counter parts
        //Ex is syncronous the startBtn text needs to be set to ready
        //as both players need to ready up before starting
        LIDtv.setText("Game Lobby ID: " + ID);
        player1tv.setText("Name: " + acct.getGivenName());
        player2tv.setText("Name: ");
        if (!game.isAsync) {
            startBtn.setText("Ready");
        }

        //Sets the text color for player depending on
        //Their start ready status
        //Start/Ready = Green
        //Not Started/Not Ready = White
        setTextColorForPlayer(player1ReadyText);

        // initialize listener to the player 2 object
        initializePlayer2Ref();

        //Start Button Listener reacts diffently based on if the
        //Game is Async or Sync
        //Async: Button will say start and when other player is in
        //lobby you any player can start whenever they want
        //Sync: Button will have the text ready and require that both players press it
        // before the game starts
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Checks are as follows
                //First check is for the drop down colors, specifically making sure they aren't the same
                //Second check is making sure that there are two players in the lobby
                //If first two pass then we check if the game is syncronous
                getGPSPermission();
                if (!gpsAccess) {
                    Toast.makeText(getActivity(), "Please make sure to grant request to GPS", Toast.LENGTH_SHORT).show();
                }
                else if (color1.equals(color2)) {
                    Toast.makeText(getActivity(), "Please pick different colors for players 1 and 2", Toast.LENGTH_SHORT).show();
                } else if (game.joinAble) {
                    Toast.makeText(getActivity(), "Cannot start race with just 1 player", Toast.LENGTH_SHORT).show();
                } else if (!game.isAsync) {
                    //Makes it so player can unready if other player hasn't readied yet
                    game.player1.playerReady = !game.player1.playerReady;
                    //Writes player1 status to database so player 2 can check if ready
                    game.writeToDatabase("player1", "playerReady");
                    setTextColorForPlayer(player1ReadyText);
                    // change button text according to ready status
                    if (!game.player1.playerReady) {
                        startBtn.setText("Ready");
                    } else {
                        startBtn.setText("Unready");
                    }
                    if (game.player1.playerReady && game.player2.playerReady) {
                        try {
                            TimeUnit.SECONDS.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //If both players ready start the race
                        startRace(color1, color2);
                    }
                } else {
                    //used for async games, so one player can start without the other
                    startRace(color1, color2);
                }
            }
        });

    }

    //Function used to join a game
    public void joinGame(Game g) {
        //Just like create game we need to know about ths signed in user
        //To help properly fill the database with appropriate user information
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getActivity());
        game = g;
        game.player2 = new RacePlayer(acct.getId(), new HashMap<String, RaceLocation>(),
                false, false, false, 0.0, 0.0, 0L, 0.0);
        game.joinAble = false;

        // write the entire game object to the database in its joined state
        game.writeToDatabase("", "");

        // if game is synchronous, initialize listener for other player's ready field
        if (!game.isAsync) {
            initializeOtherPlayerReadyRef();
        }
        // else for asynchronous, initialize listener for other player's started field
        else {
            initializeOtherPlayerStartedRef();
        }

        // set UI of lobby for joined player
        LIDtv.setText("Game Lobby: " + game.ID);
        player1tv.setText("Name: " + game.player1.playerId);
        player2tv.setText("Name: " + acct.getGivenName());
        if (!game.isAsync) {
            startBtn.setText("Ready");
        }
        //Sets text colors based on start/ready status
        //Start ready = Green
        //Not start/Not Ready = white
        setTextColorForPlayer(player1ReadyText);
        setTextColorForPlayer(player2ReadyText);

        //used in order to get the players name and display it on the UI
        User.getUserNameFromID(game.player1.playerId, new User.MyCallback() {
            @Override
            public void onCallback(String value) {
                player1tv.setText("Name: " + value);
            }
        });

        //Used to set the start button on click listener
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Just like in create game the first check
                //is to make sure both users didn't select
                //the same exact item in the color spinner
                //Then has a check and logic for sync mode
                getGPSPermission();
                if (!gpsAccess) {
                    Toast.makeText(getActivity(), "Please make sure to grant request to GPS", Toast.LENGTH_SHORT).show();
                }
                else if (color1.equals(color2)) {
                    Toast.makeText(getActivity(), "Please pick different colors for players 1 and 2", Toast.LENGTH_SHORT).show();
                } else if (!game.isAsync) {
                    game.player2.playerReady = !game.player2.playerReady;
                    game.writeToDatabase("player2", "playerReady");
                    setTextColorForPlayer(player2ReadyText);
                    if (!game.player2.playerReady) {
                        startBtn.setText("Ready");
                    } else {
                        startBtn.setText("Unready");
                    }
                    // start race if both players ready
                    if (game.player1.playerReady && game.player2.playerReady) {
                        startRace(color2, color1);
                    }
                } else {
                    // start race if player clicked to start
                    startRace(color2, color1);
                }
            }
        });
    }



    //Function use case is when someone leaves a lobby but hasn't
    //started a game. This function is called so they can rejoin the lobby
    //And start a race if needed/can deepening on the other player's status
    public void rejoinGame(Game g) {

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getActivity());

        //Used to get ID of current account and
        //Reference with database
        String pId = acct.getId();
        game = g;
        LIDtv.setText("Game Lobby: " + game.ID);

        //if the game isn't joinable and the ID matches with the
        //ID of the player that was in the game
        //Then set both player text views to display their names
        if (!game.joinAble && pId.equals(game.player1.playerId)) {
            player1tv.setText("Name: " + acct.getGivenName());
            User.getUserNameFromID(game.player2.playerId, new User.MyCallback() {
                @Override
                public void onCallback(String value) {
                    player2tv.setText("Name: " + value);
                }
            });
            //Else if the game is joinable that meants player 2 hasn't joined yet
            //And the UI must reflect that by saying the player hasn't joined yet
        } else if (pId.equals(game.player1.playerId)) {
            player1tv.setText("Name: " + acct.getGivenName());
            player2tv.setText("Name: ");
            initializePlayer2Ref();
            //Else we are player 2 and must set the player text view to the
            //current local user and get and set the online player1's name
        } else {
            player2tv.setText("Name: " + acct.getGivenName());
            User.getUserNameFromID(game.player1.playerId, new User.MyCallback() {
                @Override
                public void onCallback(String value) {
                    player1tv.setText("Name: " + value);
                }
            });
        }

        // update lobby UI with correct info
        if (!game.isAsync) {
            startBtn.setText("Ready");
        }
        setTextColorForPlayer(player1ReadyText);
        if (game.player2 != null) {
            setTextColorForPlayer(player2ReadyText);
            // if game is synchronous, initialize listener for other player's ready field
            if (!game.isAsync) {
                initializeOtherPlayerReadyRef();
            }
            // else for asynchronous, initialize listener for other player's started field
            else {
                initializeOtherPlayerStartedRef();
            }
        }
        else
            initializePlayer2Ref();

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getGPSPermission();
                if (!gpsAccess) {
                    Toast.makeText(getActivity(), "Please make sure to grant request to GPS", Toast.LENGTH_SHORT).show();
                }
                else if (color1.equals(color2)) {
                    Toast.makeText(getActivity(), "Please pick different colors for players 1 and 2", Toast.LENGTH_SHORT).show();
                } else if (game.joinAble) {
                    Toast.makeText(getActivity(), "Cannot start race with just 1 player", Toast.LENGTH_SHORT).show();
                } else if (!game.isAsync) {
                    if (pId.equals(game.player1.playerId)) {
                        game.player1.playerReady = !game.player1.playerReady;
                        game.writeToDatabase("player1", "playerReady");
                        setTextColorForPlayer(player1ReadyText);
                        if (!game.player1.playerReady) {
                            startBtn.setText("Ready");
                        } else {
                            startBtn.setText("Unready");
                        }
                    } else {
                        game.player2.playerReady = !game.player2.playerReady;
                        game.writeToDatabase("player2", "playerReady");
                        setTextColorForPlayer(player2ReadyText);
                        if (!game.player2.playerReady) {
                            startBtn.setText("Ready");
                        } else {
                            startBtn.setText("Unready");
                        }
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

    //Function that is in charge of properly setting up DB for next intent
    //Which is Race Activity, also starts that activity
    public void startRace(Integer localColor, Integer onlineColor) {
        startBtn.setEnabled(false);

        //This is used to start the countdown beeps
        //Will check if user is in silent or vibrate mode
        //And play sound if not in any of those modes
        if (audio.getRingerMode() != AudioManager.RINGER_MODE_VIBRATE && audio.getRingerMode() != AudioManager.RINGER_MODE_SILENT) {
            startSounds.start();
        }

        //used to delay the intent call and display a countdown
        //In the UI
        new CountDownTimer(4000, 1000) {
            int counter = 0;

            public void onTick(long millisUntilFinished) {
                LIDtv.setText("Game Start In: " + ((millisUntilFinished / 1000)));
            }

            //When the timer is finished onFinish is called
            //It writes to the database that the player started
            //And passes extras such as the game object
            //and player 1 and 2's colors picked by local user
            public void onFinish() {
                if (getActivity() != null) {
                    if (game.isAsync || (game.player1.playerReady && game.player2.playerReady)) {
                        boolean isPlayer1 = (game.player1.playerId.equals(GoogleSignIn.getLastSignedInAccount(getActivity()).getId()));
                        if (isPlayer1) {
                            game.player1.playerStarted = true;
                            game.writeToDatabase("player1", "playerStarted");
                        } else {
                            game.player2.playerStarted = true;
                            game.writeToDatabase("player2", "playerStarted");
                        }
                        Intent intent = new Intent(getActivity(), RaceActivity.class);
                        intent.putExtra("localPlayerColor", localColor);
                        intent.putExtra("onlinePlayerColor", onlineColor);
                        intent.putExtra("game", game);
                        startActivity(intent);
                    }
                }
            }
        }.start(); //Starts the countDown object to execute

    }

    //Creates a player 2 reference for listening
    public void initializePlayer2Ref() {
        // initialize reference listening to player 2 for the game object
        player2Ref = RunBuddyApplication.getDatabase().getReference("games").child(game.ID).child("player2");
        player2Listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // once a non-null value is retrieved, a second player has joined the game
                if (snapshot.exists()) {
                    // convert the retrieved data into a race player object
                    RacePlayer p2 = snapshot.getValue(RacePlayer.class);

                    // query the db for this joined player's name
                    User.getUserNameFromID(p2.playerId, new User.MyCallback() {
                        @Override
                        public void onCallback(String value) {
                            // set the player 2 name to be the retrieved value
                            player2tv.setText("Name: " + value);

                            // update the local game object given we now have a second player
                            game.joinAble = false;
                            game.player2 = p2;

                            // set initial ready text of other player
                            setTextColorForPlayer(player2ReadyText);

                            // remove event listener for whole player 2 object
                            player2Ref.removeEventListener(player2Listener);

                            // if game is synchronous, initialize listener for other player's ready field
                            if (!game.isAsync) {
                                initializeOtherPlayerReadyRef();
                            }
                            // else for asynchronous, initialize listener for other player's started field
                            else {
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
        // add listener to reference defined at beginning of function
        player2Ref.addValueEventListener(player2Listener);
    }


    public void initializeOtherPlayerReadyRef() {

        // initialize reference listening to other player's ready field. used in synchronous games only
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getActivity());
        String pId = acct.getId();
        if (pId.equals(game.player1.playerId)) {
            otherPlayerReadyRef = RunBuddyApplication.getDatabase().getReference("games")
                    .child(game.ID).child("player2").child("playerReady");
        } else {
            otherPlayerReadyRef = RunBuddyApplication.getDatabase().getReference("games")
                    .child(game.ID).child("player1").child("playerReady");
        }

        // initialize listener for other player ready field
        otherPlayerReadyListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // if change to field happens
                if (snapshot.exists()) {
                    // if player 1
                    if (pId.equals(game.player1.playerId)) {
                        // updated game object with new value in player ready field
                        game.player2.playerReady = snapshot.getValue(Boolean.class);
                        setTextColorForPlayer(player2ReadyText);
                        // if both players now ready, start the race
                        if (game.player1.playerReady && game.player2.playerReady) {
                            startRace(color1, color2);
                        }
                    }
                    // else, player 2
                    else {
                        // updated game object with new value in player ready field
                        game.player1.playerReady = snapshot.getValue(Boolean.class);
                        setTextColorForPlayer(player1ReadyText);
                        // if both players now ready, start the race
                        if (game.player1.playerReady && game.player2.playerReady) {
                            startRace(color2, color1);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        // add listener to reference defined at beginning of function
        otherPlayerReadyRef.addValueEventListener(otherPlayerReadyListener);
    }

    public void initializeOtherPlayerStartedRef() {

        // initialize reference listening to other player's started field. used in asynchronous games only
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getActivity());
        String pId = acct.getId();
        if (pId.equals(game.player1.playerId)) {
            otherPlayerStartedRef = RunBuddyApplication.getDatabase().getReference("games")
                    .child(game.ID).child("player2").child("playerStarted");
        } else {
            otherPlayerStartedRef = RunBuddyApplication.getDatabase().getReference("games")
                    .child(game.ID).child("player1").child("playerStarted");
        }

        // initialize listener for other started ready field
        otherPlayerStartedListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // if started field changed to true
                    if (snapshot.getValue(Boolean.class)) {
                        // update ui and game object depending on if the other player that started was player 1 or 2
                        if (pId.equals(game.player1.playerId)) {
                            game.player2.playerStarted = true;
                            setTextColorForPlayer(player2ReadyText);
                        } else {
                            game.player1.playerStarted = true;
                            setTextColorForPlayer(player1ReadyText);
                        }
                        // remove the event listener to stop listening for changes
                        otherPlayerStartedRef.removeEventListener(otherPlayerStartedListener);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        // add listener to reference defined at beginning of function
        otherPlayerStartedRef.addValueEventListener(otherPlayerStartedListener);
    }

    //helper function that gets the proper player ready/start text color
    public void setTextColorForPlayer(TextView tv) {
        String s = tv.getText().toString();
        if (game.isAsync) {
            if (s.substring(0, 8).equalsIgnoreCase("Player 1")) {
                if (game.player1.playerStarted) {
                    tv.setText("Player 1: Started");
                    tv.setTextColor(Color.GREEN);
                } else {
                    tv.setText("Player 1: Not Started");
                    tv.setTextColor(Color.WHITE);
                }
            } else {
                if (game.player2.playerStarted) {
                    tv.setText("Player 2: Started");
                    tv.setTextColor(Color.GREEN);
                } else {
                    tv.setText("Player 2: Not Started");
                    tv.setTextColor(Color.WHITE);
                }
            }
        } else {
            if (s.substring(0, 8).equalsIgnoreCase("Player 1")) {
                if (game.player1.playerReady) {
                    tv.setText("Player 1: Ready");
                    tv.setTextColor(Color.GREEN);
                } else {
                    tv.setText("Player 1: Not Ready");
                    tv.setTextColor(Color.WHITE);
                }
            } else {
                if (game.player2.playerReady) {
                    tv.setText("Player 2: Ready");
                    tv.setTextColor(Color.GREEN);
                } else {
                    tv.setText("Player 2: Not Ready");
                    tv.setTextColor(Color.WHITE);
                }
            }
        }
    }

    public void getGPSPermission() {
        //If permission is granted
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            gpsAccess = true;
        }
        //Request permission via helper function below
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_FINE_LOCATION);
            }
        }
    }

    //Requested for gps permissions
    //If permission is granted or has been granted carry on
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getGPSPermission();
                } else {
                    gpsAccess = false;
                }
                break;
        }
    }

    //Helper function for spinner
    //Makes it so we pass in the appropriate color
    //for the Race Activity's seekbar tracks
    //Matches with the strings of spinner to appropriate color
    public int colorValFinder(String c) {
        int colorPicked = Color.RED;
        if (c.equals("Blue")) {
            colorPicked = Color.parseColor("#46AEFF");
        } else if (c.equals("Red")) {
            colorPicked = Color.parseColor("#FF5161");
        } else if (c.equals("Green")) {
            colorPicked = Color.parseColor("#47FF6F");
        } else if (c.equals("Yellow")) {
            colorPicked = Color.parseColor("#FFFF48");
        } else if (c.equals("Orange")) {
            colorPicked = Color.parseColor("#FFA946");
        }
        return colorPicked;
    }

    // leaving the fragment, before leave, save those data and correct them
    @Override
    public void onDetach() {
        super.onDetach();
        // if game isnt null
        if (game != null) {
            // check if game is synchronous
            if (!game.isAsync) {
                GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getActivity());
                // check if logged in user is player 1 or 2 of the game
                if (acct.getId().equals(game.player1.playerId)) {
                    // if player 1 and has not started, make sure they are not labelled as ready
                    // and update db
                    if (!game.player1.playerStarted) {
                        game.player1.playerReady = false;
                        game.writeToDatabase("player1", "playerReady");
                    }
                } else {
                    // if player 2 and has not started, make sure they are not labelled as ready
                    // and update db
                    if (!game.player2.playerStarted) {
                        game.player2.playerReady = false;
                        game.writeToDatabase("player2", "playerReady");
                    }
                }
            }
        }
        // remove the event listener to stop listening for changes when fragment detaches
        if (otherPlayerReadyRef != null)
            otherPlayerReadyRef.removeEventListener(otherPlayerReadyListener);
        if (otherPlayerStartedRef != null)
            otherPlayerStartedRef.removeEventListener(otherPlayerStartedListener);
        if (player2Ref != null)
            player2Ref.removeEventListener(player2Listener);

        f = null;
    }
}

