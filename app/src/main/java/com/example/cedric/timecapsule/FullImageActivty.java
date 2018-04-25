package com.example.cedric.timecapsule;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class FullImageActivty extends AppCompatActivity {

    private ImageView capturedImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image_activty);

        capturedImg = findViewById(R.id.capturedImg);
    }


}
