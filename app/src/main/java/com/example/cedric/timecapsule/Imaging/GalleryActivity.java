package com.example.cedric.timecapsule.Imaging;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;

import com.example.cedric.timecapsule.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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

    private String key = "";
    private ArrayList<ImageCell> images = new ArrayList<ImageCell>();
    private ImageButton chatButton;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private DatabaseReference databaseReference;
    private StorageReference storageRef;
    private RecyclerView.Adapter iAdapter;
    private RecyclerView iRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_gallery);

        iRecyclerView = (RecyclerView) findViewById(R.id.images_recycler);
        iRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 3);
        iRecyclerView.setLayoutManager(layoutManager);

        // firebase setup
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        databaseReference = database.getReference("locations");
        storageRef = storage.getReference();

        // retrieving key from comment dialog activity
        Intent i = getIntent();
        Bundle intentExtras = i.getExtras();

        if(intentExtras !=null) {
            key =(String) intentExtras.get("key");
        }

        chatButton = findViewById(R.id.chat_button);

        setChatButtonListener();

        getPhotos();
    }

    public void setChatButtonListener() {
        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                onBackPressed();
            }
        });
    }

    public void getPhotos() {
        databaseReference.child(key).child("Photo Gallery").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {

                String downloadURL = (String) dataSnapshot.getValue();
                ImageCell ic = new ImageCell();
                ic.setImg(downloadURL);
                images.add(ic);
                setAdapterAndUpdateData();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void setAdapterAndUpdateData() {
        // create a new adapter with the updated mComments array
        // this will "refresh" our recycler view
        iAdapter = new GalleryAdapter(this, images);
        iRecyclerView.setAdapter(iAdapter);
    }

}
