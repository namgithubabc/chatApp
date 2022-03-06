package com.google.chatapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.chatapp.R;
import com.google.chatapp.adapter.UserAdapter;
import com.google.chatapp.listener.UserListener;
import com.google.chatapp.modules.User;
import com.google.chatapp.utilities.Constants;
import com.google.chatapp.utilities.ManagerPreference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserActivity extends BaseActivity implements UserListener {

    private RecyclerView rcvUser;
    private UserAdapter userAdapter;
    private List<User>  mListUser;
    private ProgressBar progressBar;
    private ManagerPreference managerPreference;
    private TextView txErrorMessage;
    private ImageView imgBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        initWeight();
        managerPreference = new ManagerPreference(this);
        setListener();
        getUser();
    }


    private void initWeight() {
        progressBar = findViewById(R.id.progressBar);
        txErrorMessage = findViewById(R.id.textErrorMessage);
        rcvUser = findViewById(R.id.usersRecycleView);
        imgBack = findViewById(R.id.imageBack);

    }

    private void setListener(){
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void getUser(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS).get().addOnCompleteListener(task -> {
            loading(false);
            String currentUserId = managerPreference.getString(Constants.KEY_USERS_ID);
            if(task.isSuccessful() && task.getResult() != null){
                 mListUser = new ArrayList<>();
                for(QueryDocumentSnapshot documentSnapshot : task.getResult()){
                    if(currentUserId.equals(documentSnapshot.getId())){
                        continue;
                    }
                    User user = new User();
                    user.mName = documentSnapshot.getString(Constants.KEY_NAME);
                    user.mEmail = documentSnapshot.getString(Constants.KEY_EMAIL);
                    user.mImage = documentSnapshot.getString(Constants.KEY_IMAGE);
                    user.mId = documentSnapshot.getId();
                    user.mToken = documentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                    mListUser.add(user);
                }
                if(mListUser.size() > 0 ){
                    userAdapter = new UserAdapter(mListUser,this);
                    rcvUser.setAdapter(userAdapter);
                    rcvUser.setVisibility(View.VISIBLE);
                }else{
                    showErrorMessage();
                }
            }else{
                showErrorMessage();
            }
        });
    }

    private void showErrorMessage(){
        txErrorMessage.setText(String.format("%s","No user available"));
        txErrorMessage.setVisibility(View.VISIBLE);
    }

    private void loading(boolean load){
        if(load){
            progressBar.setVisibility(View.VISIBLE);
        }else {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(this,ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
        finish();
    }
}