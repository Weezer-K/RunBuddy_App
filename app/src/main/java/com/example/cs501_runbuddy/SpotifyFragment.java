package com.example.cs501_runbuddy;

import static com.example.cs501_runbuddy.R.drawable.repeat_off;
import static com.example.cs501_runbuddy.R.drawable.repeat_playlist;
import static com.example.cs501_runbuddy.R.drawable.repeat_song;
import static com.example.cs501_runbuddy.R.drawable.shuffle_activated;
import static com.example.cs501_runbuddy.R.drawable.shuffle_off;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;

import java.util.concurrent.TimeUnit;


public class SpotifyFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String CLIENT_ID = "9bd3a819fa964513bf26880dd8db490c";
    private static final String REDIRECT_URI = "http://localhost/";
    private SpotifyAppRemote mSpotifyAppRemote;
    TextView threadStopper;
    TextView threadStopperOriginal;
    ImageButton pausePlay;
    ImageButton nextButton;
    ImageButton previousButton;
    ImageButton repeatButton;
    ImageButton shuffleButton;
    Bundle bundle;
    ImageView songImage;
    TextView songDisplay;
    TextView backgroundColor;
    TextView spotifyError;
    int spotifyBackgroundColor = Color.rgb(24, 24, 24);
    private boolean isPlaying;
    SeekBar seeker;
    int seekBarFinalPos = 0;
    int threadCount = 0;
    boolean playing = true;
    Thread t;





    public interface spotifyInterface{
        void spotifyNotOpen();
    }

    public SpotifyFragment() {
        // Required empty public constructor
    }


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

        }

    }


    //When spotfiy fragment first starts it checks
    //If your phone is connected to the spotify app or not
    //if not you will get a message that you need to download
    //If not you will be able to use spotify
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

                    //If user is connected then initalize UI
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        enableViews();
                        connected();

                    }

                    //If not connected show error message for user
                    public void onFailure(Throwable throwable) {
                        disableViews();
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_spotify, container, false);

        bundle = savedInstanceState;

        threadStopper = (TextView) v.findViewById(R.id.threadStopTv);//TextView used to stop thread

        threadStopperOriginal = (TextView) v.findViewById(R.id.threadStopTv);//Used to reset threadStopper


        pausePlay = (ImageButton) v.findViewById(R.id.playImageButton);
        seeker = (SeekBar) v.findViewById(R.id.seekBar);

        songImage = (ImageView) v.findViewById(R.id.imageView);

        shuffleButton = (ImageButton) v.findViewById(R.id.shuffleImageButton);
        shuffleButton.setBackgroundColor(spotifyBackgroundColor);

        repeatButton = (ImageButton) v.findViewById(R.id.repeatImageButton);
        repeatButton.setBackgroundColor(spotifyBackgroundColor);

        nextButton = (ImageButton) v.findViewById(R.id.skipImageButton);
        nextButton.setBackgroundColor(spotifyBackgroundColor);

        previousButton = (ImageButton) v.findViewById(R.id.previousImageButton);
        previousButton.setBackgroundColor(spotifyBackgroundColor);

        songDisplay = (TextView) v.findViewById(R.id.songNameTextView);

        backgroundColor = (TextView) v.findViewById(R.id.backgroundOfSpotify);
        backgroundColor.setBackgroundColor(spotifyBackgroundColor);
        spotifyError = (TextView) v.findViewById(R.id.spotifyErrorMessage);

        isPlaying = false;

        //Thread used to keep progress bar progressing in play mode
        t = new Thread(() -> {
            while(playing){
                try {
                    threadStopper.setText("");//Purpose is so this while loop breaks when
                                              //threadStopper is set to null
                    seeker.setProgress(seeker.getProgress()+1);
                    TimeUnit.SECONDS.sleep(1);//Every second we progress the seek bar 1 unit or 1 second

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //Used to skip to next song
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                next();
            }
        });

        //Used to go to previous song
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

        //Seekbar that is used to comb through song
        seeker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //Used to know how many ms in the song
                //We want to travel to
                seekBarFinalPos = i * 1000;//1 postion = 1000ms

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Accesses the media player and sets the postion to be where the
                //Seek bar is
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

    //Stops the media player when app stops
    @Override
    public void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    //helper function for when not connected to spotify
    //Makes only error message visible
    private void disableViews(){
        seeker.setVisibility(View.INVISIBLE);
        songImage.setVisibility(View.INVISIBLE);
        pausePlay.setVisibility(View.INVISIBLE);
        songDisplay.setVisibility(View.INVISIBLE);
        previousButton.setVisibility(View.INVISIBLE);
        nextButton.setVisibility(View.INVISIBLE);
        repeatButton.setVisibility(View.INVISIBLE);
        shuffleButton.setVisibility(View.INVISIBLE);
        spotifyError.setVisibility(View.VISIBLE);


    }

    //Enabled everything but error message
    //This is called when connected
    private void enableViews(){
        seeker.setVisibility(View.VISIBLE);
        songImage.setVisibility(View.VISIBLE);
        pausePlay.setVisibility(View.VISIBLE);
        songDisplay.setVisibility(View.VISIBLE);
        previousButton.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.VISIBLE);
        repeatButton.setVisibility(View.VISIBLE);
        shuffleButton.setVisibility(View.VISIBLE);
        spotifyError.setVisibility(View.INVISIBLE);
    }

    //Used to determine start/stop seekbar, play, pause audio.
    public void playPause(){
        try {
            if (isPlaying) {
                mSpotifyAppRemote.getPlayerApi().pause();
                pausePlay.setImageResource(R.drawable.play);
                pausePlay.setBackgroundColor(spotifyBackgroundColor);
                threadStopper = null;
                isPlaying = false;
            } else {
                if(!t.isAlive()){
                    threadStopper = threadStopperOriginal;
                    t.start();
                }
                mSpotifyAppRemote.getPlayerApi().resume();
                pausePlay.setImageResource(R.drawable.playing);
                pausePlay.setBackgroundColor(spotifyBackgroundColor);
                isPlaying = true;

            }
        }catch(Exception e){

        }
    }
    //Enable/disable shuffle mode and set image resourece
    public void shuffle(){
        try {
            if (shuffleButton.getImageMatrix().equals(shuffle_activated)) {
                shuffleButton.setImageResource(shuffle_off);
            } else {
                shuffleButton.setImageResource(shuffle_activated);
            }
            mSpotifyAppRemote.getPlayerApi().toggleShuffle();
        }catch(Exception e){

        }
    }

    //Set approprite repeatMode image
    public void repeat() {
        try {
            if (repeatButton.getImageMatrix().equals(repeat_off)) {
                repeatButton.setImageResource(repeat_playlist);
            } else if (repeatButton.getImageMatrix().equals(repeat_playlist)) {
                repeatButton.setImageResource(repeat_song);
            } else {
                repeatButton.setImageResource(repeat_off);
            }
            mSpotifyAppRemote.getPlayerApi().toggleRepeat();
        }catch (Exception e){

        }
    }

    //Skips to next song and plays
    public void next(){
        try {
            mSpotifyAppRemote.getPlayerApi().skipNext();
            mSpotifyAppRemote.getPlayerApi().resume();
            pausePlay.setImageResource(R.drawable.playing);
        }catch (Exception e){

        }
    }

    //goes to previous song
    public void prev(){
        try {
            mSpotifyAppRemote.getPlayerApi().skipPrevious();
        }catch(Exception e){
            //spotInterface.spotifyNotOpen();
        }
    }


    //Function called once a connection is made with
    //the spotify app on the device
    private void connected() {


        //Event listener for spotify app
        //Will update when a significant event happens
        //Such as the song position changing or a play mode is changed
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
                //Following conditionals set image resources
                //And UI intilization
                //Based off of spotify media player current state
                if(playerState.isPaused){
                    pausePlay.setImageResource(R.drawable.play);
                    pausePlay.setBackgroundColor(spotifyBackgroundColor);
                    isPlaying = false;
                    threadStopper = null; //In order to stop the thread by creating an error
                    threadCount = 0;//Since the thread crashes but is caught we reset thread count
                }else{
                    threadCount += 1;
                    if(threadCount == 1) { //Called when playerState is play and no threads are executed
                        threadStopper = threadStopperOriginal;
                        t.start();
                    }
                    isPlaying = true;
                    pausePlay.setImageResource(R.drawable.playing);
                    pausePlay.setBackgroundColor(spotifyBackgroundColor);
                }

                //Deals with setting shuffle and repeat buttons states
                if(playerState.playbackOptions.isShuffling){
                    shuffleButton.setImageResource(shuffle_activated);
                }else{
                    shuffleButton.setImageResource(shuffle_off);
                }


                // 0 is off
                // 1 is repeat song
                // 2 is repeat playlist
                if(playerState.playbackOptions.repeatMode == 0){
                    repeatButton.setImageResource(repeat_off);
                }else if(playerState.playbackOptions.repeatMode == 2){
                    repeatButton.setImageResource(repeat_playlist);
                }else if(playerState.playbackOptions.repeatMode == 1) {
                    repeatButton.setImageResource(repeat_song);
                }

            }
        });

    }
}