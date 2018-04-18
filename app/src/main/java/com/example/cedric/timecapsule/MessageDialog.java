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

import java.util.Date;


public class MessageDialog extends Activity {
    public String myLastPost = "";
    android.support.v7.widget.Toolbar titleBar;
    FirebaseDatabase database;
    DatabaseReference myRef;
    String refKey = "locations";
    Utils u;
    private EditText textField;
    private ImageButton sendButton;
    private String username = "";
    private String key = "";
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private ArrayList<Message> mMessages = new ArrayList<>();


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

        username = u.getUsername(this);

        u = new Utils();

        if (intentExtras != null) {
            titleBar.setTitle((String) intentExtras.get("boxName"));

            titleBar.setSubtitle((String) intentExtras.get("address"));
            key = intentExtras.get("boxName") + "%" + intentExtras.get("address") + "%" + intentExtras.get("imageName");

            refKey = "locations/" + key + "/messages/";
        }

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("locations");

        textField = findViewById(R.id.message_input_edit_text);
        sendButton = findViewById(R.id.message_send_button);
        setButtonListener();

        getMessages();
    }

    public void setButtonListener() {
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

                        postNewMessage(text);
                    } else {
                        Toast.makeText(MessageDialog.this, "Message cannot be larger than 300 characters", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }


    private void getMessages() {

        myRef.child(key).child("messages").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {

                String u = (String) dataSnapshot.child("user").getValue();
                String m = (String) dataSnapshot.child("message").getValue();

                if (m != null && !myLastPost.equals(u + m)) {
                    String date = dataSnapshot.getKey();

                    Date d = new Date(date);

                    Message message = new Message(m, u, d, key);

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

        // scroll to the first message
        mRecyclerView.smoothScrollToPosition(0);
    }


    private void postNewMessage(String messageText) {
        Date curDate = new Date();


        myRef.child(key).child("messages").child(curDate.toString()).child("user").setValue(username);
        myRef.child(key).child("messages").child(curDate.toString()).child("message").setValue(messageText);
        myRef.child(key).child("messages").child(curDate.toString()).child("upVotes").setValue("1");
        myRef.child(key).child("messages").child(curDate.toString()).child("replies").setValue("0");

        myLastPost = username + messageText;

        String replies = "0";

        Message newMessage = new Message(messageText, username, curDate, key);

        mMessages.add(newMessage);

        setAdapterAndUpdateData();
    }
}