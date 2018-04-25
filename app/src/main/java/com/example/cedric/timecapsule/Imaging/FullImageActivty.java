package com.example.cedric.timecapsule.Imaging;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.example.cedric.timecapsule.R;
import com.squareup.picasso.Picasso;

public class FullImageActivty extends AppCompatActivity {

    private ImageView capturedImg;
    private String downloadURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image_activty);

        capturedImg = findViewById(R.id.capturedImg);

        // retrieving downloadURL
        Intent i = getIntent();
        Bundle intentExtras = i.getExtras();

        if(intentExtras !=null) {
            downloadURL =(String) intentExtras.get("image");
        }

        Picasso.get().load(downloadURL).into(capturedImg);
    }


}
