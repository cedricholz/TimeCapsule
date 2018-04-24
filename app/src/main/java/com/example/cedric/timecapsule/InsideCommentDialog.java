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

public class InsideCommentDialog extends Activity {

    public String myLastPost = "";
    android.support.v7.widget.Toolbar titleBar;
    FirebaseDatabase database;
    DatabaseReference myRef;

    DatabaseReference headRef;

    Utils u;
    String headUsername;
    String headMessage;
    String headDate;
    String headReplies;

    String headRefString = "";

    String headVotes;
    String refKey = "locations";
    private EditText textField;
    private ImageButton sendButton;
    private String username = "";
    private String boxKey = "";
    private RecyclerView mCommentRecyclerView;
    private RecyclerView.Adapter mCommentAdapter;
    private ArrayList<Comment> mComments = new ArrayList<>();
    private HashMap<String, Comment> mCommentHashMap = new HashMap<>();

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


        if (intentExtras != null) {

            headUsername = (String) intentExtras.get("headUsername");
            headMessage = (String) intentExtras.get("headMessage");
            headDate = (String) intentExtras.get("headDate");
            headReplies = (String) intentExtras.get("headReplies");

            headVotes = (String) intentExtras.get("headVotes");
            boxKey = (String) intentExtras.get("boxKey");

            refKey = "locations/" + boxKey + "/messages/" + headDate + "/commentMessages";
            headRefString = "locations/" + boxKey + "/messages/";

            Comment headComment = new Comment(headMessage, headUsername, new Date(headDate), headVotes, boxKey, true, headReplies, headRefString, commentLevel, "");
            mComments.add(headComment);
            mCommentHashMap.put(headDate + headMessage, headComment);


        }


        database = FirebaseDatabase.getInstance();
        myRef = database.getReference(refKey);

        headRef = database.getReference(headRefString);

        textField = findViewById(R.id.inside_comment_input_edit_text);
        sendButton = findViewById(R.id.send_button);

        setButtonListener();

        setmCommentAdapter();

        getComments();

        getHeadCommentUpdates();

    }


    public void setButtonListener() {
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

                        postNewComment(text);
                    } else {
                        Toast.makeText(InsideCommentDialog.this, "Comment cannot be larger than 300 characters", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    public ArrayList<Comment> sortComments(ArrayList<Comment> comments) {

        if (comments.size() > 2) {
            Comment headComment = comments.get(0);
            comments.remove(headComment);

            Collections.sort(comments, new Comparator<Comment>() {
                public int compare(Comment c1, Comment c2) {
                    return c2.getUpVotes().compareTo(c1.getUpVotes());
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
        mCommentRecyclerView.smoothScrollToPosition(0);
    }

    private void postNewComment(String commentText) {
        Date curDate = new Date();

        myRef.child(curDate.toString()).child("user").setValue(username);
        myRef.child(curDate.toString()).child("message").setValue(commentText);
        myRef.child(curDate.toString()).child("upVotes").setValue("1");

        headReplies = Integer.toString(Integer.parseInt(headReplies) + 1);
        headRef.child(headDate).child("replies").setValue(headReplies);

        String replies = "0";
        Comment newComment = new Comment(commentText, username, curDate, "1", boxKey, false, replies, refKey, commentLevel, "");

        mComments.add(newComment);
        mComments = sortComments(mComments);

        mCommentHashMap.put(curDate.toString() + commentText, newComment);

        setmCommentAdapter();
    }

    private void getComments() {
        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {

                String u = (String) dataSnapshot.child("user").getValue();
                String m = (String) dataSnapshot.child("message").getValue();
                String votes = (String) dataSnapshot.child("upVotes").getValue();

                String date = dataSnapshot.getKey();
                Date d = new Date(date);
                String replies = "";
                Comment c = new Comment(m, u, d, votes, boxKey, false, replies, refKey, 2, "");

                if (c != null && m != null) {
                    mComments.add(c);
                    mComments = sortComments(mComments);
                    mCommentHashMap.put(d.toString() + m, c);
                    setmCommentAdapter();
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                String date = dataSnapshot.getKey();
                String message = (String) dataSnapshot.child("message").getValue();
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
                String message = (String) dataSnapshot.child("message").getValue();
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