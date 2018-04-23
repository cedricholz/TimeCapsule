package com.example.cedric.timecapsule;

import java.util.Date;

public class Conversation {

    String conversationKey;
    String mostRecentMessage;
    String mostRecentMessenger;
    String mostRecentTime;
    String friendUsername;

    public Conversation(String conversationKey, String mostRecentMessage, String mostRecentMessenger, String mostRecentTime, String friendUsername) {
        this.conversationKey = conversationKey;
        this.mostRecentMessage = mostRecentMessage;
        this.mostRecentMessenger = mostRecentMessenger;
        this.mostRecentTime = mostRecentTime;
        this.friendUsername = friendUsername;
    }

}
