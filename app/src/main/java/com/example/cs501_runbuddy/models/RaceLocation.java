package com.example.cs501_runbuddy.models;

import android.os.Build;

import java.io.Serializable;
import java.time.Instant;


public class RaceLocation implements Serializable {

    public LatLngDB latLng;
    public double time;

    public RaceLocation(LatLngDB latLng, double time){
        this.time = time;
        this.latLng = latLng;
    }




    public RaceLocation(){
        latLng = new LatLngDB(0.0, 0.0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Instant now = Instant.now();
            time = now.toEpochMilli();
        }
    }





}
