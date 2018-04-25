package com.example.cedric.timecapsule;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;


public class MessageDialog extends Activity {
    public String myLastPost = "";
    android.support.v7.widget.Toolbar titleBar;
    FirebaseDatabase database;
    DatabaseReference myRef;
    DatabaseReference usersRef;
    Utils u;
    String friendUsername = "";
    String messageKey = "";
    private EditText textField;
    private ImageButton sendButton;
    private String username = "";
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
        myRef = database.getReference("messages");

        usersRef = database.getReference("users");

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

    private void postNewMessage(String messageText) {
        Date curDate = new Date();

        String dateString = curDate.toString();


        String timeStamp = Long.toString(System.currentTimeMillis());

        myRef.child(messageKey).child(timeStamp).child("user").setValue(username);
        myRef.child(messageKey).child(timeStamp).child("my_message").setValue(messageText);
        myRef.child(messageKey).child(timeStamp).child("date").setValue(dateString);


        usersRef.child(username).child("conversations").child(messageKey).child("mostRecentMessage").setValue(messageText);
        usersRef.child(username).child("conversations").child(messageKey).child("mostRecentMessenger").setValue(username);
        usersRef.child(username).child("conversations").child(messageKey).child("mostRecentTime").setValue(dateString);
        usersRef.child(username).child("conversations").child(messageKey).child("friendUsername").setValue(friendUsername);

        usersRef.child(friendUsername).child("conversations").child(messageKey).child("mostRecentMessage").setValue(messageText);
        usersRef.child(friendUsername).child("conversations").child(messageKey).child("mostRecentMessenger").setValue(username);
        usersRef.child(friendUsername).child("conversations").child(messageKey).child("mostRecentTime").setValue(dateString);
        usersRef.child(friendUsername).child("conversations").child(messageKey).child("friendUsername").setValue(username);

        setAdapterAndUpdateData();
    }

    private void getNewMessage(String k) {
        myRef.child(messageKey).child(k).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String u = (String) dataSnapshot.child("user").getValue();
                String m = (String) dataSnapshot.child("my_message").getValue();
                String date = (String) dataSnapshot.child("date").getValue();


                Date d = new Date(date);

                Message message = new Message(m, u, d, messageKey);

                mMessages.add(message);

                setAdapterAndUpdateData();

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


                if (m == null){
                    getNewMessage(dataSnapshot.getKey());
                }

                if (m != null) {

                    Date d = new Date(date);

                    Message message = new Message(m, u, d, messageKey);

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