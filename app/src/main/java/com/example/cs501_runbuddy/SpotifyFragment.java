package com.example.cs501_runbuddy;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerContext;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;

import java.util.concurrent.TimeUnit;


public class SpotifyFragment extends Fragment {



    
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String CLIENT_ID = "9bd3a819fa964513bf26880dd8db490c";
    private static final String REDIRECT_URI = "http://localhost/";
    private SpotifyAppRemote mSpotifyAppRemote;
    private PlayerState playerState;
    private PlayerContext playerContext;


    TextView threadStopper;
    TextView threadStopperOriginal;
    Button pausePlay;
    Button play;
    Button nextButton;
    Button previousButton;
    Button repeatButton;
    Button shuffleButton;
    Button resume;
    Bundle bundle;
    ImageView songImage;
    ListView playListListView;
    TextView songDisplay;

    int shuffleColor = Color.BLACK;
    int repeatColor = Color.BLACK;
    int playPauseColor = Color.BLACK;

    int mode2Color = Color.rgb(0, 150, 0);

    SeekBar seeker;
    int seekBarFinalPos = 0;

    int check = 0;


    int threadCount = 0;

    boolean playing = true;
    Thread t;

    spotifyInterface spotInterface;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;




    public interface spotifyInterface{
        void spotifyNotOpen();
    }

    public SpotifyFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static SpotifyFragment newInstance(String param1, String param2) {
        SpotifyFragment fragment = new SpotifyFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.connect(getContext(), connectionParams,
                new Connector.ConnectionListener() {

                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        connected();

                    }

                    public void onFailure(Throwable throwable) {
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_spotify, container, false);

        bundle = savedInstanceState;
        
        threadStopper = (TextView) v.findViewById(R.id.threadStopTv);
        threadStopperOriginal = (TextView) v.findViewById(R.id.threadStopTv);


        pausePlay = (Button) v.findViewById(R.id.pause);
        seeker = (SeekBar) v.findViewById(R.id.seekBar);

        songImage = (ImageView) v.findViewById(R.id.imageView);

        shuffleButton = (Button) v.findViewById(R.id.shuffle);

        repeatButton = (Button) v.findViewById(R.id.repeat);

        nextButton = (Button) v.findViewById(R.id.next);
        nextButton.setBackgroundColor(Color.GREEN);

        previousButton = (Button) v.findViewById(R.id.previous);
        previousButton.setBackgroundColor(Color.GREEN);

        songDisplay = (TextView) v.findViewById(R.id.songNameTextView);

        t = new Thread(() -> {
            while(playing){
                try {
                    threadStopper.setText("");
                    seeker.setProgress(seeker.getProgress()+1);
                    TimeUnit.SECONDS.sleep(1);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                next();
            }
        });

        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prev();
            }
        });

        shuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shuffle();
            }
        });

        repeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                repeat();
            }
        });


        seeker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                seekBarFinalPos = i * 1000;

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                String s = Integer.toString(seekBarFinalPos);
                mSpotifyAppRemote.getPlayerApi().seekTo(seekBarFinalPos);
            }
        });

        pausePlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playPause();
            }
        });

        return v;
    }

    @Override
    public void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    public void playPause(){
        try {
            if (pausePlay.getText().toString().equals("Pause")) {
                mSpotifyAppRemote.getPlayerApi().pause();
                pausePlay.setText("Play");
                pausePlay.setBackgroundColor(Color.BLACK);
                threadStopper = null;
            } else {
                if(!t.isAlive()){
                    threadStopper = threadStopperOriginal;
                    t.start();
                }
                mSpotifyAppRemote.getPlayerApi().resume();
                pausePlay.setText("Pause");
                pausePlay.setBackgroundColor(Color.GREEN);
            }
        }catch(Exception e){

        }
    }

    public void shuffle(){
        try {
            if (shuffleColor == Color.GREEN) {
                shuffleColor = Color.BLACK;
                shuffleButton.setBackgroundColor(Color.BLACK);
            } else {
                shuffleColor = Color.GREEN;
                shuffleButton.setBackgroundColor(Color.GREEN);
            }
            mSpotifyAppRemote.getPlayerApi().toggleShuffle();
        }catch(Exception e){

        }
    }

    public void repeat() {
        try {
            if (repeatColor == mode2Color) {
                repeatColor = mode2Color;
                repeatButton.setBackgroundColor(Color.GREEN);
            } else if (repeatColor == Color.BLACK) {
                repeatColor = mode2Color;
                repeatButton.setBackgroundColor(mode2Color);
            } else {
                repeatColor = Color.BLACK;
                repeatButton.setBackgroundColor(Color.BLACK);
            }
            mSpotifyAppRemote.getPlayerApi().toggleRepeat();
        }catch (Exception e){

        }
    }

    public void next(){
        try {
            mSpotifyAppRemote.getPlayerApi().skipNext();
            mSpotifyAppRemote.getPlayerApi().resume();
            pausePlay.setText("Pause");
            pausePlay.setBackgroundColor(Color.GREEN);
        }catch (Exception e){

        }
    }

    public void prev(){
        try {
            mSpotifyAppRemote.getPlayerApi().skipPrevious();
        }catch(Exception e){
            //spotInterface.spotifyNotOpen();
        }
    }

    private void connected() {

        mSpotifyAppRemote.getPlayerApi().subscribeToPlayerState().setEventCallback(new Subscription.EventCallback<PlayerState>() {
            @Override
            public void onEvent(PlayerState playerState) {
                float length = playerState.track.duration;
                float position = playerState.playbackPosition;
                seeker.setMax((int)length/1000);
                seeker.setProgress((int)position/1000);
                final Track track = playerState.track;
                songDisplay.setText(track.name);
                if (track != null) {
                    mSpotifyAppRemote.getImagesApi().getImage(track.imageUri).setResultCallback(new CallResult.ResultCallback<Bitmap>() {
                        @Override
                        public void onResult(Bitmap bitmap) {
                            songImage.setImageBitmap(bitmap);
                        }
                    });

                }
                if(playerState.isPaused){
                    pausePlay.setText("Play");
                    pausePlay.setBackgroundColor(Color.BLACK);
                    playPauseColor = Color.BLACK;
                    threadStopper = null;
                    threadCount = 0;
                }else{
                    threadCount += 1;
                    if(threadCount == 1) {
                        threadStopper = threadStopperOriginal;
                        t.start();
                    }
                    pausePlay.setText("Pause");
                    pausePlay.setBackgroundColor(Color.GREEN);
                    playPauseColor = Color.GREEN;
                }


                if(playerState.playbackOptions.isShuffling){
                    shuffleButton.setBackgroundColor(Color.GREEN);
                    shuffleColor = Color.GREEN;
                }else{
                    shuffleButton.setBackgroundColor(Color.BLACK);
                    shuffleColor = Color.BLACK;
                }
                if(playerState.playbackOptions.repeatMode == 0){
                    repeatButton.setBackgroundColor(Color.BLACK);
                    repeatColor = Color.BLACK;
                }else if(playerState.playbackOptions.repeatMode == 2){
                    repeatButton.setBackgroundColor(mode2Color);
                    repeatColor = mode2Color;
                }else if(playerState.playbackOptions.repeatMode == 1) {
                    repeatButton.setBackgroundColor(Color.GREEN);
                    repeatColor = Color.GREEN;
                }

            }
        });

    }
}