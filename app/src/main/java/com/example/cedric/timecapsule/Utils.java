package com.example.cedric.timecapsule;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.InputStream;

public class Utils {
    Utils(){}

    private double visibleMarkerDistancekm = 100.0;
    private double validDistanceMeters = 10;

    private double validDistanceToRequestNewPinsKm = 5;


    public double getVisibleMarkerDistancekm(){
        return visibleMarkerDistancekm;
    }

    public double getValidDistanceMeters(){
     return validDistanceMeters;
    }

    public double getValidDistanceKm(){
        return validDistanceMeters/10;
    }

    public double getValidDistanceToRequestNewPinsKm(){
        return validDistanceToRequestNewPinsKm;
    }



    public double getDistance(LatLng x, LatLng y) {
        if (x == null || y == null){
            return 0;
        }
        Location loc1 = new Location("");
        loc1.setLatitude(x.latitude);
        loc1.setLongitude(x.longitude);

        Location loc2 = new Location("");
        loc2.setLatitude(y.latitude);
        loc2.setLongitude(y.longitude);

        return loc1.distanceTo(loc2);
    }

    public String loadJSONFromAsset(String filename, Context c) {
        String json = null;
        try {
            InputStream is = c.getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

}
