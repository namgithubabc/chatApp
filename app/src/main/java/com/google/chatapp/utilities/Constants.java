package com.google.chatapp.utilities;

import java.util.HashMap;

public class Constants {
    public static final String KEY_COLLECTION_USERS = "user";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_PREFERENCE_NAME = "chatAppPreference";
    public static final String KEY_IS_DESIGN_IN = "isSignIn";
    public static final String KEY_USERS_ID = "userId";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_FCM_TOKEN = "fcmToken";
    public static final String KEY_USER = "user";
    public static final String KEY_COLLECTION_CHAT = "chat";
    public static final String KEY_SENDER_ID = "senderId";
    public static final String KEY_RECEIVED_ID = "receivedId";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TIMESTAMP = "timesTamp";
    public static final String KEY_COLLECTION_CONVERSION = "conversation";
    public static final String KEY_SENDER_NAME = "senderName";
    public static final String KEY_RECEIVED_NAME = "receivedName";
    public static final String KEY_SENDER_IMAGE = "senderImage";
    public static final String KEY_RECEIVED_IMAGE = "receivedImage";
    public static final String KEY_LASS_MESSAGE = "lastMessage";
    public static final String KEY_AVAILABILITY = "availability";
    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";

    public static HashMap<String, String> remoteMsgHeader = null;
    public static HashMap<String, String> getRemoteMsgHeader(){
        if(remoteMsgHeader == null){
            remoteMsgHeader = new HashMap<>();
            remoteMsgHeader.put(REMOTE_MSG_AUTHORIZATION,"key=AAAAx3Kv0Po:APA91bHx7IAWHpYM598zd3vZzhoCZp2QmmmkCc3a5_ZEeKTgsmUtz0903tJJquDvZf1OnPr6kxHhRvmm1OCTDuV1rD7zk56BF8npDcb-tOgGSyp2API4g91fcOL-cybEi6lKpZJAKfqe");
            remoteMsgHeader.put(REMOTE_MSG_CONTENT_TYPE,"application/json");
        }
        return remoteMsgHeader;
    }

}
