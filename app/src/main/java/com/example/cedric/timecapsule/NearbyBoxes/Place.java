package com.example.cedric.timecapsule.NearbyBoxes;

public class Place {

    public String getLandmark_name() {
        return landmark_name;
    }


    public String getCoordinates() {
        return coordinates;
    }


    public String getFilename() {
        return filename;
    }


    private String landmark_name;
    private String coordinates;
    private String filename;

    public Place(String landmark_name, String coordinates, String filename){
        this.landmark_name = landmark_name;
        this.coordinates = coordinates;
        this.filename = filename;
    }
}
