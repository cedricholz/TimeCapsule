package com.example.cedric.timecapsule;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

public class boxDialog extends Activity {
    android.support.v7.widget.Toolbar titleBar;
    FirebaseDatabase database;
    DatabaseReference myRef;
    private EditText textField;
    private ImageButton sendButton;
    private String username = "";
    private String key = "";
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private ArrayList<Comment> mComments = new ArrayList<>();

    private HashMap<String, Comment> commentHashMap = new HashMap<>();

    Utils u;

    private int maxCommentLength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dialog);

        titleBar = findViewById(R.id.my_toolbar);

        Intent thisIntent = getIntent();
        Bundle intentExtras = thisIntent.getExtras();

        mRecyclerView = findViewById(R.id.comment_recycler);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        username = getUsername();

        u = new Utils();

        maxCommentLength = u.getMaxCommentLength();



        if (intentExtras != null) {
            titleBar.setTitle((String) intentExtras.get("boxName"));
            titleBar.setSubtitle((String) intentExtras.get("address"));
            key = intentExtras.get("boxName") + "%" + intentExtras.get("address") + "%" + intentExtras.get("imageName");
        }

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("locations");

        textField = findViewById(R.id.comment_input_edit_text);
        sendButton = findViewById(R.id.send_button);
        setButtonListener();

        getComments();
    }

    public void setButtonListener(){
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String text = textField.getText().toString();
                if (text.length() < 1) {
                    textField.requestFocus();
                } else {

                    if (text.length() <= maxCommentLength){

                        textField.setText("");

                        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        mgr.hideSoftInputFromWindow(textField.getWindowToken(), 0);

                        postNewComment(text);
                    }
                    else{
                        Toast.makeText(boxDialog.this, "Comment cannot be larger than 300 characters", Toast.LENGTH_SHORT).show();

                    }


                }
            }
        });
    }


    public ArrayList<Comment> sortComments(ArrayList<Comment> comments) {
        Collections.sort(comments, new Comparator<Comment>() {
            public int compare(Comment c1, Comment c2) {
                return c2.getUpVotes().compareTo(c1.getUpVotes());
            }
        });
        return comments;
    }

    private String getUsername() {
        SharedPreferences prefs = this.getSharedPreferences(
                "com.example.cedric.timecapsule", Context.MODE_PRIVATE);
        return prefs.getString("username", "Default");
    }

    public String myLastPost = "";

    private void getComments() {

        myRef.child(key).child("messages").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {

                String u = (String) dataSnapshot.child("user").getValue();
                String m = (String) dataSnapshot.child("message").getValue();
                String votes = (String) dataSnapshot.child("upVotes").getValue();

                if (m != null && !myLastPost.equals(u + m)){
                    String date = dataSnapshot.getKey();

                    Date d = new Date(date);

                    Comment c = new Comment(m, u, d, votes, key);

                    mComments.add(c);

                    mComments = sortComments(mComments);

                    commentHashMap.put(d.toString() + m, c);

                    setAdapterAndUpdateData();
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {

                String date = dataSnapshot.getKey();
                String message = (String) dataSnapshot.child("message").getValue();
                Comment c = commentHashMap.get(date + message);
                if (c != null) {
                    c.upVotes = (String) dataSnapshot.child("upVotes").getValue();
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
        mRecyclerView.smoothScrollToPosition(0);
    }



    private void postNewComment(String commentText) {
        Date curDate = new Date();


        myRef.child(key).child("messages").child(curDate.toString()).child("user").setValue(username);
        myRef.child(key).child("messages").child(curDate.toString()).child("message").setValue(commentText);
        myRef.child(key).child("messages").child(curDate.toString()).child("upVotes").setValue("1");

        myLastPost = username + commentText;

        Comment newComment = new Comment(commentText, username, curDate, "1", key);

        mComments.add(newComment);
        mComments = sortComments(mComments);

        commentHashMap.put(curDate.toString() + commentText, newComment);

        setAdapterAndUpdateData();
    }
}