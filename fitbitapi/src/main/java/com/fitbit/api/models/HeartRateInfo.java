package com.fitbit.api.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class HeartRateInfo {

    @SerializedName("dateTime")
    @Expose
    private String dateTime;

    @SerializedName("value")
    @Expose
    private HeartRateValue value;

    public HeartRateValue getValue() {
        return value;
    }

    public void setValue(HeartRateValue value) {
        this.value = value;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}
