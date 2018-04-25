package com.example.cedric.timecapsule.Messaging;


import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.cedric.timecapsule.R;
import com.example.cedric.timecapsule.Utils.Utils;

import java.util.ArrayList;


public class ConversationsAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private ArrayList<Conversation> mConversations;

    public ConversationsAdapter(Context context, ArrayList<Conversation> conversations) {

        mContext = context;
        mConversations = conversations;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // here, we specify what kind of view each cell should have. In our case, all of them will have a view

        View view = LayoutInflater.from(mContext).inflate(R.layout.conversation_cell_layout, parent, false);

        return new ConversationsViewHolder(view);
    }


    // - get element from your dataset at this position
    // - replace the contents of the view with that element
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        Conversation conversation = mConversations.get(position);
        ((ConversationsViewHolder) holder).bind(conversation);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mConversations.size();
    }
}

class ConversationsViewHolder extends RecyclerView.ViewHolder {

    // each data item is just a string in this case
    public RelativeLayout mConversationBubbleLayout;
    TextView conversation_username_text_view;
    TextView conversation_date_text_view;
    TextView conversation_comment_text_view;


    String commentUsername;

    Utils u;
    private String myUsername = "";

    public ConversationsViewHolder(View itemView) {

        super(itemView);

        mConversationBubbleLayout = itemView.findViewById(R.id.conversation_cell_layout);

        conversation_username_text_view = mConversationBubbleLayout.findViewById(R.id.conversation_username_text_view);
        conversation_date_text_view = mConversationBubbleLayout.findViewById(R.id.conversation_date_text_view);
        conversation_comment_text_view = mConversationBubbleLayout.findViewById(R.id.conversation_comment_text_view);

        u = new Utils();

        myUsername = u.getUsername(itemView.getContext());

        setListeners();
    }

    public void openMessage(View view) {
        Intent insideCommentIntent = new Intent(view.getContext(), MessageDialog.class);
        insideCommentIntent.putExtra("commentUsername", commentUsername);
        view.getContext().startActivity(insideCommentIntent);
    }

    public void setListeners() {
        conversation_username_text_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMessage(view);
            }
        });

        conversation_date_text_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMessage(view);
            }
        });

        conversation_comment_text_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMessage(view);
            }
        });

    }


    void bind(Conversation convo) {
        commentUsername = convo.friendUsername;
        conversation_username_text_view.setText(commentUsername);
        conversation_date_text_view.setText(convo.mostRecentTime);
        conversation_comment_text_view.setText(convo.mostRecentMessage);
    }

}