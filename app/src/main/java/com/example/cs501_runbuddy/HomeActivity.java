package com.example.cs501_runbuddy;

import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.example.cs501_runbuddy.models.Game;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity implements CreateFragment.CreateGame, SearchFragment.SearchGame{

    private SearchFragment SearchFragment;
    private CreateFragment CreateFragment;
    private LobbyFragment LobbyFragment;
    private HistoryFragment HistoryFragment;
    private PublicGameListFragment PublicGameListFragment;
    private FragmentManager fm;
    public ArrayList<Double> distFilters;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        SearchFragment = new SearchFragment();
        CreateFragment = new CreateFragment();
        LobbyFragment = new LobbyFragment();
        HistoryFragment = new HistoryFragment();
        PublicGameListFragment = new PublicGameListFragment();

        distFilters = new ArrayList<Double>();
        fm = getSupportFragmentManager();

        fm.beginTransaction().replace(R.id.homeFragment, SearchFragment).commitNow();

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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.menu_create) {
            fm.beginTransaction().replace(R.id.homeFragment, CreateFragment).commitNow();
            return true;
        }

        if (id == R.id.menu_search) {
            fm.beginTransaction().replace(R.id.homeFragment, SearchFragment).commitNow();
            return true;
        }

        if (id == R.id.menu_history) {
            fm.beginTransaction().replace(R.id.homeFragment, HistoryFragment).commitNow();
            return true;
        }

        return super.onOptionsItemSelected(item);  //if none of the above are true, do the default and return a boolean.
    }

    @Override
    public void startGame(String ID, boolean isPrivate, int totalDistance) {
        fm.beginTransaction().replace(R.id.homeFragment, LobbyFragment).commitNow();
        LobbyFragment.createGame(ID,isPrivate,totalDistance);
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
}