package com.example.test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.test.Adapters.ChatAdapter;
import com.example.test.Models.MessageModel;
import com.example.test.databinding.ActivityGroupChatBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;

public class GroupChatActivity extends AppCompatActivity {
ActivityGroupChatBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityGroupChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();
        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i= new Intent(GroupChatActivity.this,HomeActivity.class);
                startActivity(i);
            }
        });
        final FirebaseDatabase database=FirebaseDatabase.getInstance("https://msgapp-d31af-default-rtdb.europe-west1.firebasedatabase.app/");
        final ArrayList<MessageModel> messageModels=new ArrayList<>();
        final String senderId= FirebaseAuth.getInstance().getUid();
        binding.username.setText("Groupe Chat");
        final ChatAdapter adapter=new ChatAdapter(messageModels,this);
        binding.chatRecycleView.setAdapter(adapter);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        binding.chatRecycleView.setLayoutManager(layoutManager);

        database.getReference().child("group chat").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageModels.clear();
                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                   MessageModel model=dataSnapshot.getValue(MessageModel.class) ;
                   model.setType("text");
                   messageModels.add(model);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              final String message=binding.enterMessage.getText().toString();
              if(!message.equals("")){
                  final MessageModel model=new MessageModel(senderId,message,"text","");
                  model.setTimeStamp(new Date().getTime());
                  binding.enterMessage.setText("");
                  database.getReference().child("group chat")
                          .push()
                          .setValue(model)
                          .addOnSuccessListener(new OnSuccessListener<Void>() {
                              @Override
                              public void onSuccess(Void unused) {

                              }
                          });
              }
            }
        });


    }
}