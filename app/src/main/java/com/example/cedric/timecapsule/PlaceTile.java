package com.example.cedric.timecapsule;

import java.io.Serializable;
import java.util.Date;

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

    PlaceTile(String imageName, String placeName, String distance, String address) {
        this.imageName = imageName;
        this.placeName = placeName;
        this.distance = distance;
        this.address = address;
    }
}
