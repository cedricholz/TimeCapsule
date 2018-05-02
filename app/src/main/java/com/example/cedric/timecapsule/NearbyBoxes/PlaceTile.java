package com.example.cedric.timecapsule.NearbyBoxes;

import java.io.Serializable;
import java.text.SimpleDateFormat;


public class PlaceTile implements Serializable {
    public String imageName;
    public String placeName;

    public String numPhotos;
    public String numComments;
    public String distance;
    public String address;
    public String key;

    public String timestamp;

    public PlaceTile(String imageName, String placeName, String distance, String address, String numPhotos, String numComments, String timestamp, String key) {
        this.imageName = imageName;
        this.placeName = placeName;
        this.distance = distance;
        this.address = address;

        this.numPhotos = numPhotos;
        this.numComments = numComments;

        this.timestamp = timestamp;

        this.key = key;
    }

    public String getDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/mm/yyyy");
        long timeLong = Long.parseLong(timestamp);
        return "Added: " + formatter.format(timeLong);
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }
}
