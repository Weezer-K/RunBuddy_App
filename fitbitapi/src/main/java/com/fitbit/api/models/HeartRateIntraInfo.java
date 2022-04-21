package com.fitbit.api.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class HeartRateIntraInfo {

    @SerializedName("dataset")
    @Expose
    private List<HeartRateData> dataset = new ArrayList<HeartRateData>();

    @SerializedName("datasetInterval")
    @Expose
    private Integer datasetInterval;

    @SerializedName("datasetType")
    @Expose
    private String datasetType;


    public List<HeartRateData> getDataset() {
        return dataset;
    }

    public void setDataset(List<HeartRateData> dataset) {
        this.dataset = dataset;
    }

    public Integer getDatasetInterval() {
        return datasetInterval;
    }

    public void setDatasetInterval(Integer datasetInterval) {
        this.datasetInterval = datasetInterval;
    }

    public String getDatasetType() {
        return datasetType;
    }

    public void setDatasetType(String datasetType) {
        this.datasetType = datasetType;
    }
}
