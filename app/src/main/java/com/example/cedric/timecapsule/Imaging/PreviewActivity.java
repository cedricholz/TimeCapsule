package com.example.cedric.timecapsule.Imaging;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.cedric.timecapsule.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;

// Allows user to preview the image/video one last time before posting

public class PreviewActivity extends AppCompatActivity {

    private FirebaseStorage storage;
    private StorageReference storageRef;
    private ImageView capturedImage;
    private ImageButton postBtn;
    private EditText caption;
    private String mCurrentPath;
    private Bitmap thumb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        postBtn = findViewById(R.id.btnPost);
        capturedImage = findViewById(R.id.capturedImg);
        caption = findViewById(R.id.comment_input_edit_text);

        Intent i = getIntent();
        Bundle intentExtras = i.getExtras();

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        if (intentExtras != null) {
            mCurrentPath = (String) intentExtras.get("image");
            Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPath);
            if (bitmap.getHeight() > bitmap.getWidth()) { // the photo was taken portrait
                capturedImage.setScaleType(ImageView.ScaleType.FIT_XY);
            }
            capturedImage.setImageBitmap(bitmap);


            Float width = new Float(bitmap.getWidth());
            Float height = new Float(bitmap.getHeight());
            Float ratio = width / height;

            int scaledDimensions = 128;
            thumb = Bitmap.createScaledBitmap(bitmap, (int) (scaledDimensions * ratio), scaledDimensions, false);

        }

        setPostButtonListener();
    }


    public void setPostButtonListener() {
        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent();
                intent.putExtra("user_permission", "true");
                intent.putExtra("caption", caption.getText().toString());
                intent.putExtra("thumbnail", thumb);

                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}
