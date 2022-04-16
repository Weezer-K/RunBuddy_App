package com.example.cs501_runbuddy.models;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RacePlayer implements Serializable {

    public String playerId; // Player name
    public ArrayList<RaceLocation> playerLocation;//distance that player One has been completed during the time
    public Boolean isPlayerFinished;//If true player finished


    public RacePlayer(){

    }

    public RacePlayer(String playerId, ArrayList<RaceLocation> playerLocation, Boolean isPlayerFinished){
        this.playerId = playerId;
        this.playerLocation = playerLocation;
        this.isPlayerFinished = isPlayerFinished;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("playerId", playerId);
        result.put("playerLocation", playerLocation);
        result.put("isPlayerFinished", isPlayerFinished);
        return result;
    }


}
