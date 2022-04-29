package com.example.cs501_runbuddy.models;

import java.io.Serializable;

// custom location object used to facilitate races
public class LatLngDB implements Serializable {

    // location attributes
    public double lat;
    public double lng;

    // default constructor, needed for firebase db to work when reading data
    public LatLngDB(){

    }

    // constructor that takes all necessary fields
    public LatLngDB(Double lat, Double lng){
        this.lat = lat;
        this.lng = lng;
    }
}
