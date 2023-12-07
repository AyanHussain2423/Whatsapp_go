package com.example.whatsapp20.Adapters;

import static android.content.Intent.getIntent;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.whatsapp20.Activities.GroupChatActivity;
import com.example.whatsapp20.Models.Message;
import com.example.whatsapp20.Models.User;
import com.example.whatsapp20.R;
import com.example.whatsapp20.databinding.ItemRecieveBinding;
import com.example.whatsapp20.databinding.ItemSentBinding;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class GroupMessageAdapter extends RecyclerView.Adapter {
    Context context;
    ArrayList<Message> messages;

    final int ITEM_SENT = 1;
    final int ITEM_RECEIVE = 2;



    public GroupMessageAdapter(Context context, ArrayList<Message> messages) {

        this.context = context;
        this.messages = messages;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == ITEM_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_sent, parent, false);
            return new SenderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_recieve, parent, false);
            return new RecieverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        int reactions[] = new int[]{
                R.drawable.ic_fb_like,
                R.drawable.ic_fb_love,
                R.drawable.ic_fb_laugh,
                R.drawable.ic_fb_wow,
                R.drawable.ic_fb_sad,
                R.drawable.ic_fb_angry
        };
        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(reactions)
                .build();


        ReactionPopup popup = new ReactionPopup(context, config, (pos) -> {
            if (pos<0)
                return false;

            if(holder.getClass() == SenderViewHolder.class){
                SenderViewHolder viewHolder = (SenderViewHolder) holder;
                viewHolder.binding.reactionr.setImageResource(reactions[pos]);
                viewHolder.binding.reactionr.setVisibility(View.VISIBLE);
            }
            else {
                RecieverViewHolder viewHolder = (RecieverViewHolder) holder;
                viewHolder.binding.reactionl.setImageResource(reactions[pos]);
                viewHolder.binding.reactionl.setVisibility(View.VISIBLE);
            }
            message.setFeeling(pos);
            return true; // true is closing popup, false is requesting a new selection
        });

        if(holder.getClass() == SenderViewHolder.class){
            SenderViewHolder viewHolder = (SenderViewHolder) holder;
            if (message.getMessage().equals("photo")){
                viewHolder.binding.imagechat.setVisibility(View.VISIBLE);
                viewHolder.binding.Sendtext.setVisibility(View.GONE);
                Glide.with(context).load(message.getImageUrl())
                        .placeholder(R.drawable.imageplaceholder)
                        .into(viewHolder.binding.imagechat);
            }
            if (message.getMessage().equals("cphoto")){
                viewHolder.binding.imagechat.setVisibility(View.VISIBLE);
                viewHolder.binding.Sendtext.setVisibility(View.GONE);
                Glide.with(context).load(message.getImageUrl())
                        .placeholder(R.drawable.imageplaceholder)
                        .into(viewHolder.binding.imagechat);
            }
            String senderId = FirebaseAuth.getInstance().getUid();
            String senderroom = senderId + message.getSenderId();
                FirebaseDatabase.getInstance().getReference().child("public")
                        .child(message.getMessageId())
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    Message time = snapshot.getValue(Message.class);
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                                    viewHolder.binding.sendtime.setVisibility(View.VISIBLE);
                                    viewHolder.binding.sendtime.setText(dateFormat.format(new Date(time.getTimestamp())));
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });// under update
            FirebaseDatabase.getInstance().getReference().child("Users")
                    .child(message.getSenderId())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                               User time = snapshot.getValue(User.class);
                                viewHolder.binding.Usernametext.setVisibility(View.VISIBLE);
                                viewHolder.binding.Usernametext.setText(time.getName());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });// under update

            viewHolder.binding.Sendtext.setText(message.getMessage());


            viewHolder.binding.Sendtext.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v,event);
                    return false;
                }
            });
            viewHolder.binding.imagechat.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v,event);
                    return false;
                }
            });
        }else {
            RecieverViewHolder viewHolder = (RecieverViewHolder) holder;

            if (message.getMessage().equals("photo")){
                viewHolder.binding.imagechat.setVisibility(View.VISIBLE);
                viewHolder.binding.RecieveText.setVisibility(View.GONE);
                Glide.with(context).load(message.getImageUrl())
                        .placeholder(R.drawable.imageplaceholder)
                        .into(viewHolder.binding.imagechat);
            }
            if (message.getMessage().equals("cphoto")){
                viewHolder.binding.imagechat.setVisibility(View.VISIBLE);
                viewHolder.binding.RecieveText.setVisibility(View.GONE);
                Glide.with(context).load(message.getImageUrl())
                        .placeholder(R.drawable.imageplaceholder)
                        .into(viewHolder.binding.imagechat);
            }
            String senderId = FirebaseAuth.getInstance().getUid();
            String senderroom = senderId + message.getSenderId();
            FirebaseDatabase.getInstance().getReference().child("public")
                    .child(message.getMessageId())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                Message time = snapshot.getValue(Message.class);
                                SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                                viewHolder.binding.sendtime.setVisibility(View.VISIBLE);
                                viewHolder.binding.sendtime.setText(dateFormat.format(new Date(time.getTimestamp())));
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });// under update
            FirebaseDatabase.getInstance().getReference().child("Users")
                    .child(message.getSenderId())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){

                                User time = snapshot.getValue(User.class);
                                viewHolder.binding.Usernametext.setVisibility(View.VISIBLE);
                                viewHolder.binding.Usernametext.setText(time.getName());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });// under update
            viewHolder.binding.RecieveText.setText(message.getMessage());

            viewHolder.binding.RecieveText.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v,event);
                    return false;

                }
            });
            viewHolder.binding.imagechat.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v,event);
                    return false;
                }
            });
        }
    }


    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if(FirebaseAuth.getInstance().getUid().equals(message.getSenderId())) {
            return ITEM_SENT;
        } else {
            return ITEM_RECEIVE;
        }
    }


    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class RecieverViewHolder extends RecyclerView.ViewHolder{

        ItemRecieveBinding binding;

        public RecieverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemRecieveBinding.bind(itemView);

        }
    }
    public class SenderViewHolder extends RecyclerView.ViewHolder{
        ItemSentBinding binding;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemSentBinding.bind(itemView);

        }
    }

}

