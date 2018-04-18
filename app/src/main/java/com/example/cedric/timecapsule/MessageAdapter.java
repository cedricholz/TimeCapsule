package com.example.cedric.timecapsule;


import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;


public class MessageAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private ArrayList<Message> mMessages;

    public MessageAdapter(Context context, ArrayList<Message> messages) {
        mContext = context;
        mMessages = messages;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // here, we specify what kind of view each cell should have. In our case, all of them will have a view

        View view = LayoutInflater.from(mContext).inflate(R.layout.message_cell_layout, parent, false);


        return new MessageViewHolder(view);
    }


    // - get element from your dataset at this position
    // - replace the contents of the view with that element
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        Message message = mMessages.get(position);
        ((MessageViewHolder) holder).bind(message);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mMessages.size();
    }
}

class MessageViewHolder extends RecyclerView.ViewHolder {

    // each data item is just a string in this case
    public RelativeLayout mMessageBubbleLayout;
    public TextView mUsernameTextView;
    public TextView mDateTextView;
    public TextView mMessageTextView;
    public TextView mUpVoteTextView;
    public String date = "";
    public String boxKey = "";
    public String upVotes = "1";
    public String message = "";
    FirebaseDatabase database;
    DatabaseReference myRef;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    private ImageButton upButton;
    private ImageButton downButton;


    public MessageViewHolder(View itemView) {

        super(itemView);

        mMessageBubbleLayout = itemView.findViewById(R.id.message_cell_layout);
        mUsernameTextView = mMessageBubbleLayout.findViewById(R.id.username_text_view);
        mDateTextView = mMessageBubbleLayout.findViewById(R.id.date_text_view);
        mMessageTextView = mMessageBubbleLayout.findViewById(R.id.message_text_view);

        upButton = mMessageBubbleLayout.findViewById(R.id.upVote);
        downButton = mMessageBubbleLayout.findViewById(R.id.downVote);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("locations");

        prefs = itemView.getContext().getSharedPreferences(
                "com.example.cedric.timecapsule", Context.MODE_PRIVATE);

        editor = prefs.edit();

        prefs.getString("username", "Default");

    }

    void bind(Message messages) {
        mUsernameTextView.setText(messages.username);
        mDateTextView.setText("posted " + messages.elapsedTimeString() + " ago");

        mMessageTextView.setText(messages.text);

        //mUpVoteTextView.setText("1");

        date = messages.date.toString();
        boxKey = messages.boxKey;
    }

}