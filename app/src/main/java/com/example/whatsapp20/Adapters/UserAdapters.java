package com.example.whatsapp20.Adapters;

import android.content.Context;
import android.content.Intent;
import android.opengl.Visibility;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.whatsapp20.Activities.ChatActivity;
import com.example.whatsapp20.R;
import com.example.whatsapp20.Models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapters extends RecyclerView.Adapter<UserAdapters.UserViewHolder> {

    Context context;
    ArrayList<User> users;

    public UserAdapters(Context context,ArrayList<User> users){
        this.context = context;
        this.users = users;

    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_conversation, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
     User user = users.get(position);


     String senderId = FirebaseAuth.getInstance().getUid();
     String senderroom = senderId + user.getUid();
     FirebaseDatabase.getInstance().getReference().child("chats")
                     .child(senderroom)
                             .addValueEventListener(new ValueEventListener() {
                                 @Override
                                 public void onDataChange(@NonNull DataSnapshot snapshot) {
                                     if (snapshot.exists()) {
                                         String lastMsg = snapshot.child("lastMsg").getValue(String.class);
                                         long time = snapshot.child("lastMsgTime").getValue(Long.class);
                                         SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                                         holder.LastMsgtime.setText(dateFormat.format(new Date(time)));
                                         holder.Lastmsg.setText(lastMsg);
                                     }
                                     else {
                                         holder.Lastmsg.setText("Tap to chat");
                                         holder.LastMsgtime.setVisibility(View.GONE);
                                     }
                                 }

                                 @Override
                                 public void onCancelled(@NonNull DatabaseError error) {

                                 }
                             });
     FirebaseDatabase.getInstance().getReference().child("presence").child(user.getUid()).addValueEventListener(new ValueEventListener() {
         @Override
         public void onDataChange(@NonNull DataSnapshot snapshot) {
             if (snapshot.exists()){
                 String status = snapshot.getValue(String.class);
                 if (!status.isEmpty()) {
                     if (status.equals("Offline")){
                         holder.onlinedi.setVisibility(View.GONE);
                     }else {
                         holder.onlinedi.setVisibility(View.VISIBLE);
                     }
                 }
             }
         }

         @Override
         public void onCancelled(@NonNull DatabaseError error) {

         }
     });

     holder.userName.setText(user.getName());
     Glide.with(context).load(user.getProfileImage())
             .placeholder(R.drawable.user_image)
             .into(holder.img);


     holder.itemView.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             Intent intent = new Intent(context, ChatActivity.class);
             intent.putExtra("name",user.getName());
             intent.putExtra("image",user.getProfileImage());
             intent.putExtra("uid", user.getUid());
             context.startActivity(intent);

         }
     });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder{
        TextView userName,Lastmsg,LastMsgtime;
        CircleImageView img;
        ImageView onlinedi;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
             userName = itemView.findViewById(R.id.username);
            img = itemView.findViewById(R.id.image);
            Lastmsg = itemView.findViewById(R.id.lastMsg);
            LastMsgtime = itemView.findViewById(R.id.Msgtime);
            onlinedi = itemView.findViewById(R.id.onlineindicater);

        }


    }

}
