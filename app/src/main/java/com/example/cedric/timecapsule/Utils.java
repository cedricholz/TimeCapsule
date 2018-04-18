package com.example.cedric.timecapsule;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.InputStream;

public class Utils {
    // All markers within this range in Kilometers will be visible
    private double visibleMarkerDistancekm = 100.0;
    // A box can be accessed when you are within this distance in meters.
    private double validDistanceMeters = 10.0;
    // New boxes are not reloaded every time we get a new location, we wait until we have
    // moved this far from our original position before updating
    private double validDistanceToRequestNewPinsKm = 5.0;
    // Markers cannot be created too close together, this is the acceptable
    // distance between them.
    private double validDistanceFromMarkerForNewMarkerKm = .1;
    // The amount of characters you can put into a comment
    private int maxCommentLength = 300;
    private int maxMessageLength = 300;

    Utils() {
    }

    public String getUsername(Context c) {
        SharedPreferences prefs = c.getSharedPreferences(
                "com.example.cedric.timecapsule", Context.MODE_PRIVATE);
        return prefs.getString("username", "Default");
    }

    public int getMaxMessageLength() {
        return maxMessageLength;
    }

    public int getMaxCommentLength() {
        return maxCommentLength;
    }

    public double getVisibleMarkerDistancekm() {
        return visibleMarkerDistancekm;
    }

    public double getValidDistanceMeters() {
        return validDistanceMeters;
    }

    public double getValidDistanceKm() {
        return validDistanceMeters / 1000;
    }

    public double getValidDistanceToRequestNewPinsKm() {
        return validDistanceToRequestNewPinsKm;
    }

    public double getValidDistanceFromMarkerForNewMarkerKm() {
        return validDistanceFromMarkerForNewMarkerKm;
    }

    // Gets the distance between two markers.
    public double getDistance(LatLng x, LatLng y) {
        if (x == null || y == null) {
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
