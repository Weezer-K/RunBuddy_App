package com.example.cs501_runbuddy.models;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Game {

    //Game Lobby ID
    public String ID;

    //private or public
    public Boolean type;

    //Is the game ready to be searchable
    public Boolean joinable;

    //mile for this game
    public Double totalDistance;

    //distance that has been completed during the time
    public List<Double> playerLocation;


    //default initiate, which should never be called
    public Game(){

    }

    //Starting Game with id and type
    public Game(String ID, Boolean type, Double totalDistance, Boolean joinable, List<Double> playerLocation){

        this.type = type;
        this.ID = ID;
        this.joinable = joinable;
        this.totalDistance = totalDistance;
        this.playerLocation = playerLocation;

    }

    public void setJoinable(boolean x){
        this.joinable = x;
    }

    @Exclude
    public Map<String, Object> toMap() {

        HashMap<String, Object> result = new HashMap<>();
        result.put("ID", ID);
        result.put("type", type);
        result.put("joinable", joinable);
        result.put("totalDistance", totalDistance);
        result.put("playerLocation", playerLocation);

        return result;
    }
}
