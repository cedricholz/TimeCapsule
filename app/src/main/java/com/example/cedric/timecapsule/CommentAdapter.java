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


// Adapter for the recycler view in CommentFeedActivity. You do not need to modify this file
public class CommentAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private ArrayList<Comment> mComments;

    public CommentAdapter(Context context, ArrayList<Comment> comments) {
        mContext = context;
        mComments = comments;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // here, we specify what kind of view each cell should have. In our case, all of them will have a view
        // made from comment_cell_layout
        View view = LayoutInflater.from(mContext).inflate(R.layout.comment_cell_layout, parent, false);
        return new CommentViewHolder(view);
    }


    // - get element from your dataset at this position
    // - replace the contents of the view with that element
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // here, we the comment that should be displayed at index `position` in our recylcer view
        // everytime the recycler view is refreshed, this method is called getItemCount() times (because
        // it needs to recreate every cell).
        Comment comment = mComments.get(position);
        ((CommentViewHolder) holder).bind(comment);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mComments.size();
    }
}

class CommentViewHolder extends RecyclerView.ViewHolder {

    // each data item is just a string in this case
    public RelativeLayout mCommentBubbleLayout;
    public TextView mUsernameTextView;
    public TextView mDateTextView;
    public TextView mCommentTextView;
    public TextView mUpVoteTextView;

    private ImageButton upButton;
    private ImageButton downButton;

    public String date = "";
    public String boxKey = "";

    FirebaseDatabase database;
    DatabaseReference myRef;

    public String upVotes = "1";
    public String message = "";


    SharedPreferences prefs;

    SharedPreferences.Editor editor;


    public CommentViewHolder(View itemView) {

        super(itemView);

        mCommentBubbleLayout = itemView.findViewById(R.id.comment_cell_layout);
        mUsernameTextView = mCommentBubbleLayout.findViewById(R.id.username_text_view);
        mDateTextView = mCommentBubbleLayout.findViewById(R.id.date_text_view);
        mCommentTextView = mCommentBubbleLayout.findViewById(R.id.comment_text_view);
        mUpVoteTextView = mCommentBubbleLayout.findViewById(R.id.votes);

        upButton = mCommentBubbleLayout.findViewById(R.id.upVote);
        downButton = mCommentBubbleLayout.findViewById(R.id.downVote);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("locations");


        setButtonListeners();

        prefs = itemView.getContext().getSharedPreferences(
                "com.example.cedric.timecapsule", Context.MODE_PRIVATE);

        editor = prefs.edit();


        prefs.getString("username", "Default");

    }

    public String getVotedKey(String upOrDown){
        return boxKey + date + message + upOrDown;
    }

    public String getIfVoted(String upOrDown){
        String votedKey = getVotedKey(upOrDown);
        return prefs.getString(votedKey, "0");
    }

    public void saveString(String stringToSave, String value){
        editor.putString(stringToSave, value).commit();
    }

    public void addVote(){
        upVotes = Integer.toString((Integer.parseInt(upVotes) + 1));
    }

    public void removeVote(){
        upVotes = Integer.toString((Integer.parseInt(upVotes) - 1));
    }

    public void handleVotes(String upOrDown){
        boolean upVoted = getIfVoted("up").equals("1");
        boolean downVoted = getIfVoted("down").equals("1");

        if (upOrDown.equals("up")){
            if (!upVoted && !downVoted){
                saveString(getVotedKey("up"), "1");
                addVote();
            }
            else if (upVoted && !downVoted){
                saveString(getVotedKey("up"), "0");
                removeVote();
            }
            else if (!upVoted && downVoted){
                saveString(getVotedKey("up"), "1");
                saveString(getVotedKey("down"), "0");
                addVote();
                addVote();
            }
        }
        else{
            if (!upVoted && !downVoted){
                saveString(getVotedKey("down"), "1");
                removeVote();
            }
            else if (upVoted && !downVoted){
                saveString(getVotedKey("down"), "1");
                saveString(getVotedKey("up"), "0");
                removeVote();
                removeVote();
            }
            else if (!upVoted && downVoted){
                saveString(getVotedKey("down"), "0");
                addVote();
            }

        }
    }

    public void setButtonListeners(){
        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                handleVotes("up");

                myRef.child(boxKey).child("messages").child(date).child("upVotes").setValue(upVotes);
            }
        });

        downButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                handleVotes("down");

                myRef.child(boxKey).child("messages").child(date).child("upVotes").setValue(upVotes);
            }
        });
    }

    void bind(Comment comment) {
        mUsernameTextView.setText(comment.username);
        mDateTextView.setText("posted " + comment.elapsedTimeString() + " ago");

        message = comment.text;
        mCommentTextView.setText(comment.text);

        upVotes = comment.upVotes;

        mUpVoteTextView.setText(upVotes);


        date = comment.date.toString();
        boxKey = comment.boxKey;

        handleVoteButtonColor();
     }

    public void handleVoteButtonColor(){
        if (getIfVoted("up").equals("1")){
            upButton.setBackgroundResource(R.drawable.ic_keyboard_arrow_up_green_24dp);
        }
        else{
            upButton.setBackgroundResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
        }


        if (getIfVoted("down").equals("1")){
            downButton.setBackgroundResource(R.drawable.ic_keyboard_arrow_down_green_24dp);
        }
        else{
            downButton.setBackgroundResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
        }
    }

}