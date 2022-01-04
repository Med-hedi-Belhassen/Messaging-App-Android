package com.example.test.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.test.Adapters.UsersAdapter;
import com.example.test.Models.Users;
import com.example.test.R;
import com.example.test.databinding.FragmentChatsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatsFragment extends Fragment {



    public ChatsFragment() {
        // Required empty public constructor
    }



   ArrayList<Users> list=new ArrayList<>();
    FirebaseDatabase database;
    FragmentChatsBinding binding;
    String me;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding=FragmentChatsBinding.inflate(inflater,container,false);



        database=FirebaseDatabase.getInstance("https://msgapp-d31af-default-rtdb.europe-west1.firebasedatabase.app/");
        UsersAdapter adapter=new UsersAdapter(list,getContext());
        binding.chatRecycleView.setAdapter(adapter);
        LinearLayoutManager layoutManager= new LinearLayoutManager(getContext());
        binding.chatRecycleView.setLayoutManager(layoutManager);
         me=FirebaseAuth.getInstance().getUid();
         database.getReference().child("Users").addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot snapshot) {
                 list.clear();
                 for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                     Users users=dataSnapshot.getValue(Users.class);
                     users.setUserId(dataSnapshot.getKey());
                     //not show current connected user
                     if(!users.getUserId().equals(FirebaseAuth.getInstance().getUid())){
                         list.add(users);
                     }

                 }
                 adapter.notifyDataSetChanged();
             }

             @Override
             public void onCancelled(@NonNull DatabaseError error) {

             }
         });
        // Inflate the layout for this fragment
        return binding.getRoot();
    }
    public void status(String status){
        DatabaseReference refrence = FirebaseDatabase.getInstance("https://msgapp-d31af-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users").child(me);
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("status",status);
        refrence.updateChildren(hashMap);
    }

    @Override
    public void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    public void onPause() {
        super.onPause();
        status("ofline");
    }
}