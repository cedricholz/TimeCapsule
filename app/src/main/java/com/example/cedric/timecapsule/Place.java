package com.example.cedric.timecapsule;

public class Place {

    public String getLandmark_name() {
        return landmark_name;
    }

    public void setLandmark_name(String landmark_name) {
        this.landmark_name = landmark_name;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
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
