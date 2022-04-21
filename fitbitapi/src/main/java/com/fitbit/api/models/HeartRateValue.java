package com.fitbit.api.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class HeartRateValue {

    @SerializedName("customHeartRateZones")
    @Expose
    private List<Object> customHeartRateZones = new ArrayList<Object>();

    @SerializedName("heartRateZones")
    @Expose
    private List<Object> heartRateZones = new ArrayList<Object>();

    @SerializedName("restingHeartRate")
    @Expose
    private Integer restingHeartRate;

    public List<Object> getCustomHeartRateZones() {
        return customHeartRateZones;
    }

    public List<Object> getHeartRateZones() {
        return heartRateZones;
    }

    public Integer getRestingHeartRate() {
        return restingHeartRate;
    }

    public void setCustomHeartRateZones(List<Object> customHeartRateZones) {
        this.customHeartRateZones = customHeartRateZones;
    }

    public void setHeartRateZones(List<Object> heartRateZones) {
        this.heartRateZones = heartRateZones;
    }

    public void setRestingHeartRate(Integer restingHeartRate) {
        this.restingHeartRate = restingHeartRate;
    }
}
