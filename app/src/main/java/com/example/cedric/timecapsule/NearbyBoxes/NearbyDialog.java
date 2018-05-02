package com.example.cedric.timecapsule.NearbyBoxes;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.cedric.timecapsule.R;
import com.example.cedric.timecapsule.Utils.Utils;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class NearbyDialog extends Activity {

    FirebaseDatabase database;
    SharedPreferences prefs;
    DatabaseReference locationsRef;
    DatabaseReference usersRef;

    Utils u;
    android.support.v7.widget.Toolbar titleBar;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private ArrayList<PlaceTile> nearbyPlaceTiles = new ArrayList<>();
    private ArrayList<PlaceTile> userPlaceTiles = new ArrayList<>();
    private ImageButton boxesSwitcher;
    private String username;
    private boolean nearby = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.nearby_dialog_layout);

        mRecyclerView = findViewById(R.id.place_tile_recycler);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        titleBar = findViewById(R.id.my_toolbar);
        boxesSwitcher = findViewById(R.id.boxes_switcher);

        u = new Utils();

        username = u.getUsername(this);

        prefs = this.getSharedPreferences(
                "com.example.cedric.timecapsule", Context.MODE_PRIVATE);

        database = FirebaseDatabase.getInstance();
        locationsRef = database.getReference("locations");
        usersRef = database.getReference("users");

        nearbyPlaceTiles = getNearbyPlaceTiles();

        if (nearbyPlaceTiles != null) {
            nearbyPlaceTiles = sortTiles(nearbyPlaceTiles);

            setNearbyPlaceTileListeners();
            setAdapterAndUpdateData(nearbyPlaceTiles);
        }

        getUserBoxes();

        setBoxesSwitcherListener();
    }

    public void getUserBoxes() {
        usersRef.child(username).child("boxes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String boxKey = ds.getKey();
                    addUserBox(boxKey);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // work left
            }
        });
    }

    public void addUserBox(String key) {
        locationsRef.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String[] keyData = dataSnapshot.getKey().split("%");

                String numPhotos = (String) dataSnapshot.child("data").child("photos").getValue();
                if (numPhotos == null) {
                    numPhotos = "0";
                }

                String numComments = (String) dataSnapshot.child("data").child("comments").getValue();
                if (numComments == null) {
                    numComments = "0";

                }

                boolean isPrivate = false;
                if (dataSnapshot.child("creator").getValue() != null) {
                    isPrivate = true;
                }

                String timestamp = (String) dataSnapshot.child("data").child("timestamp").getValue();

                Double placeLat = (Double) dataSnapshot.child("l").child("0").getValue();
                Double placeLon = (Double) dataSnapshot.child("l").child("1").getValue();

                if (placeLat != null) {
                    LatLng placeDist = new LatLng(placeLat, placeLon);

                    LatLng userLocation = u.getLocation(NearbyDialog.this);

                    DecimalFormat df = new DecimalFormat();
                    df.setMaximumFractionDigits(2);

                    String distance = df.format(u.getDistance(placeDist, userLocation) / 1000) + " KM";


                    PlaceTile pt = new PlaceTile(keyData[2], keyData[0], distance, keyData[1], numPhotos, numComments, timestamp, key, isPrivate);

                    listenForAddedNumCommentsAndPhotos(pt);
                    userPlaceTiles.add(pt);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // work left
            }
        });
    }

    public void setBoxesSwitcherListener() {
        boxesSwitcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                if (!nearby) {
                    titleBar.setTitle("Nearby Boxes");
                    nearbyPlaceTiles = sortTiles(nearbyPlaceTiles);
                    setAdapterAndUpdateData(nearbyPlaceTiles);
                    nearby = true;
                } else {
                    titleBar.setTitle("My Boxes");
                    userPlaceTiles = sortTiles(userPlaceTiles);
                    setAdapterAndUpdateData(userPlaceTiles);
                    nearby = false;
                }

            }
        });
    }


    public void setNearbyPlaceTileListeners() {
        for (PlaceTile pt : nearbyPlaceTiles) {
            listenForAddedNumCommentsAndPhotos(pt);
        }
    }


    private void listenForAddedNumCommentsAndPhotos(PlaceTile pt) {

        locationsRef.child(pt.key).child("data").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String photos = (String) dataSnapshot.child("photos").getValue();
                String comments = (String) dataSnapshot.child("comments").getValue();
                String timestamp = (String) dataSnapshot.child("timestamp").getValue();
                String isPrivate = (String) dataSnapshot.child("isPrivate").getValue();

                boolean updated = false;
                if (photos != null && !photos.equals(pt.numPhotos) && !photos.equals("")) {
                    updated = true;
                    pt.numPhotos = photos;
                }
                if (comments != null && !comments.equals(pt.numComments) && !comments.equals("")) {
                    updated = true;
                    pt.numComments = comments;
                }
                if (timestamp != null && !timestamp.equals(pt.timestamp) && !timestamp.equals("")) {
                    updated = true;
                    pt.timestamp = timestamp;
                }

                if (isPrivate != null && !isPrivate.equals("")) {

                    if (pt.isPrivate == false) {
                        updated = true;
                        pt.isPrivate = true;

                    }
                }


                if (updated) {
                    u.savePlaceTiles(prefs, nearbyPlaceTiles);
                    setAdapterAndUpdateData(nearbyPlaceTiles);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // work left
            }
        });
    }


    public ArrayList<PlaceTile> getNearbyPlaceTiles() {
        SharedPreferences settings = this.getSharedPreferences(
                "com.example.cedric.timecapsule", Context.MODE_PRIVATE);

        List<PlaceTile> placeTiles;

        if (settings.contains("placeTiles")) {
            String jsonPlaceTile = settings.getString("placeTiles", "");

            Gson gson = new Gson();

            PlaceTile[] placeTileArray = gson.fromJson(jsonPlaceTile, PlaceTile[].class);

            placeTiles = Arrays.asList(placeTileArray);
            placeTiles = new ArrayList<>(placeTiles);

            return (ArrayList<PlaceTile>) placeTiles;

        } else {
            return new ArrayList<>();
        }
    }

    public ArrayList<PlaceTile> sortTiles(ArrayList<PlaceTile> pt) {
        Collections.sort(pt, new Comparator<PlaceTile>() {
            public int compare(PlaceTile p1, PlaceTile p2) {
                return p1.getDistance().compareTo(p2.getDistance());
            }
        });
        return pt;
    }


    private void setAdapterAndUpdateData(ArrayList<PlaceTile> pt) {
        mAdapter = new PlaceTileAdapter(this, pt);
        mRecyclerView.setAdapter(mAdapter);
        // scroll to the last
        // mRecyclerView.smoothScrollToPosition(mPlaceTiles.size() - 1);
        // scroll to top
        mRecyclerView.smoothScrollToPosition(0);
    }

}
