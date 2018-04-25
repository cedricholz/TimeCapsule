package com.example.cedric.timecapsule.NearbyBoxes;

import java.io.Serializable;

public class PlaceTile implements Serializable {
    public String imageName;
    public String placeName;

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String distance;

    public String address;

    public PlaceTile(String imageName, String placeName, String distance, String address) {
        this.imageName = imageName;
        this.placeName = placeName;
        this.distance = distance;
        this.address = address;
    }
}
