package com.fitbit.api.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class HeartRateContainer {

    @SerializedName("activities-heart")
    @Expose
    private List<HeartRateInfo> activitiesHeart = new ArrayList<HeartRateInfo>();

    @SerializedName("activities-heart-intraday")
    @Expose
    private HeartRateIntraInfo activitiesHeartIntraday;

    public List<HeartRateInfo> getActivitiesHeart() {
        return activitiesHeart;
    }

    public void setActivitiesHeart(List<HeartRateInfo> activitiesHeart) {
        this.activitiesHeart = activitiesHeart;
    }

    public HeartRateIntraInfo getActivitiesHeartIntraday() {
        return activitiesHeartIntraday;
    }

    public void setActivitiesHeartIntraday(HeartRateIntraInfo activitiesHeartIntraday) {
        this.activitiesHeartIntraday = activitiesHeartIntraday;
    }
}
