package com.google.chatapp.adapter;

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
import androidx.recyclerview.widget.RecyclerView;

import com.google.chatapp.R;
import com.google.chatapp.activities.MainActivity;
import com.google.chatapp.listener.ConversionListener;
import com.google.chatapp.modules.ChatMessage;
import com.google.chatapp.modules.User;

import java.util.List;

public class RecentConversionAdapter extends RecyclerView.Adapter<RecentConversionAdapter.ConversionViewHolder> {
    private List<ChatMessage> chatMessages;
    private final ConversionListener conversionListener;

    public RecentConversionAdapter(List<ChatMessage> chatMessages, ConversionListener conversionListener ) {
        this.chatMessages = chatMessages;
        this.conversionListener = conversionListener;
    }

    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent , int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_recent_conversion,parent,false);
        return  new ConversionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversionViewHolder holder , int position) {
        holder.setData(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        if(chatMessages != null){
            return chatMessages.size();
        }
        return 0;
    }

    class ConversionViewHolder extends RecyclerView.ViewHolder{
        private ImageView imgRecentConversion;
        private TextView txNameRecentConversion, txRecentMessage;
        private ConstraintLayout layoutConversion;
        public ConversionViewHolder(@NonNull View itemView) {
            super(itemView);
            imgRecentConversion = itemView.findViewById(R.id.image_profile_display);
            txNameRecentConversion = itemView.findViewById(R.id.text_name);
            txRecentMessage = itemView.findViewById(R.id.textRecentMessage);
            layoutConversion = itemView.findViewById(R.id.layoutItemUser);
        }
        void setData(ChatMessage chatMessage){
            imgRecentConversion.setImageBitmap(getConversionImage(chatMessage.conversionImage));
            txNameRecentConversion.setText(chatMessage.conversionName);
            txRecentMessage.setText(chatMessage.message);
            layoutConversion.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    User user = new User();
                    user.mId = chatMessage.conversionId;
                    user.mName = chatMessage.conversionName;
                    user.mImage = chatMessage.conversionImage;
                    conversionListener.onConversionClicked(user);
                }
            });

        }
    }

    private Bitmap getConversionImage(String encodeImage){
        byte[] bytes = Base64.decode(encodeImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0, bytes.length);
    }
}
