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

public class HomeActivity extends AppCompatActivity implements CreateFragment.CreateGame, SearchFragment.SearchGame, MyRacesFragment.BackToLobby, LobbyFragment.fragmentListener{

    private SearchFragment SearchFragment;
    private CreateFragment CreateFragment;
    private LobbyFragment LobbyFragment;
    private MyRacesFragment MyRacesFragment;
    private PublicGameListFragment PublicGameListFragment;
    private FragmentManager fm;
    public ArrayList<Double> distFilters;
    private GoogleSignInClient mGoogleSignInClient;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        SearchFragment = new SearchFragment();
        CreateFragment = new CreateFragment();
        LobbyFragment = new LobbyFragment();
        MyRacesFragment = new MyRacesFragment();
        PublicGameListFragment = new PublicGameListFragment();


        distFilters = new ArrayList<Double>();
        fm = getSupportFragmentManager();

        fm.beginTransaction().replace(R.id.homeFragment, MyRacesFragment).commitNow();

        bottomNavigationView = findViewById(R.id.theMenu);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menu_create:
                        fm.beginTransaction().replace(R.id.homeFragment, CreateFragment).commitNow();
                        return true;
                    case R.id.menu_history:
                        fm.beginTransaction().replace(R.id.homeFragment, MyRacesFragment).commitNow();
                        return true;
                    case R.id.menu_search:
                        fm.beginTransaction().replace(R.id.homeFragment, SearchFragment).commitNow();
                        return true;
                    case R.id.menu_logout:

                        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                        builder.setCancelable(true);
                        builder.setTitle(Html.fromHtml("<font color='#00203F'>Logout?</font>"));
                        builder.setMessage(Html.fromHtml("<font color='#00203F'>Are you sure you want to logout?</font>"));
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
                        builder.setNegativeButton(Html.fromHtml("<font color='#00203F'>No</font>"), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                        AlertDialog dialog = builder.create();
                        dialog.show();
                        return false;

                }
                return false;
            }
        });
    }

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

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("fragment", "");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void startGame(String ID, boolean isPrivate, boolean isAsync, int totalDistance) {
        fm.beginTransaction().replace(R.id.homeFragment, LobbyFragment).commitNow();
        LobbyFragment.createGame(ID, isPrivate, isAsync, totalDistance);
    }


    @Override
    public void searchGame(ArrayList<Double> d) {
        fm.beginTransaction().replace(R.id.homeFragment, PublicGameListFragment).commitNow();
        PublicGameListFragment.searchGame(d);
    }


    @Override
    public void joinGame(Game game) {
        fm.beginTransaction().replace(R.id.homeFragment, LobbyFragment).commitNow();
        LobbyFragment.joinGame(game);
    }

    public void onClick(View view){
        CheckBox c = (CheckBox) view;
        c.isChecked();
        String temp = c.getText().toString();
        if(!c.isChecked()) {
            if (temp.equals("1 mile")) {
                distFilters.add(1.0);
            } else if (temp.equals("5 miles")) {
                distFilters.add(5.0);
            } else if (temp.equals("10 miles")) {
                distFilters.add(10.0);
            }
        }else{
            if (temp.equals("1 mile")) {
                distFilters.remove(1.0);
            } else if (temp.equals("5 miles")) {
                distFilters.remove(5.0);
            } else if (temp.equals("10 miles")) {
                distFilters.remove(10.0);
            }
        }


    }

    @Override
    public void backGame(Game game) {
        fm.beginTransaction().replace(R.id.homeFragment, LobbyFragment).commitNow();
        LobbyFragment.rejoinGame(game);
    }

    @Override
    public AudioManager getAudioManager() {
        return (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }


}