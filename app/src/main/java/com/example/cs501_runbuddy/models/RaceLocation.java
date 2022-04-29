package com.example.cs501_runbuddy.models;

import android.os.Build;

import java.io.Serializable;
import java.time.Instant;

// class combines custom location object with a timestamp
public class RaceLocation implements Serializable {

    public LatLngDB latLng; // location object containing coordinate
    public double time; // timestamp of gps reading

    // default constructor, needed for firebase db to work when reading data
    public RaceLocation(){
        latLng = new LatLngDB(0.0, 0.0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Instant now = Instant.now();
            time = now.toEpochMilli();
        }
    }

    // constructor that takes all necessary fields
    public RaceLocation(LatLngDB latLng, double time){
        this.time = time;
        this.latLng = latLng;
    }

    // comparison function that allows us to sort locations based on their timestamps
    public int compareTo(RaceLocation r){
        if(this.time > r.time){
            return 1;
        }else if(this.time < r.time){
            return -1;
        }else{
            return 0;
        }
    }
}
