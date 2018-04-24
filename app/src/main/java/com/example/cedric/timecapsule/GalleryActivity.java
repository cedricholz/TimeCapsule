package com.example.cedric.timecapsule;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Window;

import java.util.ArrayList;

public class GalleryActivity extends Activity {

    private final Integer image_ids[] = {
        R.drawable.bell_bears,
        R.drawable.bench_bears,
        R.drawable.oski_bear,
        R.drawable.strawberry_creek,
        R.drawable.bench_bears,
        R.drawable.bell_bears,
        R.drawable.bench_bears,
        R.drawable.oski_bear,
        R.drawable.strawberry_creek,
        R.drawable.bench_bears
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_gallery);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.images_recycler);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 3);
        recyclerView.setLayoutManager(layoutManager);

        ArrayList<ImageCell> imageCells = prepareData();
        GalleryAdapter adapter = new GalleryAdapter(getApplicationContext(), imageCells);
        recyclerView.setAdapter(adapter);
    }

    private ArrayList<ImageCell> prepareData() {
        ArrayList<ImageCell> theimage = new ArrayList<>();
        for (int i = 0; i < image_ids.length; i++) {
            ImageCell imagecell = new ImageCell();
            imagecell.setImg(image_ids[i]);
            theimage.add(imagecell);
        }

        return theimage;
    }
}
