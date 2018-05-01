package com.example.cedric.timecapsule.Comments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;

import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.cedric.timecapsule.Imaging.PreviewActivity;
import com.example.cedric.timecapsule.R;
import com.example.cedric.timecapsule.Utils.Utils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

public class InsideCommentDialog extends Activity {
    private static final int CAMERA_REQUEST_CODE = 1;
    public String myLastPost = "";
    android.support.v7.widget.Toolbar titleBar;
    FirebaseDatabase database;
    FirebaseStorage storage;

    DatabaseReference myRef;
    DatabaseReference headRef;
    StorageReference storageRef;

    Utils u;
    String headUsername;
    String headMessage;
    String headDate;
    String headReplies;

    String headRefString = "";

    String headVotes;
    String refKey = "locations";


    String mCurrentPhotoPath;
    private int maxCommentLength;

    private String highresUrl;
    private String thumbUrl;

    private EditText textField;
    private ImageButton sendButton;
    private ImageButton cameraButton;
    private String username = "";
    private String boxKey = "";
    private RecyclerView mCommentRecyclerView;
    private RecyclerView.Adapter mCommentAdapter;
    private ArrayList<Comment> mComments = new ArrayList<>();
    private HashMap<String, Comment> mCommentHashMap = new HashMap<>();
    private Uri photoURI;
    private ProgressDialog mProgress;

    private int commentLevel = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_inside_comment);

        titleBar = findViewById(R.id.my_toolbar);

        Intent thisIntent = getIntent();
        Bundle intentExtras = thisIntent.getExtras();

        mCommentRecyclerView = findViewById(R.id.inside_comment_recycler);
        mCommentRecyclerView.setHasFixedSize(true);
        mCommentRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        username = getUsername();

        u = new Utils();
        maxCommentLength = u.getMaxCommentLength();

        if (intentExtras != null) {

            headUsername = (String) intentExtras.get("headUsername");
            headMessage = (String) intentExtras.get("headMessage");
            headDate = (String) intentExtras.get("headDate");
            headReplies = (String) intentExtras.get("headReplies");

            headVotes = (String) intentExtras.get("headVotes");
            boxKey = (String) intentExtras.get("boxKey");

            highresUrl = (String) intentExtras.get("highresUrl");
            thumbUrl = (String) intentExtras.get("thumbUrl");

            refKey = "locations/" + boxKey + "/messages/" + headDate + "/commentMessages";
            headRefString = "locations/" + boxKey + "/messages/";

            Comment headComment = new Comment(headMessage, headUsername, headDate, headVotes, boxKey, true, headReplies, headRefString, commentLevel, highresUrl, thumbUrl);
            mComments.add(headComment);
            mCommentHashMap.put(headDate + headMessage, headComment);
            mProgress = new ProgressDialog(this);
        }

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        myRef = database.getReference(refKey);
        headRef = database.getReference(headRefString);
        storageRef = storage.getReference();

        textField = findViewById(R.id.inside_comment_input_edit_text);
        sendButton = findViewById(R.id.send_button);
        cameraButton = findViewById(R.id.camera_button);

        setSendButtonListener();
        setCameraButtonListener();

        setmCommentAdapter();

        getComments();

        getHeadCommentUpdates();

    }


    public void setSendButtonListener() {
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String text = textField.getText().toString();
                if (text.length() < 1) {
                    textField.requestFocus();
                } else {

                    if (text.length() <= u.getMaxCommentLength()) {

                        textField.setText("");

                        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        mgr.hideSoftInputFromWindow(textField.getWindowToken(), 0);

                        postNewComment(text, "", "");
                    } else {
                        Toast.makeText(InsideCommentDialog.this, "Comment cannot be larger than 300 characters", Toast.LENGTH_SHORT).show();
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


    public void storeThumbNail(Bitmap bitmap, String url, final String caption, final String highresUrl) {
        if (bitmap != null) {

            final StorageReference filepath = storageRef.child("Thumbnails").child(url);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] bitmapData = baos.toByteArray();

            UploadTask uploadTask = filepath.putBytes(bitmapData);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(InsideCommentDialog.this, "Upload Successful!", Toast.LENGTH_SHORT).show();
                    mProgress.dismiss();
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    String thumbUrl = taskSnapshot.getDownloadUrl().toString();
                    postImage(caption, highresUrl, thumbUrl);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(InsideCommentDialog.this, "Upload Failed", Toast.LENGTH_SHORT).show();
                    mProgress.dismiss();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {

            // user clicked "post" button from Preview Activity
            if (data != null && data.getStringExtra("user_permission") != null) {
                mProgress.setMessage("Uploading...");
                mProgress.show();

                Uri uri = photoURI;

                final String caption = (String) data.getStringExtra("caption");

                final Bitmap thumb = (Bitmap) data.getParcelableExtra("thumbnail");

                final StorageReference filepath = storageRef.child("Photos").child(uri.getLastPathSegment());
                filepath.putFile(photoURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String highresUrl = uri.toString();
                                storeThumbNail(thumb, uri.getLastPathSegment().split("Photos/")[1], caption, highresUrl);
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
                        Toast.makeText(InsideCommentDialog.this, "Upload Failed", Toast.LENGTH_SHORT).show();
                        mProgress.dismiss();
                    }
                });

            } else { // Takes user to the Preview Activity
                Intent i = new Intent(InsideCommentDialog.this, PreviewActivity.class);
                i.putExtra("image", mCurrentPhotoPath);

                startActivityForResult(i, 1);
            }
        }
    }

    public void postImage(String caption, String highresUrl, String thumbUrl) {
        if (caption.length() <= maxCommentLength) {
            postNewComment(caption, highresUrl, thumbUrl);
        } else {
            Toast.makeText(InsideCommentDialog.this, "Comment cannot be larger than 300 characters", Toast.LENGTH_SHORT).show();
        }
    }

    public ArrayList<Comment> sortComments(ArrayList<Comment> comments) {

        if (comments.size() > 2) {
            Comment headComment = comments.get(0);
            comments.remove(headComment);

            Collections.sort(comments, new Comparator<Comment>() {
                public int compare(Comment c1, Comment c2) {
                    return c2.upVotes.compareTo(c1.upVotes);
                }
            });

            comments.add(0, headComment);
        }
        return comments;
    }


    private String getUsername() {
        SharedPreferences prefs = this.getSharedPreferences(
                "com.example.cedric.timecapsule", Context.MODE_PRIVATE);
        return prefs.getString("username", "Default");
    }


    private void setmCommentAdapter() {
        mCommentAdapter = new CommentAdapter(this, mComments);
        mCommentRecyclerView.setAdapter(mCommentAdapter);
        //mCommentRecyclerView.smoothScrollToPosition(0);
    }

    private void postNewComment(String commentText, String highresUrl, String thumbUrl) {
        Date curDate = new Date();
        String timeStamp = Long.toString(System.currentTimeMillis());

        if (highresUrl != "") {
            DatabaseReference galleryRef = database.getReference("locations/" + boxKey);
            galleryRef.child("Photo Gallery").child(timeStamp).child("highresUrl").setValue(highresUrl);
            galleryRef.child("Photo Gallery").child(timeStamp).child("thumbUrl").setValue(highresUrl);
        }

        myRef.child(timeStamp).child("user").setValue(username);
        myRef.child(timeStamp).child("my_message").setValue(commentText);
        myRef.child(timeStamp).child("upVotes").setValue("1");
        myRef.child(timeStamp).child("highresUrl").setValue(highresUrl);
        myRef.child(timeStamp).child("thumbUrl").setValue(thumbUrl);

        headReplies = Integer.toString(Integer.parseInt(headReplies) + 1);
        headRef.child(headDate).child("replies").setValue(headReplies);

    }

    private void getNewChildData(String commentKey) {
        myRef.child(commentKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String u = (String) dataSnapshot.child("user").getValue();
                String m = (String) dataSnapshot.child("my_message").getValue();
                String votes = (String) dataSnapshot.child("upVotes").getValue();
                String highresUrl = (String) dataSnapshot.child("highresUrl").getValue();
                String thumbUrl = (String) dataSnapshot.child("thumbUrl").getValue();

                String timeStamp = dataSnapshot.getKey();

                String replies = "";
                Comment c = new Comment(m, u, timeStamp, votes, boxKey, false, replies, refKey, 2, highresUrl, thumbUrl);

                if (votes != null) {

                    mComments.add(c);
                    mComments = sortComments(mComments);
                    mCommentHashMap.put(timeStamp + m, c);
                    setmCommentAdapter();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // work left
            }
        });
    }




    private void getComments() {
        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {

                String u = (String) dataSnapshot.child("user").getValue();
                String m = (String) dataSnapshot.child("my_message").getValue();
                String votes = (String) dataSnapshot.child("upVotes").getValue();
                String highresUrl = (String) dataSnapshot.child("highresUrl").getValue();
                String thumbUrl = (String) dataSnapshot.child("thumbUrl").getValue();

                String timeStamp = dataSnapshot.getKey();

                String replies = "";
                Comment c = new Comment(m, u, timeStamp, votes, boxKey, false, replies, refKey, 2, highresUrl, thumbUrl);

                if (m == null) {
                    getNewChildData(timeStamp);
                }

                if (c != null && m != null) {
                    mComments.add(c);
                    mComments = sortComments(mComments);
                    mCommentHashMap.put(timeStamp + m, c);
                    setmCommentAdapter();
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                String date = dataSnapshot.getKey();
                String message = (String) dataSnapshot.child("my_message").getValue();
                Comment c = mCommentHashMap.get(date + message);
                if (c != null) {
                    c.upVotes = (String) dataSnapshot.child("upVotes").getValue();
                    setmCommentAdapter();
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

    private void getHeadCommentUpdates() {
        headRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                String date = dataSnapshot.getKey();
                String message = (String) dataSnapshot.child("my_message").getValue();
                Comment c = mCommentHashMap.get(date + message);
                if (c != null) {
                    c.upVotes = (String) dataSnapshot.child("upVotes").getValue();
                    c.replies = (String) dataSnapshot.child("replies").getValue();
                    setmCommentAdapter();
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

}