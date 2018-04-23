package com.example.cedric.timecapsule;


import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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

    public TextView mLeftUsernameTextView;
    public TextView mRightUsernameTextView;

    public TextView mLeftDateTextView;
    public TextView mRightDateTextView;

    public TextView mLeftMessageTextView;
    public TextView mRightMessageTextView;

    public String date = "";
    public String messageKey = "";
    public String message = "";

    Utils u;
    private String myUsername = "";

    public MessageViewHolder(View itemView) {

        super(itemView);

        mMessageBubbleLayout = itemView.findViewById(R.id.message_cell_layout);

        mLeftUsernameTextView = mMessageBubbleLayout.findViewById(R.id.left_username_text_view);
        mRightUsernameTextView = mMessageBubbleLayout.findViewById(R.id.right_username_text_view);

        mLeftDateTextView = mMessageBubbleLayout.findViewById(R.id.left_date_text_view);
        mRightDateTextView = mMessageBubbleLayout.findViewById(R.id.right_date_text_view);

        mLeftMessageTextView = mMessageBubbleLayout.findViewById(R.id.left_message_text_view);
        mRightMessageTextView = mMessageBubbleLayout.findViewById(R.id.right_message_text_view);


        u = new Utils();

        myUsername = u.getUsername(itemView.getContext());

    }

    void bind(Message message) {

        String messageUser = message.username;
        String dateText = message.elapsedTimeString();

        if (messageUser.equals(myUsername)) {
            mLeftUsernameTextView.setVisibility(View.INVISIBLE);
            mLeftDateTextView.setVisibility(View.INVISIBLE);
            mLeftMessageTextView.setVisibility(View.INVISIBLE);

            mRightUsernameTextView.setVisibility(View.VISIBLE);
            mRightDateTextView.setVisibility(View.VISIBLE);
            mRightMessageTextView.setVisibility(View.VISIBLE);

            mRightUsernameTextView.setText(message.username);
            mRightMessageTextView.setText(message.text);
            mRightDateTextView.setText(dateText);
        } else {

            mLeftUsernameTextView.setText(message.username);
            mLeftMessageTextView.setText(message.text);
            mLeftDateTextView.setText(dateText);
        }

        date = message.date.toString();
        messageKey = message.messageKey;

    }

}