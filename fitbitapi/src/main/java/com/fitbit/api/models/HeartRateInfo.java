package com.fitbit.api.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class HeartRateInfo {

    @SerializedName("dateTime")
    @Expose
    private String dateTime;

    @SerializedName("customHeartRateZones")
    @Expose
    private List<Object> customHeartRateZones = new ArrayList<Object>();

    @SerializedName("heartRateZones")
    @Expose
    private List<Object> heartRateZones = new ArrayList<Object>();

    @SerializedName("value")
    @Expose
    private Double value;

    public List<Object> getCustomHeartRateZones() {
        return customHeartRateZones;
    }

    public List<Object> getHeartRateZones() {
        return heartRateZones;
    }

    public Double getValue() {
        return value;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setCustomHeartRateZones(List<Object> customHeartRateZones) {
        this.customHeartRateZones = customHeartRateZones;
    }

    public void setHeartRateZones(List<Object> heartRateZones) {
        this.heartRateZones = heartRateZones;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}
