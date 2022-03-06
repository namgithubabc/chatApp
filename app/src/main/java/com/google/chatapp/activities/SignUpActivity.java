package com.google.chatapp.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.chatapp.R;
import com.google.chatapp.databinding.ActivitySignInBinding;
import com.google.chatapp.databinding.ActivitySignUpBinding;
import com.google.chatapp.utilities.Constants;
import com.google.chatapp.utilities.ManagerPreference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding activitySignUpBinding;
    private TextView textSignUp,textAddImage;
    private ImageView imageSignUp;
    private EditText inputName,inputMail,inputPass,inputConfirm;
    private Button btnSignUp;
    private FrameLayout layoutImage;
    private String encodeImage;
    private ManagerPreference managerPreference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        inintUI();
        managerPreference = new ManagerPreference(this);
        setListeners();
    }

    private void inintUI() {
        textSignUp = findViewById(R.id.textSignInToSignUp);
        imageSignUp = findViewById(R.id.image_sign_up);
        inputName = findViewById(R.id.inputName);
        inputMail = findViewById(R.id.input_email);
        inputPass = findViewById(R.id.input_password);
        inputConfirm = findViewById(R.id.inputConfirmPassword);
        btnSignUp = findViewById(R.id.buttonSignUp);
        textAddImage = findViewById(R.id.textAddImage);
        layoutImage = findViewById(R.id.layoutImage);
    }

    private void setListeners() {
        textSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(intent);
            }
        });
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isValidSignUpDetails()){
                    SignUp();
                }
            }
        });
        layoutImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                pickImage.launch(intent);
            }
        });

    }
    private void showToast(String message){
        Toast.makeText(SignUpActivity.this , message , Toast.LENGTH_SHORT).show();
    }
    private void SignUp(){
        Loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME,inputName.getText().toString());
        user.put(Constants.KEY_EMAIL,inputMail.getText().toString());
        user.put(Constants.KEY_PASSWORD,inputPass.getText().toString());
        user.put(Constants.KEY_IMAGE, encodeImage);
        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    Loading(false);
                    managerPreference.putBoolean(Constants.KEY_IS_DESIGN_IN, true);
                    managerPreference.putString(Constants.KEY_USERS_ID,documentReference.getId());
                    managerPreference.putString(Constants.KEY_NAME,inputName.getText().toString());
                    managerPreference.putString(Constants.KEY_IMAGE,encodeImage);
                    Intent intent = new Intent(this,MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                })
                .addOnFailureListener(exception ->{
                    Loading(false);
                    showToast(exception.getMessage());
                } );
    }


    private String encodeImage(Bitmap bitmap){
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap,previewWidth,previewHeight,false);
        ByteArrayOutputStream byteArrayOutputStream  = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }


    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK){
                    if(result.getData() != null){
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            imageSignUp.setImageBitmap(bitmap);
                            textAddImage.setVisibility(View.GONE);
                            encodeImage = encodeImage(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private Boolean isValidSignUpDetails(){
        if(imageSignUp == null){
            showToast("Selected profile image");
            return  false;
        }else if(inputName.getText().toString().trim().isEmpty()){
            showToast("Enter name");
            return false;
        }else if(inputMail.getText().toString().trim().isEmpty()){
            showToast("Enter email");
            return false;
        }else if(inputPass.getText().toString().trim().isEmpty()){
            showToast("Enter password");
            return false;
        }
        else if(inputConfirm.getText().toString().trim().isEmpty()){
            showToast("Confirm your password");
            return false;
        }else if(!inputPass.getText().toString().equals(inputConfirm.getText().toString())){
            showToast("Password & Confirm password must be same");
            return false;
        }else{
            return true;
        }
    }
    private void Loading(Boolean isLoading){
        if(isLoading){
            btnSignUp.setVisibility(View.INVISIBLE);
            btnSignUp.setVisibility(View.VISIBLE);
        }else{
            btnSignUp.setVisibility(View.INVISIBLE);
            btnSignUp.setVisibility(View.VISIBLE);
        }
    }
}