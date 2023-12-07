package com.example.whatsapp20.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.whatsapp20.Adapters.GroupMessageAdapter;
import com.example.whatsapp20.Adapters.MessageAdapter;
import com.example.whatsapp20.Models.Message;
import com.example.whatsapp20.R;
import com.example.whatsapp20.databinding.ActivityGroupChatBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import kotlin.random.URandomKt;

public class GroupChatActivity extends AppCompatActivity {

    ActivityGroupChatBinding binding;
    GroupMessageAdapter adapter;
    ArrayList<Message> Messages;

    FirebaseDatabase database;
    FirebaseStorage storage;
    String reciveruid;
    String senderUid;

    ProgressDialog Dailog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setTitle("Group Chat");
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        Dailog = new ProgressDialog(this);
        Dailog.setMessage("sending image");
        Dailog.setCancelable(false);

        Messages = new ArrayList<>();

        reciveruid = getIntent().getStringExtra("uid");
        senderUid = FirebaseAuth.getInstance().getUid();

        adapter = new GroupMessageAdapter(this, Messages);
        binding.recycle.setLayoutManager(new LinearLayoutManager(this));
        binding.recycle.setAdapter(adapter);

        database.getReference()
                .child("public")
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

                    database.getReference().child("public").push().setValue(message);
                    binding.messagetext.setText("");
                    int itemCount = adapter.getItemCount()-1;
                    binding.recycle.smoothScrollToPosition(itemCount);
                    adapter.notifyDataSetChanged();

                }
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


    }
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

                                            database.getReference()
                                                    .child("public")
                                                    .push()
                                                    .setValue(message);
                                            int itemCount = adapter.getItemCount()-1;
                                            binding.recycle.smoothScrollToPosition(itemCount);
                                            adapter.notifyDataSetChanged();
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
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}