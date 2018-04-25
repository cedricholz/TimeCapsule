package com.example.cedric.timecapsule;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.cedric.timecapsule.UserInformation.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

public class CommentDialog extends Activity {
    private static final int CAMERA_REQUEST_CODE = 1;
    public String myLastPost = "";
    android.support.v7.widget.Toolbar titleBar;
    FirebaseDatabase database;
    FirebaseStorage storage;
    DatabaseReference myRef;
    StorageReference storageRef;
    String refKey = "locations";
    Utils u;
    ProgressDialog mProgress;
    Uri photoURI;
    String mCurrentPhotoPath;
    private EditText textField;
    private ImageButton sendButton;
    private ImageButton cameraButton;
    private ImageButton photoGalleryButton;
    private String username = "";
    private String key = "";
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private ArrayList<Comment> mComments = new ArrayList<>();
    private HashMap<String, Comment> commentHashMap = new HashMap<>();
    private int commentLevel = 1;
    private int maxCommentLength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_comment_dialog);

        titleBar = findViewById(R.id.my_toolbar);

        Intent thisIntent = getIntent();
        Bundle intentExtras = thisIntent.getExtras();

        mRecyclerView = findViewById(R.id.comment_recycler);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mRecyclerView.setNestedScrollingEnabled(false);

        username = getUsername();

        u = new Utils();

        maxCommentLength = u.getMaxCommentLength();

        if (intentExtras != null) {
            titleBar.setTitle((String) intentExtras.get("boxName"));

            titleBar.setSubtitle((String) intentExtras.get("address"));
            key = intentExtras.get("boxName") + "%" + intentExtras.get("address") + "%" + intentExtras.get("imageName");

            refKey = "locations/" + key + "/messages/";
        }

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        myRef = database.getReference("locations");
        storageRef = storage.getReference();

        textField = findViewById(R.id.comment_input_edit_text);
        sendButton = findViewById(R.id.send_button);
        cameraButton = findViewById(R.id.camera_button);
        photoGalleryButton = findViewById(R.id.photo_gallery);

        setSendButtonListener();
        setCameraButtonListener();
        setPhotoGalleryButtonListener();

        getComments();

        mProgress = new ProgressDialog(this);

    }

    public void setSendButtonListener() {
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String text = textField.getText().toString();
                if (text.length() < 1) {
                    textField.requestFocus();
                } else {

                    if (text.length() <= maxCommentLength) {

                        textField.setText("");

                        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        mgr.hideSoftInputFromWindow(textField.getWindowToken(), 0);

                        postNewComment(text, "");
                    } else {
                        Toast.makeText(CommentDialog.this, "Comment cannot be larger than 300 characters", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    public void setCameraButtonListener() {
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                takePictureIntent();
            }
        });
    }

    public void setPhotoGalleryButtonListener() {
        photoGalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(CommentDialog.this, GalleryActivity.class);
                startActivity(i);
            }
        });
    }

    public void takePictureIntent() {
        Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (pictureIntent.resolveActivity(getPackageManager()) != null) {

            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();

            } catch (IOException ex) {
                // failed while creating the file
                System.out.println("Failed to create Image");
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider", photoFile);
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(pictureIntent, CAMERA_REQUEST_CODE);
            }

        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);


        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        mCurrentPhotoPath = image.getAbsolutePath();

        return image;
    }

    public static void resizeImage(String sPath,String sTo) throws IOException {

        Bitmap photo = BitmapFactory.decodeFile(sPath);
        photo = Bitmap.createScaledBitmap(photo, 300, 300, false);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        File f = new File(sTo);
        f.createNewFile();
        FileOutputStream fo = new FileOutputStream(f);
        fo.write(bytes.toByteArray());
        fo.close();

        File file =  new File(sPath);
        file.delete();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {

            // user clicked "post" button from Preview Activity
            if (data != null && data.getStringExtra("user_permission") != null) {
                mProgress.setMessage("Uploading...");
                mProgress.show();

//                try {
//                    resizeImage(mCurrentPhotoPath, mCurrentPhotoPath);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

                Uri uri = photoURI;

                final String caption = (String) data.getStringExtra("caption");

                final StorageReference filepath = storageRef.child("Photos").child(uri.getLastPathSegment());
                filepath.putFile(photoURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(CommentDialog.this, "Upload Successful!", Toast.LENGTH_SHORT).show();
                        mProgress.dismiss();

                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String downloadURL = uri.toString();
                                postImage(caption, downloadURL);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle any errors
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CommentDialog.this, "Upload Failed!", Toast.LENGTH_SHORT).show();
                        mProgress.dismiss();
                    }
                });

            } else { // Takes user to the Preview Activity
                Intent i = new Intent(CommentDialog.this, PreviewActivity.class);



                i.putExtra("image", mCurrentPhotoPath);

                startActivityForResult(i, 1);
            }
        }
    }

    public void postImage(String caption, String downloadURL) {
        if (caption.length() <= maxCommentLength) {
            postNewComment(caption, downloadURL);
        } else {
            Toast.makeText(CommentDialog.this, "Comment cannot be larger than 300 characters", Toast.LENGTH_SHORT).show();
        }
    }

    public static String encodeURL(String string) {
        return string.replace(".", ",");
    }

    public static String deocodeURL(String string) {
        return string.replace(",", ".");
    }

    public ArrayList<Comment> sortComments(ArrayList<Comment> comments) {
        Collections.sort(comments, new Comparator<Comment>() {
            public int compare(Comment c1, Comment c2) {
                return c2.upVotes.compareTo(c1.upVotes);
            }
        });

        return comments;
    }

    private String getUsername() {
        SharedPreferences prefs = this.getSharedPreferences(
                "com.example.cedric.timecapsule", Context.MODE_PRIVATE);
        return prefs.getString("username", "Default");
    }

    private void getNewChildData(String commentKey) {
        myRef.child(key).child("messages").child(commentKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String u = (String) dataSnapshot.child("user").getValue();
                String m = (String) dataSnapshot.child("my_message").getValue();
                String votes = (String) dataSnapshot.child("upVotes").getValue();
                String replies = (String) dataSnapshot.child("replies").getValue();
                String photoUrl = (String) dataSnapshot.child("photoURL").getValue();

                String timeStamp = dataSnapshot.getKey();


                Comment c = new Comment(m, u, timeStamp, votes, key, false, replies,
                        refKey, commentLevel, photoUrl);

                if (votes != null) {
                    mComments.add(c);

                    mComments = sortComments(mComments);

                    commentHashMap.put(timeStamp + m, c);

                    setAdapterAndUpdateData();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // work left
            }
        });
    }


    private void getComments() {

        myRef.child(key).child("messages").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {


                String u = (String) dataSnapshot.child("user").getValue();
                String m = (String) dataSnapshot.child("my_message").getValue();
                String votes = (String) dataSnapshot.child("upVotes").getValue();
                String replies = (String) dataSnapshot.child("replies").getValue();
                String photoUrl = (String) dataSnapshot.child("photoURL").getValue();

                if (m == null) {
                    getNewChildData(dataSnapshot.getKey());
                }

                if (m != null) {
                    String timestamp = dataSnapshot.getKey();


                    Comment c = new Comment(m, u, timestamp, votes, key, false, replies,
                            refKey, commentLevel, photoUrl);

                    mComments.add(c);

                    commentHashMap.put(timestamp + m, c);

                    mComments = sortComments(mComments);

                    setAdapterAndUpdateData();
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {

                String timestamp = dataSnapshot.getKey();
                String message = (String) dataSnapshot.child("my_message").getValue();
                Comment c = commentHashMap.get(timestamp + message);
                if (c != null && message != null && dataSnapshot.child("upVotes") != null && dataSnapshot.child("replies") != null) {
                    c.upVotes = (String) dataSnapshot.child("upVotes").getValue();
                    c.replies = (String) dataSnapshot.child("replies").getValue();
                    mComments = sortComments(mComments);
                    setAdapterAndUpdateData();
                }
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
        mAdapter = new CommentAdapter(this, mComments);
        mRecyclerView.setAdapter(mAdapter);

        // scroll to the first comment
//        mRecyclerView.smoothScrollToPosition(0);
    }


    private void postNewComment(String commentText, String photoURL) {

        String timeStamp = Long.toString(System.currentTimeMillis());

        myRef.child(key).child("Photo Gallery").child(timeStamp).setValue(photoURL);

        myRef.child(key).child("messages").child(timeStamp).child("user").setValue(username);
        myRef.child(key).child("messages").child(timeStamp).child("my_message").setValue(commentText);
        myRef.child(key).child("messages").child(timeStamp).child("upVotes").setValue("1");
        myRef.child(key).child("messages").child(timeStamp).child("replies").setValue("0");
        myRef.child(key).child("messages").child(timeStamp).child("photoURL").setValue(photoURL);


//        myLastPost = username + commentText;
//
//        String replies = "0";
//
//        Comment newComment = new Comment(commentText, username, curDate, "1", key, false,
//                replies, refKey, commentLevel, photoURL);
//
//        mComments.add(newComment);
//        mComments = sortComments(mComments);
//
//        commentHashMap.put(curDate.toString() + commentText, newComment);
//        setAdapterAndUpdateData();


    }
}