package com.example.cs501_runbuddy.models;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RacePlayer implements Serializable {

    public String playerId; // Player name
    public ArrayList<RaceLocation> playerLocation;//distance that player One has been completed during the time
    public Boolean playerFinished;//If true player reached finish line

    public Boolean playerStarted; // if they clicked the start button from lobby fragment



    public RacePlayer(){

    }

    public RacePlayer(String playerId, ArrayList<RaceLocation> playerLocation, Boolean playerFinished, Boolean playerStarted){
        this.playerId = playerId;
        this.playerLocation = playerLocation;
        this.playerFinished = playerFinished;
        this.playerStarted = playerStarted;

    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("playerId", playerId);
        result.put("playerLocation", playerLocation);
        result.put("playerFinished", playerFinished);
        result.put("playerStarted", playerStarted);
        return result;
    }


}
