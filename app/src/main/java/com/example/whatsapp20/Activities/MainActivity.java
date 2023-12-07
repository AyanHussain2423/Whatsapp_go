package com.example.whatsapp20.Activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.Touch;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.whatsapp20.Adapters.TopStatusAdapter;
import com.example.whatsapp20.Models.Status;
import com.example.whatsapp20.Models.UserStatus;
import com.example.whatsapp20.R;
import com.example.whatsapp20.Models.User;
import com.example.whatsapp20.Adapters.UserAdapters;
import com.example.whatsapp20.databinding.ActivityChatBinding;
import com.example.whatsapp20.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
   RecyclerView recyclerView,statusList;
   BottomNavigationView bottomNavigationView;
   FirebaseDatabase database,database2;
   ArrayList<User> users;
   UserAdapters userAdapters;
   TopStatusAdapter statusAdapter;
    ImageView onlineindi;
   ArrayList<UserStatus> userStatuses;
   ProgressDialog dialog;
   User user;
    String mAuth;
    String reciveruid;
    @Override
    protected void onCreate(Bundle savedInstancesState){
        super.onCreate(savedInstancesState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading image...");
        dialog.setCancelable(false);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        recyclerView = findViewById(R.id.recyclerview);
        onlineindi = findViewById(R.id.onlineindicater);
        statusList = findViewById(R.id.status);
        database = FirebaseDatabase.getInstance().getReference("Users").getDatabase();
        mAuth = FirebaseAuth.getInstance().getUid();
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        users = new ArrayList<>();
        userStatuses = new ArrayList<>();

        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                     user = snapshot.getValue(User.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        userAdapters = new UserAdapters(this,users);
        statusAdapter = new TopStatusAdapter(this,userStatuses);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        statusList.setLayoutManager(linearLayoutManager);

        statusList.setAdapter(statusAdapter);
       //binding.recyclerview.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(userAdapters);


        database.getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                users.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    User user = snapshot1.getValue(User.class);
                    if(!user.getUid().equals(FirebaseAuth.getInstance().getUid())){
                        users.add(user);
                    }
                    userAdapters.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                //to access status button and finding out menu
                if (item.getItemId() == R.id.statusmenu) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent, 22);
                    return true;
                }
                else if (item.getItemId() == R.id.calls){
                    Intent intent = new Intent(MainActivity.this,GroupChatActivity.class);
                    startActivity(intent);
                }
                return false;
            }
        });

        database.getReference().child("stories").addValueEventListener(new ValueEventListener() {
            //status
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userStatuses.clear();
                    for (DataSnapshot storySnaoshot : snapshot.getChildren()){
                        UserStatus status = new UserStatus();
                        status.setName((storySnaoshot.child("name").getValue(String.class)));
                        status.setProfileImage(storySnaoshot.child("profileImage").getValue(String.class));
                        status.setLastUpdate(storySnaoshot.child("lastUpdate").getValue(Long.class));
                        ArrayList<Status> statuses = new ArrayList<>();
                        for(DataSnapshot statusSnap : storySnaoshot.child("statuses").getChildren()){
                            Status sampleStatus = statusSnap.getValue(Status.class);
                            statuses.add(sampleStatus);
                        }
                        status.setStatuses(statuses);
                        userStatuses.add(status);
                    }
                    statusAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    protected void onResume() {//online
        super.onResume();
            String currentid = FirebaseAuth.getInstance().getUid();
            database.getReference().child("presence").child(currentid).setValue("Online");
    }

    @Override
    protected void onPause() {//offline
        super.onPause();
        String currentid = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentid).setValue("Offline");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {//when status is uploading
        super.onActivityResult(requestCode, resultCode, data);
        if(data != null){
            if(data.getData() != null){
                dialog.show();
                FirebaseStorage storage = FirebaseStorage.getInstance();
                Date date = new Date();
                StorageReference reference = storage.getReference().child("status").child(date.getTime() +"");
                reference.putFile(data.getData()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful())
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                UserStatus userStatus = new UserStatus();
                                userStatus.setName(user.getName());
                                userStatus.setProfileImage(user.getProfileImage());
                                userStatus.setLastUpdate(date.getTime());

                                HashMap<String, Object> obj = new HashMap<>();
                                obj.put("name",userStatus.getName());
                                obj.put("profileImage", userStatus.getProfileImage());
                                obj.put("lastUpdate",userStatus.getLastUpdate());

                                String imageUrl = uri.toString();
                                Status status = new Status(imageUrl, userStatus.getLastUpdate());



                                database.getReference()
                                        .child("stories")
                                                .child(FirebaseAuth.getInstance().getUid())
                                                        .updateChildren(obj);

                                database.getReference().child("stories")
                                                .child(FirebaseAuth.getInstance().getUid())
                                                        .child("statuses")
                                                                .push().setValue(status);
                                dialog.dismiss();
                            }

                        });
                    }
                });
            }
        }
    }
}
