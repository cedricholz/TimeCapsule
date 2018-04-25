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

import com.example.cedric.timecapsule.R;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class NearbyDialog extends Activity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private ArrayList<PlaceTile> mPlaceTiles = new ArrayList<>();
    private Button refreshButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.nearby_dialog_layout);

        mRecyclerView = findViewById(R.id.place_tile_recycler);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        setRefreshListener();

        mPlaceTiles = getmPlaceTiles();

        if (mPlaceTiles != null) {
            mPlaceTiles = sortTiles(mPlaceTiles);
            setAdapterAndUpdateData();
        }
    }

    public void setRefreshListener() {
        refreshButton = findViewById(R.id.refresh);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mPlaceTiles = sortTiles(getmPlaceTiles());
                setAdapterAndUpdateData();
            }
        });
    }

    public ArrayList<PlaceTile> getmPlaceTiles() {
            SharedPreferences settings = this.getSharedPreferences(
                    "com.example.cedric.timecapsule", Context.MODE_PRIVATE);

            List<PlaceTile> placeTiles;

            if (settings.contains("placeTiles")) {
                String jsonPlaceTile = settings.getString("placeTiles", "");

                Gson gson = new Gson();

                PlaceTile[] placeTileArray = gson.fromJson(jsonPlaceTile, PlaceTile[].class);

                placeTiles = Arrays.asList(placeTileArray);
                placeTiles = new ArrayList<>(placeTiles);

                return  (ArrayList<PlaceTile>) placeTiles;

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


    private void setAdapterAndUpdateData() {
        mAdapter = new PlaceTileAdapter(this, mPlaceTiles);
        mRecyclerView.setAdapter(mAdapter);
        // scroll to the last
        // mRecyclerView.smoothScrollToPosition(mPlaceTiles.size() - 1);
        // scroll to top
        mRecyclerView.smoothScrollToPosition(0);
    }

}
