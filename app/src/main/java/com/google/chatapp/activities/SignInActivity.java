package com.google.chatapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.chatapp.R;
import com.google.chatapp.databinding.ActivityMainBinding;
import com.google.chatapp.databinding.ActivitySignInBinding;
import com.google.chatapp.utilities.Constants;
import com.google.chatapp.utilities.ManagerPreference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SignInActivity extends AppCompatActivity {
    private ActivitySignInBinding activitySignInBinding;
    private TextView createNewAcc;
    private Button btnSignIn;
    private EditText edtEmail, edtPass;
    private ManagerPreference managerPreference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        managerPreference = new ManagerPreference(this);
        if(managerPreference.getBoolean(Constants.KEY_IS_DESIGN_IN)){
            Intent intent = new Intent(SignInActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        activitySignInBinding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_sign_in);
        initUI();
        setListeners();
    }

    private void initUI() {
        createNewAcc = findViewById(R.id.textCreateNewAccount);
        edtEmail = findViewById(R.id.input_email);
        edtPass = findViewById(R.id.input_password);
        btnSignIn = findViewById(R.id.buttonSignIn);
    }

    private void setListeners(){
        createNewAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isValidateSignInDetail()){
                    signIn();
                }
            }
        });
    }

    private void loading(Boolean isLoading){
        if(isLoading) {
            btnSignIn.setVisibility(View.INVISIBLE);
            btnSignIn.setVisibility(View.VISIBLE);
        }else{
            btnSignIn.setVisibility(View.VISIBLE);
            btnSignIn.setVisibility(View.INVISIBLE);
        }
    }

    private void signIn(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL,edtEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD,edtPass.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        managerPreference.putBoolean(Constants.KEY_IS_DESIGN_IN,true);
                        managerPreference.putString(Constants.KEY_USERS_ID, documentSnapshot.getId());
                        managerPreference.putString(Constants.KEY_NAME,documentSnapshot.getString(Constants.KEY_NAME));
                        managerPreference.putString(Constants.KEY_IMAGE,documentSnapshot.getString(Constants.KEY_IMAGE));
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }else{
                        showToast("Unable Sign In");
                    }
                });
    }
    private void showToast(String message){
        Toast.makeText(SignInActivity.this , message , Toast.LENGTH_SHORT).show();
    }
    private Boolean isValidateSignInDetail(){
        if(edtEmail.getText().toString().isEmpty()){
            showToast("Enter email");
            return false;
        }else if(edtPass.getText().toString().trim().isEmpty()){
            showToast("Enter password");
            return false;
        }else{
            return  true;
        }
    }
}