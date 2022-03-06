package com.google.chatapp.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.chatapp.R;
import com.google.chatapp.modules.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ChatMessage> listMessage;
    private Bitmap receiverProfileImage;
    private String senderId;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;


    public void setReceiverProfileImage(Bitmap bitmap){
        receiverProfileImage = bitmap;
    }



    public ChatAdapter(List<ChatMessage> listMessage , Bitmap receiverProfileImage , String senderId) {
        this.listMessage = listMessage;
        this.receiverProfileImage = receiverProfileImage;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent , int viewType) {
        if(viewType == VIEW_TYPE_SENT){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_sent_message,parent,false);
            return new SendMessageViewHolder(view);
        }else{
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_received_message,parent,false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder , int position) {
        if(getItemViewType(position) == VIEW_TYPE_SENT){
            ((SendMessageViewHolder) holder).setData(listMessage.get(position));
        }else{
            ((ReceivedMessageViewHolder) holder).setData(listMessage.get(position),receiverProfileImage);
        }
    }

    @Override
    public int getItemCount() {
        if(listMessage != null){
            return listMessage.size();
        }
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        if(listMessage.get(position).senderId.equals(senderId)){
            return VIEW_TYPE_SENT;
        }else{
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class SendMessageViewHolder extends RecyclerView.ViewHolder{
        private TextView txMessSend,txDateTimeMess;
        public SendMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            txMessSend = itemView.findViewById(R.id.textMessage);
            txDateTimeMess =  itemView.findViewById(R.id.textDataTime);
        }
        void setData(ChatMessage chatMessage){
            txMessSend.setText(chatMessage.message);
            txDateTimeMess.setText(chatMessage.datetime);
        }
    }
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder{
        private ImageView imgReceived;
        private TextView txReceived,txReceivedDateTime;
        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            imgReceived = itemView.findViewById(R.id.imageProfile);
            txReceived = itemView.findViewById(R.id.textMessage);
            txReceivedDateTime = itemView.findViewById(R.id.textDataTime);
        }
        void setData(ChatMessage chatMessage, Bitmap receiverProfileImage){
            txReceived.setText(chatMessage.message);
            if(receiverProfileImage != null){
                imgReceived.setImageBitmap(receiverProfileImage);
            }
            txReceivedDateTime.setText(chatMessage.datetime);


        }
    }
}
