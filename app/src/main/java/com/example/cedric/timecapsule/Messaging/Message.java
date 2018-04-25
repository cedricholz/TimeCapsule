package com.example.cedric.timecapsule.Messaging;

import java.util.Date;

// custom class made for storing a message. you can update this class
public class Message {

    public String text;
    public String username;
    public Date date;
    public String messageKey;

    Message(String text, String username, Date date, String messageKey) {
        this.text = text;
        this.username = username;
        this.date = date;
        this.messageKey = messageKey;
    }

    // returns a string indicating how long ago this post was made
    protected String elapsedTimeString() {
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

