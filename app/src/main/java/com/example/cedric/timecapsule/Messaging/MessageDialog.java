package com.example.cedric.timecapsule.Messaging;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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

import com.example.cedric.timecapsule.Comments.CommentDialog;
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
import java.util.Arrays;
import java.util.Date;


public class MessageDialog extends Activity {
    public String myLastPost = "";
    android.support.v7.widget.Toolbar titleBar;
    FirebaseDatabase database;
    FirebaseStorage storage;
    DatabaseReference myRef;
    DatabaseReference usersRef;
    StorageReference storageRef;
    Utils u;
    String friendUsername = "";
    String messageKey = "";
    private EditText textField;
    private ImageButton sendButton;
    private ImageButton cameraButton;
    private String username = "";
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private ArrayList<Message> mMessages = new ArrayList<>();
    private Uri photoURI;
    private static final int CAMERA_REQUEST_CODE = 1;
    private String mCurrentPhotoPath;
    private ProgressDialog mProgress;
    private int maxCommentLength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.message_dialog_layout);

        titleBar = findViewById(R.id.message_toolbar);

        Intent thisIntent = getIntent();
        Bundle intentExtras = thisIntent.getExtras();

        mRecyclerView = findViewById(R.id.message_recycler);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        u = new Utils();

        username = u.getUsername(this);

        if (intentExtras != null) {
            friendUsername = (String) intentExtras.get("commentUsername");
            titleBar.setTitle(friendUsername + " - Private Message");

            String[] usernames = {username, friendUsername};
            Arrays.sort(usernames);

            messageKey = usernames[0] + usernames[1] + "Message";
        }

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        myRef = database.getReference("messages");
        usersRef = database.getReference("users");
        storageRef = storage.getReference();

        textField = findViewById(R.id.message_input_edit_text);
        sendButton = findViewById(R.id.message_send_button);
        cameraButton = findViewById(R.id.message_camera_button);

        setSendButtonListener();
        setCameraButtonListener();

        getMessages();
        mProgress = new ProgressDialog(this);
        maxCommentLength = 300;
    }

    public void setSendButtonListener() {
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String text = textField.getText().toString();
                if (text.length() < 1) {
                    textField.requestFocus();
                } else {

                    if (text.length() <= u.getMaxMessageLength()) {

                        textField.setText("");

                        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        mgr.hideSoftInputFromWindow(textField.getWindowToken(), 0);

                        postNewMessage(text, "", "");
                    } else {
                        Toast.makeText(MessageDialog.this, "Message cannot be larger than 300 characters", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(MessageDialog.this, "Upload Failed", Toast.LENGTH_SHORT).show();
                        mProgress.dismiss();
                    }
                });

            } else { // Takes user to the Preview Activity
                Intent i = new Intent(MessageDialog.this, PreviewActivity.class);
                i.putExtra("image", mCurrentPhotoPath);

                startActivityForResult(i, 1);
            }
        }
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
                    Toast.makeText(MessageDialog.this, "Upload Successful!", Toast.LENGTH_SHORT).show();
                    mProgress.dismiss();
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    String thumbUrl = taskSnapshot.getDownloadUrl().toString();
                    postImage(caption, highresUrl, thumbUrl);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(MessageDialog.this, "Upload Failed", Toast.LENGTH_SHORT).show();
                    mProgress.dismiss();
                }
            });
        }
    }

    public void postImage(String caption, String highresUrl, String thumbUrl) {
        if (caption.length() <= maxCommentLength) {
            postNewMessage(caption, highresUrl, thumbUrl);
        } else {
            Toast.makeText(MessageDialog.this, "Comment cannot be larger than 300 characters", Toast.LENGTH_SHORT).show();
        }
    }


    private void postNewMessage(String messageText, String highresUrl, String thumbUrl) {
        Date curDate = new Date();

        String dateString = curDate.toString();


        String timeStamp = Long.toString(System.currentTimeMillis());

        myRef.child(messageKey).child(timeStamp).child("user").setValue(username);
        myRef.child(messageKey).child(timeStamp).child("my_message").setValue(messageText);
        myRef.child(messageKey).child(timeStamp).child("date").setValue(dateString);
        myRef.child(messageKey).child(timeStamp).child("highresUrl").setValue(highresUrl);
        myRef.child(messageKey).child(timeStamp).child("thumbUrl").setValue(thumbUrl);

        usersRef.child(username).child("conversations").child(messageKey).child("mostRecentMessage").setValue(username +": " + messageText);
        usersRef.child(username).child("conversations").child(messageKey).child("mostRecentImage").setValue(username +": " + highresUrl);
        usersRef.child(username).child("conversations").child(messageKey).child("mostRecentMessenger").setValue(username);
        usersRef.child(username).child("conversations").child(messageKey).child("mostRecentTime").setValue(dateString);
        usersRef.child(username).child("conversations").child(messageKey).child("friendUsername").setValue(friendUsername);

        usersRef.child(friendUsername).child("conversations").child(messageKey).child("mostRecentMessage").setValue(username +": " + messageText);
        usersRef.child(friendUsername).child("conversations").child(messageKey).child("mostRecentImage").setValue(username +": " + highresUrl);
        usersRef.child(friendUsername).child("conversations").child(messageKey).child("mostRecentMessenger").setValue(username);
        usersRef.child(friendUsername).child("conversations").child(messageKey).child("mostRecentTime").setValue(dateString);
        usersRef.child(friendUsername).child("conversations").child(messageKey).child("friendUsername").setValue(username);

        setAdapterAndUpdateData();
    }

    private void getNewMessage(final String k) {

        myRef.child(messageKey).child(k).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String u = (String) dataSnapshot.child("user").getValue();
                String m = (String) dataSnapshot.child("my_message").getValue();
                String date = (String) dataSnapshot.child("date").getValue();
                String highresUrl = (String) dataSnapshot.child("highresUrl").getValue();
                String thumbUrl = (String) dataSnapshot.child("thumbUrl").getValue();

                if (m == null) {
                    getNewMessage(k);
                } else {
                    Date d = new Date(date);

                    Message message = new Message(m, u, d, messageKey, highresUrl, thumbUrl);

                    mMessages.add(message);

                    setAdapterAndUpdateData();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // work left
            }
        });
    }


    private void getMessages() {

        myRef.child(messageKey).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {

                String u = (String) dataSnapshot.child("user").getValue();
                String m = (String) dataSnapshot.child("my_message").getValue();
                String date = (String) dataSnapshot.child("date").getValue();
                String highresUrl = (String) dataSnapshot.child("highresUrl").getValue();
                String thumbUrl = (String) dataSnapshot.child("thumbUrl").getValue();

                if (m == null) {
                    getNewMessage(dataSnapshot.getKey());
                }

                if (m != null) {

                    Date d = new Date(date);

                    Message message = new Message(m, u, d, messageKey, highresUrl, thumbUrl);

                    mMessages.add(message);

                    setAdapterAndUpdateData();
                }

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
        // create a new adapter with the updated mMessages array
        // this will "refresh" our recycler view
        mAdapter = new MessageAdapter(this, mMessages);
        mRecyclerView.setAdapter(mAdapter);

        // scroll to the first my_message
//        mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount() - 1);
        mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
    }

}