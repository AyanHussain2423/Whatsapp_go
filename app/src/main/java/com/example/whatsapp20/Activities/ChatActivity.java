package com.example.whatsapp20.Activities;

import static android.content.Intent.getIntent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.whatsapp20.Adapters.MessageAdapter;
import com.example.whatsapp20.Models.Message;
import com.example.whatsapp20.R;
import com.example.whatsapp20.databinding.ActivityChatBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;
    MessageAdapter adapter;
    ArrayList<Message> Messages;

    FirebaseDatabase database;
    FirebaseStorage storage;

    ProgressDialog Dailog;
    String senderRoom, reciverRoom;

    String reciveruid;
    String senderUid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        Dailog = new ProgressDialog(this);
        Dailog.setMessage("sending image");
        Dailog.setCancelable(false);

        Messages = new ArrayList<>();
        adapter = new MessageAdapter(this,Messages,senderRoom,reciverRoom);
        binding.recycle.setLayoutManager(new LinearLayoutManager(this));
        binding.recycle.setAdapter(adapter);

        String profileImage = getIntent().getStringExtra("image");
        String name = getIntent().getStringExtra("name");
        String ss= getIntent().getStringExtra("lasttimemsg");

        binding.name.setText(name);
        Glide.with(ChatActivity.this).load(profileImage)
                .placeholder(R.drawable.imageplaceholder)
                .into(binding.userimage);

        reciveruid = getIntent().getStringExtra("uid");
        senderUid = FirebaseAuth.getInstance().getUid();

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();


        senderRoom = senderUid + reciveruid;
        reciverRoom = reciveruid + senderUid;


        binding.imageView4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        database.getReference().child("presence").child(reciveruid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){

                    String status = snapshot.getValue(String.class);
                    if (!status.isEmpty()) {
                        if (status.equals("Offline")){
                            binding.status.setVisibility(View.GONE);
                        }else {
                            binding.status.setText(status);
                            binding.status.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        database.getReference()
                .child("chats")
                .child(senderRoom)
                .child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Messages.clear();
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            Message message =  snapshot1.getValue(Message.class);
                            message.setMessageId(snapshot1.getKey());
                            Messages.add(message);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

        binding.sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (binding.messagetext.getText().toString().isEmpty()) {

                }
                else if (binding.messagetext.getText().toString().equals("andy")) {
                 Toast.makeText(getApplicationContext(),"Not available Word" ,Toast.LENGTH_SHORT ).show();
                }
                else {
                String messagetext = binding.messagetext.getText().toString();

                Date date = new Date();
                Message message = new Message(messagetext, senderUid, date.getTime());

                String randonKey = database.getReference().push().getKey();
                message.setMessageId(randonKey);

                HashMap<String, Object> lastMsgObj = new HashMap<>();
                lastMsgObj.put("lastMsg", message.getMessage());
                lastMsgObj.put("lastMsgTime", date.getTime());


                database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                database.getReference().child("chats").child(reciverRoom).updateChildren(lastMsgObj);

                database.getReference().child("chats")
                        .child(senderRoom)
                        .child("messages")
                        .child(randonKey)
                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                database.getReference().child("chats")
                                        .child(reciverRoom)
                                        .child("messages")
                                        .child(randonKey)
                                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                int itemCount = adapter.getItemCount()-1;
                                                binding.recycle.smoothScrollToPosition(itemCount);
                                                binding.messagetext.setText("");
                                                adapter.notifyDataSetChanged();
                                            }
                                        });
                            }
                        });

            }
        }
        });

        binding.cameraicon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        binding.attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 25);

            }
        });

        Handler handler = new Handler();
        binding.messagetext.addTextChangedListener(new TextWatcher() {//typing chnage
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(binding.messagetext.equals("")){
                    database.getReference().child("presence").child(senderUid).setValue("Online");
                }else {
                    database.getReference().child("presence").child(senderUid).setValue("Typing...");
                    handler.removeCallbacksAndMessages(null);
                    handler.postDelayed(userstopptypying, 1000);
                }
            }
            Runnable userstopptypying = new Runnable() {
                @Override
                public void run() {
                    database.getReference().child("presence").child(senderUid).setValue("Online");
                }
            };
        });


        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //getSupportActionBar().setTitle(name);

       // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == 25) {
                if (data != null) {
                    if (data.getData() != null) {
                        Uri selectedImage = data.getData();
                        Calendar calendar = Calendar.getInstance();
                        StorageReference reference = storage.getReference().child("chats").child(calendar.getTimeInMillis() + "");
                        Dailog.show();
                        reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                Dailog.dismiss();
                                if (task.isSuccessful()) {
                                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            String fikePath = uri.toString();

                                            String messagetext = binding.messagetext.getText().toString();

                                            Date date = new Date();
                                            Message message = new Message(messagetext, senderUid, date.getTime());
                                            message.setMessage("photo");
                                            message.setImageUrl(fikePath);

                                            String randonKey = database.getReference().push().getKey();
                                            message.setMessageId(randonKey);

                                            HashMap<String, Object> lastMsgObj = new HashMap<>();
                                            lastMsgObj.put("lastMsg", message.getMessage());
                                            lastMsgObj.put("lastMsgTime", date.getTime());


                                            database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                                            database.getReference().child("chats").child(reciverRoom).updateChildren(lastMsgObj);

                                            database.getReference().child("chats")
                                                    .child(senderRoom)
                                                    .child("messages")
                                                    .child(randonKey)
                                                    .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            database.getReference().child("chats")
                                                                    .child(reciverRoom)
                                                                    .child("messages")
                                                                    .child(randonKey)
                                                                    .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void unused) {
                                                                            int itemCount = adapter.getItemCount()-1;
                                                                            binding.recycle.smoothScrollToPosition(itemCount);
                                                                            binding.messagetext.setText("");
                                                                            adapter.notifyDataSetChanged();
                                                                        }
                                                                    });
                                                        }
                                                    });
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            }

            }
        }


    @Override
    protected void onResume() {//online
        super.onResume();
        String currentid = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentid).setValue("Online");
        int itemCount = adapter.getItemCount();

    }
    @Override
    protected void onPause() {
        super.onPause();
        String currentid = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentid).setValue("Offline");
    }
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}