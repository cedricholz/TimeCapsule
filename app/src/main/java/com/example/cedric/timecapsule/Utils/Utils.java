package com.example.cedric.timecapsule.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import com.example.cedric.timecapsule.NearbyBoxes.Place;
import com.example.cedric.timecapsule.NearbyBoxes.PlaceTile;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;


public class Utils {
    // All markers within this range in Kilometers will be visible
    private double visibleMarkerDistancekm = 100.0;
    // A box can be accessed when you are within this distance in meters.
    private double validDistanceMeters = 50;
    // New boxes are not reloaded every time we get a new location, we wait until we have
    // moved this far from our original position before updating
    private double validDistanceToRequestNewPinsKm = 5.0;
    // Markers cannot be created too close together, this is the acceptable
    // distance between them.
    private double validDistanceFromMarkerForNewMarkerKm = .07;
    // The amount of characters you can put into a comment
    private int maxCommentLength = 300;
    private int maxMessageLength = 300;

    private double distanceChangeToMoveCamera = 20;

    public Utils() {
    }

    public static String getHashedPassword(String passwordToHash) {
        String generatedPassword = null;
        byte[] salt = {-92, 83, -14, -80, 44, 64, 96, -13, 98, 102, 46, 34, -108, -40, 100, 3};
        try {
            // Create MessageDigest instance for MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
            //Add password bytes to digest
            md.update(salt);
            //Get the hash's bytes
            byte[] bytes = md.digest(passwordToHash.getBytes());
            //This bytes[] has bytes in decimal format;
            //Convert it to hexadecimal format
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            //Get complete hashed password in hex format
            generatedPassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return generatedPassword;
    }

    public double getDistanceChangeToMoveCamera() {
        return distanceChangeToMoveCamera;
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

    public String getUsername(Context c) {
        SharedPreferences prefs = c.getSharedPreferences(
                "com.example.cedric.timecapsule", Context.MODE_PRIVATE);
        return prefs.getString("username", "Default");
    }

    public String getPassword(Context c) {
        SharedPreferences prefs = c.getSharedPreferences(
                "com.example.cedric.timecapsule", Context.MODE_PRIVATE);
        return prefs.getString("password", "Default");
    }

    public String getEmail(Context c) {
        SharedPreferences prefs = c.getSharedPreferences(
                "com.example.cedric.timecapsule", Context.MODE_PRIVATE);
        return prefs.getString("email", "Default");
    }

    public void setUsername(Context c, String username) {
        SharedPreferences prefs = c.getSharedPreferences(
                "com.example.cedric.timecapsule", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("username", username).commit();
    }

    public void setPassword(Context c, String password) {
        SharedPreferences prefs = c.getSharedPreferences(
                "com.example.cedric.timecapsule", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("password", password).commit();
    }

    public void setEmail(Context c, String email) {
        SharedPreferences prefs = c.getSharedPreferences(
                "com.example.cedric.timecapsule", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("email", email).commit();
    }


    public void savePlaceTiles(SharedPreferences prefs, ArrayList<PlaceTile> placeTiles) {
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String jsonPlaceTiles = gson.toJson(placeTiles);
        editor.putString("placeTiles", jsonPlaceTiles).commit();
    }

    public void addBearLandmarksToFirebase(Context c) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("locations");
        GeoFire geoFire = new GeoFire(myRef);


        String jsonString = loadJSONFromAsset("bear_statues.json", c);
        Place[] landmarks = new Gson().fromJson(jsonString, Place[].class);

        for (Place lm : landmarks) {
            String[] coordinatesSplit = lm.getCoordinates().split(",");
            Double lat = Double.parseDouble(coordinatesSplit[0]);
            Double lon = Double.parseDouble(coordinatesSplit[1]);

            geoFire.setLocation(lm.getLandmark_name() + "%University Of California, Berkeley%" + lm.getFilename(), new GeoLocation(lat, lon), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {
                    if (error != null) {
                        System.err.println("There was an error saving the location to GeoFire: " + error);
                    } else {
                        System.out.println("Location saved on server successfully!");
                    }
                }
            });
        }
    }


}
