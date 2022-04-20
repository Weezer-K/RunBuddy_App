package com.example.cs501_runbuddy.models;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class RacePlayer implements Serializable {

    public String playerId; // Player name
    public HashMap<String, RaceLocation> playerLocation;//distance that player One has been completed during the time
    public Boolean playerFinished;//If true player reached finish line

    public Boolean playerStarted; // if they clicked the start button from lobby fragment
    public Double totalDistanceRan;
    public Double totalTimeRan;

    public RacePlayer(){

    }

    public RacePlayer(String playerId, HashMap<String, RaceLocation> playerLocation, Boolean playerFinished,
                      Boolean playerStarted, Double totalDistanceRan, Double totalTimeRan){
        this.playerId = playerId;
        this.playerLocation = playerLocation;
        this.playerFinished = playerFinished;
        this.playerStarted = playerStarted;
        this.totalDistanceRan = totalDistanceRan;
        this.totalTimeRan = totalTimeRan;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("playerId", playerId);
        result.put("playerLocation", playerLocation);
        result.put("playerFinished", playerFinished);
        result.put("playerStarted", playerStarted);
        result.put("totalDistanceRan", totalDistanceRan);
        result.put("totalTimeRan", totalTimeRan);
        return result;
    }


}
