package com.example.cs501_runbuddy.models;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class RacePlayer implements Serializable {

    public String playerId; // Player name
    public HashMap<String, RaceLocation> playerLocation;//distance that player One has been completed during the time
    public Boolean playerFinished;//If true player reached finish line
    public Boolean playerReady;//If true player is ready for sync race
    public Boolean playerStarted; // if they clicked the start button from lobby fragment
    public Double totalDistanceRan;
    public Double totalTimeRan;
    public Long playerStartTime;
    public Double heartRate;

    public RacePlayer(){

    }

    public RacePlayer(String playerId, HashMap<String, RaceLocation> playerLocation, Boolean playerFinished, Boolean playerReady,
                      Boolean playerStarted, Double totalDistanceRan, Double totalTimeRan, Long playerStartTime, Double heartRate){
        this.playerId = playerId;
        this.playerLocation = playerLocation;
        this.playerFinished = playerFinished;
        this.playerStarted = playerStarted;
        this.playerStartTime = playerStartTime;
        this.playerReady = playerReady;
        this.totalDistanceRan = totalDistanceRan;
        this.totalTimeRan = totalTimeRan;
        this.heartRate = heartRate;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("playerId", playerId);
        result.put("playerLocation", playerLocation);
        result.put("playerFinished", playerFinished);
        result.put("playerReady", playerReady);
        result.put("playerStarted", playerStarted);
        result.put("totalDistanceRan", totalDistanceRan);
        result.put("totalTimeRan", totalTimeRan);
        result.put("playerStartTime", playerStartTime);
        result.put("heartRate", heartRate);
        return result;
    }


}
