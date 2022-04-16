package com.example.cs501_runbuddy.models;

import java.io.Serializable;

public class LatLngDB implements Serializable {

    public double lat;
    public double lng;

    public LatLngDB(){

    }

    public LatLngDB(Double lat, Double lng){
        this.lat = lat;
        this.lng = lng;
    }
}
