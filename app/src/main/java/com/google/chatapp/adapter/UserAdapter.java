package com.google.chatapp.adapter;



import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.chatapp.R;
import com.google.chatapp.activities.UserActivity;
import com.google.chatapp.listener.UserListener;
import com.google.chatapp.modules.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private final List<User> mListUser;
    private final UserListener userListener;

    public UserAdapter(List<User> mListUser, UserListener userListener) {
        this.mListUser = mListUser;
        this.userListener = userListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent , int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_user,parent,false);
        return  new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder , int position) {
        User user = new User();
        holder.setUserData(mListUser.get(position));
    }

    @Override
    public int getItemCount() {
        if(mListUser != null){
            return mListUser.size();
        }
        return 0;
    }



    class UserViewHolder extends RecyclerView.ViewHolder{
        private ImageView imgViewDisplayInfo;
        private TextView txName,txEmail;
        private ConstraintLayout userActivity;
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            imgViewDisplayInfo = itemView.findViewById(R.id.image_profile_display);
            txName = itemView.findViewById(R.id.text_name);
            txEmail = itemView.findViewById(R.id.text_email);
            userActivity = itemView.findViewById(R.id.layoutItemUser);
        }
        void setUserData(User user){
            txName.setText(user.mName);
            txEmail.setText(user.mEmail);
            imgViewDisplayInfo.setImageBitmap(getUserImage(user.mImage));
            userActivity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    userListener.onUserClicked(user);
                }
            });
        }
    }

    private Bitmap getUserImage(String encodeImage){
        byte[] bytes = Base64.decode(encodeImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0, bytes.length);

    }
}
