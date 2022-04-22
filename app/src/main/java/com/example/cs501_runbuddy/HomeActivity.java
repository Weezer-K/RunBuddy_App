package com.example.cs501_runbuddy;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.example.cs501_runbuddy.models.Game;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity implements CreateFragment.CreateGame, SearchFragment.SearchGame, MyRacesFragment.BackToLobby{

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
                        mGoogleSignInClient = GoogleSignIn.getClient(HomeActivity.this,RunBuddyApplication.getGoogleSignInClient()) ;
                        mGoogleSignInClient.signOut();
                        Intent intent = new Intent(HomeActivity.this, SignInActivity.class);
                        startActivity(intent);
                        return true;
                }
                return false;
            }
        });
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if (acct != null) {
            String personName = acct.getDisplayName();
            String personGivenName = acct.getGivenName();
            String personFamilyName = acct.getFamilyName();
            String personEmail = acct.getEmail();
            String personId = acct.getId();
            Uri personPhoto = acct.getPhotoUrl();
        }
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
}