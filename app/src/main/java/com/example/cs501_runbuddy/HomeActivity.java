package com.example.cs501_runbuddy;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.example.cs501_runbuddy.models.Game;
import com.fitbit.authentication.AuthenticationManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;

//The HomeActivity that holds all the fragments including create, search, my races and logout
public class HomeActivity extends AppCompatActivity implements CreateFragment.CreateGame, SearchFragment.SearchGame, MyRacesFragment.BackToLobby, LobbyFragment.fragmentListener{

    private SearchFragment SearchFragment;// Instantiate SearchFragment
    private CreateFragment CreateFragment;// Instantiate CreateFragment
    private LobbyFragment LobbyFragment;// Instantiate LobbyFragment
    private MyRacesFragment MyRacesFragment;// Instantiate MyRacesFragment
    private PublicGameListFragment PublicGameListFragment;// Instantiate PublicGameListFragment

    private FragmentManager fm;// Fragment Manager for fragment transaction, switching fragments

    public ArrayList<Double> distFilters;// Arraylist of Distance 1,5,10
    private GoogleSignInClient mGoogleSignInClient;// Google Sign in client for login and logout
    private BottomNavigationView bottomNavigationView;// Menu on the Bottom that user can click to switch fragment

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //Lock orientation to be only in portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        SearchFragment = new SearchFragment();// Instantiate SearchFragment
        CreateFragment = new CreateFragment();// Instantiate CreateFragment
        LobbyFragment = new LobbyFragment();// Instantiate LobbyFragment
        MyRacesFragment = new MyRacesFragment();// Instantiate MyRacesFragment
        PublicGameListFragment = new PublicGameListFragment();// Instantiate PublicGameListFragment

        distFilters = new ArrayList<Double>();// Instantiate Distance ArrayList

        // Instantiate Fragment Manager and by default show the MyRacesFragment first when user get to this activity when login
        fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.homeFragment, MyRacesFragment).commitNow();

        // Binding the BottomNavigationView with our menu
        bottomNavigationView = findViewById(R.id.theMenu);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menu_create:
                        fm.beginTransaction().replace(R.id.homeFragment, CreateFragment).commitNow();// Get to CreateFragment
                        return true;
                    case R.id.menu_history:
                        fm.beginTransaction().replace(R.id.homeFragment, MyRacesFragment).commitNow();// Get to MyRacesFragment
                        return true;
                    case R.id.menu_search:
                        fm.beginTransaction().replace(R.id.homeFragment, SearchFragment).commitNow();// Get to SearchFragment
                        return true;
                    case R.id.menu_logout:

                        // Ask for confirmation when logout is clicked, yes or no (below are the setting of the Alert)
                        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                        builder.setCancelable(true);
                        builder.setTitle(Html.fromHtml("<font color='#00203F'>Logout?</font>"));
                        builder.setMessage(Html.fromHtml("<font color='#00203F'>Are you sure you want to logout?</font>"));

                        // Logout when yes is clicked
                        builder.setPositiveButton(Html.fromHtml("<font color='#00203F'>Yes</font>"),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mGoogleSignInClient = GoogleSignIn.getClient(HomeActivity.this,RunBuddyApplication.getGoogleSignInClient()) ;
                                        mGoogleSignInClient.signOut();
                                        if (AuthenticationManager.isLoggedIn())
                                            AuthenticationManager.logout(HomeActivity.this);
                                        Intent intent = new Intent(HomeActivity.this, SignInActivity.class);
                                        startActivity(intent);
                                    }
                                });

                        // Do nothing when no is clicked
                        builder.setNegativeButton(Html.fromHtml("<font color='#00203F'>No</font>"), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        return false;// Always return false so the menuItem "logout" will never be active (highlighted), the active menuItem will always be the previous active one
                }
                return false;
            }
        });
    }

    // Reacquire Bundle information of which fragment you were in before you live
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String frag = savedInstanceState.getString("fragment");
        switch (frag) {
            case "Create":
                fm.beginTransaction().replace(R.id.homeFragment, CreateFragment).commitNow();
            case "Search":
                fm.beginTransaction().replace(R.id.homeFragment, SearchFragment).commitNow();
            default:
                fm.beginTransaction().replace(R.id.homeFragment, MyRacesFragment).commitNow();
        }
    }

    // Save Bundle information of which fragment you are in right now
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("fragment", "");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() { }

    // go to the LobbyFragment and start the game with information given from CreateFragment
    @Override
    public void startGame(String ID, boolean isPrivate, boolean isAsync, int totalDistance) {
        fm.beginTransaction().replace(R.id.homeFragment, LobbyFragment).commitNow();
        LobbyFragment.createGame(ID, isPrivate, isAsync, totalDistance);
    }

    // go to the PublicGameListFragment and display list of games with information given from the SearchFragment
    @Override
    public void searchGame(ArrayList<Double> d) {
        fm.beginTransaction().replace(R.id.homeFragment, PublicGameListFragment).commitNow();
        PublicGameListFragment.searchGame(d);
    }

    // go to the LobbyFragment with information about the private game given from SearchFragment
    @Override
    public void joinGame(Game game) {
        fm.beginTransaction().replace(R.id.homeFragment, LobbyFragment).commitNow();
        LobbyFragment.joinGame(game);
    }

    // Go back to the LobbyFragment, with the information of the game given by MyRacesFragment
    @Override
    public void backGame(Game game) {
        fm.beginTransaction().replace(R.id.homeFragment, LobbyFragment).commitNow();
        LobbyFragment.rejoinGame(game);
    }

    // When user get to HomeActivity, play a short welcome audio
    @Override
    public AudioManager getAudioManager() { return (AudioManager) getSystemService(Context.AUDIO_SERVICE); }
}