package com.google.chatapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.chatapp.R;
import com.google.chatapp.adapter.ChatAdapter;
import com.google.chatapp.databinding.ActivityChatBinding;
import com.google.chatapp.modules.ChatMessage;
import com.google.chatapp.modules.User;
import com.google.chatapp.network.ApiClient;
import com.google.chatapp.network.ApiService;
import com.google.chatapp.utilities.Constants;
import com.google.chatapp.utilities.ManagerPreference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity {
    private TextView nameChat,textAvailability;
    private User receiverUser;
    private ImageView imgBack;
    private EditText edtInputMessage;
    private ChatAdapter chatAdapter;
    private RecyclerView rcvChatListApp;
    private FrameLayout layoutSend;
    private FirebaseFirestore database;
    private ManagerPreference managerPreference;
    private List<ChatMessage> chatMessages;
    private ProgressBar bar;
    private String conversionId = null;
    private Boolean isReceiverAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initUi();
        setListener();
        loadReceivedDetails();
        init();
        listenMessage();

    }

    private void initUi() {
        nameChat = (TextView) findViewById(R.id.chatName);
        imgBack = (ImageView) findViewById(R.id.imageBack);
        rcvChatListApp = (RecyclerView) findViewById(R.id.chatRecycleView);
        edtInputMessage = (EditText) findViewById(R.id.inputMessage);
        layoutSend = (FrameLayout) findViewById(R.id.layoutSend);
        textAvailability = (TextView) findViewById(R.id.textAvailability);
        bar = (ProgressBar) findViewById(R.id.progressBar);
        database = FirebaseFirestore.getInstance();
    }

    private void init(){
        managerPreference = new ManagerPreference(this);
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages,getImageBitmap(receiverUser.mImage),managerPreference.getString(Constants.KEY_USERS_ID));
        rcvChatListApp.setAdapter(chatAdapter);
    }


    private void showToast(String message){
        Toast.makeText(ChatActivity.this , message , Toast.LENGTH_SHORT).show();
    }

    private void sendNotification(String messageBody){
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constants.getRemoteMsgHeader(),messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull  Call<String> call , @NonNull Response<String> response) {
                if(response.isSuccessful()){
                    Toast.makeText(ChatActivity.this , "abc" , Toast.LENGTH_SHORT).show();
                    try{
                        if(response.body() != null){
                            JSONObject responseJson = new JSONObject(response.body());
                            JSONArray results = responseJson.getJSONArray("results");
                            if(responseJson.getInt("failure") == 1){
                                JSONObject error = (JSONObject) results.get(0);
                                showToast(error.getString("error"));
                                return;
                            }
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                    showToast("Notification send successfully");
                }else{
                    showToast("Error" + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call , @NonNull Throwable t) {
                showToast(t.getMessage());
            }
        });
    }


    private void listenAvailabilityOfReceiver(){
        database.collection(Constants.KEY_COLLECTION_USERS).document(receiverUser.mId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value , @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    return;
                }
                if (value != null) {
                    if (value.getLong(Constants.KEY_AVAILABILITY) != null) {
                        int availability = Objects.requireNonNull(
                                value.getLong(Constants.KEY_AVAILABILITY).intValue()
                        );
                        isReceiverAvailable = availability == 1;
                    }
                    receiverUser.mToken = value.getString(Constants.KEY_FCM_TOKEN);
                    if(receiverUser.mImage == null){
                        receiverUser.mImage = value.getString(Constants.KEY_IMAGE);
                        chatAdapter.setReceiverProfileImage(getImageBitmap(receiverUser.mImage));
                        chatAdapter.notifyItemRangeChanged(0,chatMessages.size());
                    }
                }
                if(isReceiverAvailable){
                    textAvailability.setVisibility(View.VISIBLE);
                }else{
                    textAvailability.setVisibility(View.GONE);
                }
            }
        });
    }

    private void listenMessage(){
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID,managerPreference.getString(Constants.KEY_USERS_ID))
                .whereEqualTo(Constants.KEY_RECEIVED_ID,receiverUser.mId)
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID,receiverUser.mId)
                .whereEqualTo(Constants.KEY_RECEIVED_ID,managerPreference.getString(Constants.KEY_USERS_ID))
                .addSnapshotListener(eventListener);
    }


    private final EventListener<QuerySnapshot> eventListener = ((value , error) -> {
        if(error !=null){
            return;
        }
        if (value != null){
            int count = chatMessages.size();
            for(DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVED_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.datetime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.dataObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessages.add(chatMessage);
                }
            }
            Collections.sort(chatMessages,(o1 , o2) -> o1.dataObject.compareTo(o2.dataObject));
            if(count == 0){
                chatAdapter.notifyDataSetChanged();
            }else{
                chatAdapter.notifyItemRangeChanged(chatMessages.size(),chatMessages.size());
                rcvChatListApp.smoothScrollToPosition(chatMessages.size() -1);
            }
            rcvChatListApp.setVisibility(View.VISIBLE);
        }
        bar.setVisibility(View.GONE);
        if(conversionId == null){
            checkForConversion();
        }
    });

    private Bitmap getImageBitmap(String encodeImage){
        if (encodeImage != null){
            byte[] bytes = Base64.decode(encodeImage,Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes,0, bytes.length);
        }else{
            return null;
        }
    }

    private void loadReceivedDetails(){
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        nameChat.setText(receiverUser.mName);
    }

    private void sendMessage(){
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID,managerPreference.getString(Constants.KEY_USERS_ID));
        message.put(Constants.KEY_RECEIVED_ID,receiverUser.mId);
        message.put(Constants.KEY_MESSAGE,edtInputMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMP,new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        if(conversionId != null){
            updateConversion(edtInputMessage.getText().toString());
        }else{
            Toast.makeText(ChatActivity.this , "load" , Toast.LENGTH_SHORT).show();
            HashMap<String, Object> item_conversion = new HashMap<>();
            item_conversion.put(Constants.KEY_SENDER_ID,   managerPreference.getString(Constants.KEY_USERS_ID));
            item_conversion.put(Constants.KEY_SENDER_NAME, managerPreference.getString(Constants.KEY_NAME));
            item_conversion.put(Constants.KEY_SENDER_IMAGE,managerPreference.getString(Constants.KEY_IMAGE));
            item_conversion.put(Constants.KEY_RECEIVED_ID,  receiverUser.mId);
            item_conversion.put(Constants.KEY_RECEIVED_NAME,receiverUser.mName);
            item_conversion.put(Constants.KEY_RECEIVED_IMAGE,receiverUser.mImage);
            item_conversion.put(Constants.KEY_LASS_MESSAGE,edtInputMessage.getText().toString());
            item_conversion.put(Constants.KEY_TIMESTAMP, new Date());
            addConversion(item_conversion);
        }
        if(!isReceiverAvailable){
            try {
                JSONArray tokens = new JSONArray();
                tokens.put(receiverUser.mToken);


                JSONObject data = new JSONObject();
                data.put(Constants.KEY_USERS_ID,managerPreference.getString(Constants.KEY_USERS_ID));
                data.put(Constants.KEY_NAME,managerPreference.getString(Constants.KEY_NAME));
                data.put(Constants.KEY_FCM_TOKEN,managerPreference.getString(Constants.KEY_FCM_TOKEN));
                data.put(Constants.KEY_MESSAGE,managerPreference.getString(Constants.KEY_MESSAGE));

                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MSG_DATA,data);
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS,tokens);

                sendNotification(body.toString());
            }catch (Exception exception){
                showToast(exception.getMessage());
            }
        }
        edtInputMessage.setText(null);

    }

    private void setListener(){
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        layoutSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private String getReadableDateTime(Date date){
        return  new SimpleDateFormat("MMM dd, yyy - hh:mm a", Locale.getDefault()).format(date);
    }


    private void addConversion(HashMap<String, Object> conversion){
        database.collection(Constants.KEY_COLLECTION_CONVERSION)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversionId = documentReference.getId());
    }

    private void updateConversion(String message){
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_CONVERSION).document(conversionId);
        documentReference.update(
                Constants.KEY_LASS_MESSAGE,message,
                Constants.KEY_TIMESTAMP, new Date());
    }

    private void checkForConversion(){
        if(chatMessages.size()!= 0){
            checkForConversionRemotely( managerPreference.getString(Constants.KEY_USERS_ID), receiverUser.mId);
            checkForConversionRemotely(receiverUser.mId,managerPreference.getString(Constants.KEY_USERS_ID));
        }
    }

    private void checkForConversionRemotely(String senderId, String receivedId){
        database.collection(Constants.KEY_COLLECTION_CONVERSION)
                .whereEqualTo(Constants.KEY_SENDER_ID,senderId)
                .whereEqualTo(Constants.KEY_RECEIVED_ID,receivedId)
                .get()
                .addOnCompleteListener(conversionOnCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> conversionOnCompleteListener = task -> {
        if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0 ){
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversionId = documentSnapshot.getId();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }
}