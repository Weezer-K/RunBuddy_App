package com.example.cs501_runbuddy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class HomeActivity extends AppCompatActivity implements CreateFragment.CreateGame, SearchFragment.SearchGame{

    private SearchFragment SearchFragment;
    private CreateFragment CreateFragment;
    private LobbyFragment LobbyFragment;
    private HistoryFragment HistoryFragment;
    private PublicGameListFragment PublicGameListFragment;
    private FragmentManager fm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        SearchFragment = new SearchFragment();
        CreateFragment = new CreateFragment();
        LobbyFragment = new LobbyFragment();
        HistoryFragment = new HistoryFragment();
        PublicGameListFragment = new PublicGameListFragment();

        fm = getSupportFragmentManager();

        fm.beginTransaction().replace(R.id.homeFragment, SearchFragment).commitNow();

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
    public void startGame(String ID, boolean type, int totalDistance) {
        fm.beginTransaction().replace(R.id.homeFragment, LobbyFragment).commitNow();
        LobbyFragment.createGame(ID,type,totalDistance);
    }


    @Override
    public void searchGame() {
        fm.beginTransaction().replace(R.id.homeFragment, PublicGameListFragment).commitNow();
    }
}