package com.example.cs501_runbuddy;

import java.util.ArrayList;

public class Game {

    //private or public
    private String type;

    //Game Lobby ID
    private String ID;

    //Is the game ready to be searchable
    private Boolean joinable;

    //mile for this game
    private int totalDistance;

    //distance that has been completed during the time
    private ArrayList<Double> playerLocation;


    //default initiate, which should never be called
    public Game(){

    }

    //Starting Game with id and type
    public Game(String ID, String type, int totalDistance){

        this.type = type;
        this.ID = ID;
        this.joinable = false;
        this.totalDistance = totalDistance;
        this.playerLocation = new ArrayList<Double>(){};

    }

    public void setJoinable(boolean x){
        this.joinable = x;
    }

}
