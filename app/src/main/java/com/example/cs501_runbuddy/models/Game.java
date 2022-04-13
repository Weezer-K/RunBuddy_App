package com.example.cs501_runbuddy.models;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Game {

    public String ID;//Game Lobby ID
    public Boolean type;//private or public
    public Boolean joinAble;//Is the game ready to be searchable
    public Double totalDistance;//mile for this game


    public String playerOne; // Player one name
    public List<Double> playerOneLocation;//distance that player One has been completed during the time
    public String playerTwo; // PLayer two name
    public List<Double> playerTwoLocation;//distance that player Two has been completed during the time



    //default initiate, which should never be called
    public Game(){

    }

    //Starting Game with id and type
    public Game(String ID,
                Boolean type,
                Double totalDistance,
                Boolean joinAble,
                String playerOne,
                String playerTwo,
                List<Double> playerOneLocation,
                List<Double> playerTwoLocation){

        this.type = type;
        this.ID = ID;
        this.joinAble = joinAble;
        this.totalDistance = totalDistance;
        this.playerOne = playerOne;
        this.playerTwo = playerTwo;
        this.playerOneLocation = playerOneLocation;
        this.playerTwoLocation = playerTwoLocation;

    }

    public void setJoinable(boolean x){
        this.joinAble = x;
    }

    @Exclude
    public Map<String, Object> toMap() {

        HashMap<String, Object> result = new HashMap<>();
        result.put("ID", ID);
        result.put("type", type);
        result.put("joinAble", joinAble);
        result.put("totalDistance", totalDistance);
        result.put("playerOne", playerOne);
        result.put("playerOne", playerTwo);
        result.put("playerOneLocation", playerOneLocation);
        result.put("playerTwoLocation", playerTwoLocation);

        return result;
    }
}
