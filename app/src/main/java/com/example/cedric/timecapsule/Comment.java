package com.example.cedric.timecapsule;

import java.net.Inet4Address;
import java.util.Date;

// custom class made for storing a message. you can update this class
public class Comment {

    public String text;
    public String username;
    public String timeStamp;
    public String upVotes;
    public String boxKey;
    public Boolean headComment;
    public String replies;
    public String refKey;
    public int commentLevel;
    public String photoUrl;

    Comment(String text, String username, String timeStamp, String upVotes, String boxKey, Boolean headComment, String replies,
            String refKey, int commentLevel, String photoUrl) {
        this.text = text;
        this.username = username;
        this.timeStamp = timeStamp;
        this.upVotes = upVotes;
        this.boxKey = boxKey;
        this.headComment = headComment;
        this.replies = replies;
        this.refKey = refKey;

        this.commentLevel = commentLevel;

        this.photoUrl = photoUrl;



    }

    public String getUpVotes() {
        return upVotes;
    }


    // returns a string indicating how long ago this post was made
    protected String elapsedTimeString() {

        Date date = new Date(Long.parseLong(timeStamp));

        long diff = new Date().getTime() - date.getTime();
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        int daysInt = Math.round(days);
        int hoursInt = Math.round(hours);
        int minutesInt = Math.round(minutes);
        if (daysInt == 1) {
            return "1 day";
        } else if (daysInt > 1) {
            return Integer.toString(daysInt) + " days";
        } else if (hoursInt == 1) {
            return "1 hour";
        } else if (hoursInt > 1) {
            return Integer.toString(hoursInt) + " hr";
        } else {
            return Integer.toString(minutesInt) + " min";
        }
    }
}
