package com.example.cedric.timecapsule.NearbyBoxes;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cedric.timecapsule.Comments.CommentDialog;
import com.example.cedric.timecapsule.R;
import com.example.cedric.timecapsule.Utils.Utils;

import java.util.ArrayList;

public class PlaceTileAdapter extends RecyclerView.Adapter {

    public ArrayList<PlaceTile> mPlaceTile;
    private Context mContext;

    public PlaceTileAdapter(Context context, ArrayList<PlaceTile> PlaceTile) {
        mContext = context;
        mPlaceTile = PlaceTile;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // here, we specify what kind of view each cell should have. In our case, all of them will have a view
        // made from comment_cell_layout
        View view = LayoutInflater.from(mContext).inflate(R.layout.place_tile_cell_layout, parent, false);
        return new PlaceTileViewHolder(view, mPlaceTile);
    }

    // - get element from your dataset at this position
    // - replace the contents of the view with that element
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // here, we the comment that should be displayed at index `position` in our recycler view
        // everytime the recycler view is refreshed, this method is called getItemCount() times (because
        // it needs to recreate every cell).
        PlaceTile placeTile = mPlaceTile.get(position);
        ((PlaceTileViewHolder) holder).bind(placeTile);

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mPlaceTile.size();
    }
}

class PlaceTileViewHolder extends RecyclerView.ViewHolder {

    // each data item is just a string in this case
    public RelativeLayout mPlaceBubbleLayout;
    public ImageButton mPlaceImage;
    public TextView mPlaceTileName;
    public TextView mPLaceTileDistance;
    public ArrayList<PlaceTile> placeTiles;

    Utils u;
    private long mLastClickTime = 0;

    public PlaceTileViewHolder(final View itemView, final ArrayList<PlaceTile> placeTiles) {
        super(itemView);
        mPlaceBubbleLayout = itemView.findViewById(R.id.place_tile_cell_relative_layout);
        mPlaceImage = mPlaceBubbleLayout.findViewById(R.id.place_image_view);
        mPlaceTileName = mPlaceBubbleLayout.findViewById(R.id.place_tile_name);
        mPLaceTileDistance = mPlaceBubbleLayout.findViewById(R.id.place_tile_distance);

        this.placeTiles = placeTiles;
        u = new Utils();

        mPlaceImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (SystemClock.elapsedRealtime() - mLastClickTime > 1000){
                    PlaceTile recyclerTile = placeTiles.get(getAdapterPosition());
                    Double distance = Double.parseDouble(recyclerTile.distance.split(" ")[0]);

                    if (distance < u.getValidDistanceKm()) {
                        Intent createBoxIntent = new Intent(itemView.getContext(), CommentDialog.class);
                        createBoxIntent.putExtra("boxName", recyclerTile.placeName);
                        createBoxIntent.putExtra("address", recyclerTile.address);
                        createBoxIntent.putExtra("imageName", recyclerTile.imageName);
                        itemView.getContext().startActivity(createBoxIntent);
                    } else {
                        Toast.makeText(itemView.getContext(), "You Are Too Far Away To Access This Box...", Toast.LENGTH_SHORT).show();
                    }
                }
                mLastClickTime = SystemClock.elapsedRealtime();
            }
        });
    }

    void bind(PlaceTile placeTile) {
        mPlaceTileName.setText(placeTile.placeName);
        mPLaceTileDistance.setText(placeTile.distance);
        Context context = mPlaceImage.getContext();

        String imageName = placeTile.imageName;

        int imageId = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
        mPlaceImage.setImageResource(imageId);
    }

}