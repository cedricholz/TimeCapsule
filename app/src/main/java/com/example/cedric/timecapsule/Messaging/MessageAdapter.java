package com.example.cedric.timecapsule.Messaging;


import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.cedric.timecapsule.Imaging.FullImageActivty;
import com.example.cedric.timecapsule.R;
import com.example.cedric.timecapsule.Utils.Utils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

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

        return new MessageViewHolder(view, mContext);
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
    public RelativeLayout leftPlaceLayout;
    public RelativeLayout rightPlaceLayout;

    public TextView mLeftUsernameTextView;
    public TextView mRightUsernameTextView;

    public TextView mLeftDateTextView;
    public TextView mRightDateTextView;

    public TextView mLeftMessageTextView;
    public TextView mRightMessageTextView;

    public String date = "";
    public String messageKey = "";
    public String message = "";

    public FirebaseDatabase database;
    public FirebaseStorage storage;
    public DatabaseReference myRef;
    public StorageReference storageRef;

    public ImageView leftPlaceView;
    public ImageView rightPlaceView;

    public Context mContext;

    Utils u;
    private String myUsername = "";
    private String highresUrl;
    private String thumbUrl;

    public MessageViewHolder(View itemView, Context mContext) {
        super(itemView);

        this.mContext = mContext;

        mMessageBubbleLayout = itemView.findViewById(R.id.message_cell_layout);

        mLeftUsernameTextView = mMessageBubbleLayout.findViewById(R.id.left_username_text_view);

        mLeftDateTextView = mMessageBubbleLayout.findViewById(R.id.left_date_text_view);
        mRightDateTextView = mMessageBubbleLayout.findViewById(R.id.right_date_text_view);

        mLeftMessageTextView = mMessageBubbleLayout.findViewById(R.id.left_message_text_view);
        mRightMessageTextView = mMessageBubbleLayout.findViewById(R.id.right_message_text_view);

        leftPlaceView = mMessageBubbleLayout.findViewById(R.id.left_image);
        rightPlaceView = mMessageBubbleLayout.findViewById(R.id.right_image);

        leftPlaceLayout = mMessageBubbleLayout.findViewById(R.id.left_image_layout);
        rightPlaceLayout = mMessageBubbleLayout.findViewById(R.id.right_image_layout);

        u = new Utils();

        myUsername = u.getUsername(itemView.getContext());
    }

    void bind(Message message) {

        String messageUser = message.username;
        String dateText = message.elapsedTimeString();

        highresUrl = message.highresUrl;
        thumbUrl = message.thumbUrl;

        if (messageUser.equals(myUsername)) {
            mLeftUsernameTextView.setVisibility(View.GONE);
            mLeftDateTextView.setVisibility(View.GONE);
            mLeftMessageTextView.setVisibility(View.GONE);
            leftPlaceLayout.setVisibility(View.GONE);

            if (message.text != "" && message.text.length() >= 1) {
                mRightDateTextView.setVisibility(View.VISIBLE);
                mRightMessageTextView.setVisibility(View.VISIBLE);
                mRightMessageTextView.setText(message.text);
            }

            if (thumbUrl != null && thumbUrl.length() >= 1) {
                rightPlaceLayout.setVisibility(View.VISIBLE);
                Picasso.get().load(thumbUrl).into(rightPlaceView);

                rightPlaceView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, FullImageActivty.class);
                        intent.putExtra("image", highresUrl);
                        mContext.startActivity(intent);
                    }
                });
            }
        } else {
            mRightDateTextView.setVisibility(View.GONE);
            mRightMessageTextView.setVisibility(View.GONE);
            rightPlaceLayout.setVisibility(View.GONE);

            mLeftUsernameTextView.setText(message.username);
            mLeftDateTextView.setText(dateText);
            mLeftMessageTextView.setVisibility(View.GONE);

            if (message.text != "" && message.text.length() >= 1) {
                mLeftMessageTextView.setVisibility(View.VISIBLE);
                mLeftMessageTextView.setText(message.text);
            }

            if (thumbUrl != null && thumbUrl.length() >= 1) {
                leftPlaceLayout.setVisibility(View.VISIBLE);
                Picasso.get().load(thumbUrl).into(leftPlaceView);

                leftPlaceView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, FullImageActivty.class);
                        intent.putExtra("image", highresUrl);
                        mContext.startActivity(intent);
                    }
                });
            }
        }

        date = message.date.toString();
        messageKey = message.messageKey;

    }

}