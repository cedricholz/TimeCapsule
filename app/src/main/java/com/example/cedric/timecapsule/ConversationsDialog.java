package com.example.cedric.timecapsule;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Window;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Date;


public class ConversationsDialog extends Activity {

    FirebaseDatabase database;
    DatabaseReference usersRef;

    Utils u;
    private String username = "";
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private ArrayList<Conversation> mConversations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.conversations_dialog_layout);

        mRecyclerView = findViewById(R.id.conversation_recycler);
        mRecyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);

        u = new Utils();

        username = u.getUsername(this);

        database = FirebaseDatabase.getInstance();

        usersRef = database.getReference("users");

        getConversations();
    }


    private void getConversations() {

        usersRef.child(username).child("conversations").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {

                String conversationKey = dataSnapshot.getKey();


                String mostRecentMessage = (String) dataSnapshot.child("mostRecentMessage").getValue();
                String mostRecentMessenger = (String) dataSnapshot.child("mostRecentMessenger").getValue();
                String mostRecentTime = (String) dataSnapshot.child("mostRecentTime").getValue();
                String friendUsername = (String) dataSnapshot.child("friendUsername").getValue();

                Conversation conv = new Conversation(conversationKey, mostRecentMessage, mostRecentMessenger, mostRecentTime, friendUsername);

                mConversations.add(0,conv);

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
        // create a new adapter with the updated mMessages array
        // this will "refresh" our recycler view
        mAdapter = new ConversationsAdapter(this, mConversations);
        mRecyclerView.setAdapter(mAdapter);


        mRecyclerView.smoothScrollToPosition(mRecyclerView.getAdapter().getItemCount() - 1);

    }

}