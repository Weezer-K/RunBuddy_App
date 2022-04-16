package com.example.cs501_runbuddy.models;

import android.os.Build;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.time.Instant;

public class RaceLocation implements Serializable {

    private LatLng latLng;
    private double time;

    public RaceLocation(LatLng latLng, double time){
        this.time = time;
        this.latLng = latLng;
    }

    public RaceLocation(){
        latLng = new LatLng(0, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Instant now = Instant.now();
            time = now.toEpochMilli();
        }
    }

    public LatLng getLatLng(){
        return latLng;
    }

    public double getTime(){
        return time;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public void setTime(double time) {
        this.time = time;
    }
}
