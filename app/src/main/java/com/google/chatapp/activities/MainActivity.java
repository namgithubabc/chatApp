package com.google.chatapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.chatapp.R;
import com.google.chatapp.adapter.RecentConversionAdapter;
import com.google.chatapp.listener.ConversionListener;
import com.google.chatapp.modules.ChatMessage;
import com.google.chatapp.modules.User;
import com.google.chatapp.utilities.Constants;
import com.google.chatapp.utilities.ManagerPreference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity implements ConversionListener {
    private ManagerPreference managerPreference;
    private TextView infoName;
    private ImageView imgProfile,imgSignOut;
    private FloatingActionButton btnActionAdd;
    private List<ChatMessage> conversions;
    private RecentConversionAdapter recentConversionAdapter;
    private RecyclerView recyclerViewARecentConversion;
    private FirebaseFirestore database;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initWeight();
        managerPreference = new ManagerPreference(this);
        init();
        loadUserDetail();
        getToken();
        setListener();
        listenerConversion();
    }


    private void init(){
        conversions = new ArrayList<>();
        progressBar = new ProgressBar(this);
        recentConversionAdapter = new RecentConversionAdapter(conversions,this);
        recyclerViewARecentConversion.setAdapter(recentConversionAdapter);
        database =  FirebaseFirestore.getInstance();
    }

    private void initWeight() {
        infoName = findViewById(R.id.textName);
        imgProfile = findViewById(R.id.imageProfile);
        imgSignOut = findViewById(R.id.imageSignOut);
        btnActionAdd = findViewById(R.id.fabNewChat);
        recyclerViewARecentConversion = findViewById(R.id.conversionRecycleView);
//        progressBar = findViewById(R.id.progressBar);

    }

    private void setListener(){
        imgSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
        btnActionAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UserActivity.class);
                startActivity(intent);
            }
        });
    }
    private void loadUserDetail(){
        infoName.setText(managerPreference.getString(Constants.KEY_NAME));
        byte[] bytes = Base64.decode(managerPreference.getString(Constants.KEY_IMAGE),Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
        imgProfile.setImageBitmap(bitmap);
    }
    private void showToast(String message){
        Toast.makeText(MainActivity.this , message , Toast.LENGTH_SHORT).show();
    }
    private void listenerConversion(){
        database.collection(Constants.KEY_COLLECTION_CONVERSION)
                .whereEqualTo(Constants.KEY_SENDER_ID,managerPreference.getString(Constants.KEY_USERS_ID))
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CONVERSION)
                .whereEqualTo(Constants.KEY_RECEIVED_ID,managerPreference.getString(Constants.KEY_USERS_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = ((value , error) -> {
        if(error != null){
            return;
        }
        if(value != null){
            for (DocumentChange documentChange : value.getDocumentChanges()){
                if(documentChange.getType() == DocumentChange.Type.ADDED){
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receivedId = documentChange.getDocument().getString(Constants.KEY_RECEIVED_ID);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = senderId;
                    chatMessage.receiverId = receivedId;
                    if(managerPreference.getString(Constants.KEY_USERS_ID).equals(senderId)){
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_RECEIVED_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_RECEIVED_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_RECEIVED_ID);
                    }else{
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    }
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_LASS_MESSAGE);
                    chatMessage.dataObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    conversions.add(chatMessage);
                }else if(documentChange.getType() == DocumentChange.Type.MODIFIED){
                    for (int i = 0; i < conversions.size(); i++){
                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receivedId = documentChange.getDocument().getString(Constants.KEY_RECEIVED_ID);
                        if(conversions.get(i).senderId.equals(senderId) && conversions.get(i).receiverId.equals(receivedId)){
                            conversions.get(i).message = documentChange.getDocument().getString(Constants.KEY_LASS_MESSAGE);
                            conversions.get(i).dataObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            break;
                        }
                    }
                }
            }
            Collections.sort(conversions, (o1 , o2) -> o2.dataObject.compareTo(o1.dataObject));
            recentConversionAdapter.notifyDataSetChanged();
            recyclerViewARecentConversion.setAdapter(recentConversionAdapter);
            recyclerViewARecentConversion.setVisibility(View.VISIBLE);
//            progressBar.setVisibility(View.GONE);

        }
    });

    private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token){
        managerPreference.putString(Constants.KEY_FCM_TOKEN,token);
        FirebaseFirestore database  =  FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(managerPreference.getString(Constants.KEY_USERS_ID));
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
//                .addOnSuccessListener(unused -> showToast("Token update successfully"))
                .addOnFailureListener(e -> showToast("Unable to update token"));
    }

    private void signOut(){
        showToast("Sign Out....");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(managerPreference.getString(Constants.KEY_USERS_ID));
        HashMap<String, Object> stringObjectHashMap = new HashMap<>();
        stringObjectHashMap.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(stringObjectHashMap).addOnSuccessListener(unused -> {
            managerPreference.clear();
            startActivity(new Intent(this,SignInActivity.class));
            finish();
        })
                .addOnFailureListener(e -> showToast("Unable to sign out"));
    }

    @Override
    public void onConversionClicked(User user) {
        Intent intent = new Intent(MainActivity.this,ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
    }
}