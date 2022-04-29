package com.example.cs501_runbuddy.models;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class RacePlayer implements Serializable {

    public String playerId; // player ID
    public HashMap<String, RaceLocation> playerLocation; // location data with respective timestamps
    public Boolean playerFinished; // has the player finished their race
    public Boolean playerReady; // has the player readied up for the race
    public Boolean playerStarted; // has the player started their race
    public Double totalDistanceRan; // distance the player ran
    public Double totalTimeRan; // time the player been running for
    public Long playerStartTime; // player start time for the race
    public Double heartRate; // average heart rate for the finished race for this player

    // default constructor, needed for firebase db to work when reading data
    public RacePlayer(){

    }

    // constructor that takes all necessary fields
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

    // toMap function converts RacePlayer object into HashMap<String, Object> with each attribute
    // hashmap of string keys and object values is the accepted format to write to the database
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
