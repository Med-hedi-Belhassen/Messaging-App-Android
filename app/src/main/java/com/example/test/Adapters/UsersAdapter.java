package com.example.test.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test.ChatDetailActivity;
import com.example.test.HomeActivity;
import com.example.test.MainActivity;
import com.example.test.Models.Users;
import com.example.test.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder>{

    ArrayList<Users> list;
    Context context;

    public UsersAdapter(ArrayList<Users> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.row_show_users,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
         Users users=list.get(position);
        Picasso.get().load(users.getProfilePic()).placeholder(R.drawable.avatar3).into(holder.image);
        holder.userName.setText(users.getUserName());
        //last message
        FirebaseDatabase.getInstance("https://msgapp-d31af-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("chats")
        .child(FirebaseAuth.getInstance().getUid()+users.getUserId())
        .orderByChild("timeStamp")
        .limitToLast(1)
        .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChildren()){
                    for (DataSnapshot snapshot1:snapshot.getChildren()){
                        if(snapshot1.child("type").getValue().toString().equals("text"))
                        holder.LastMessage.setText(snapshot1.child("message").getValue().toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        Log.d("userstatus", "onBindViewHolder: "+users.getStatus());
        if(users.getStatus()==null){
            DatabaseReference refrence = FirebaseDatabase.getInstance("https://msgapp-d31af-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users").child(users.getUserId());
            HashMap<String,Object> hashMap=new HashMap<>();
            hashMap.put("status","offline");
            refrence.updateChildren(hashMap);
        }else if(users.getStatus().equals("online")){
            holder.imageon.setVisibility(View.VISIBLE);
            holder.imageoff.setVisibility(View.GONE);
        }
        else{
            holder.imageon.setVisibility(View.GONE);
            holder.imageoff.setVisibility(View.VISIBLE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i= new Intent(context, ChatDetailActivity.class);
                i.putExtra("userId",users.getUserId());
                i.putExtra("profilePic",users.getProfilePic());
                i.putExtra("username",users.getUserName());
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView image,imageon,imageoff;
        TextView userName,LastMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image=itemView.findViewById(R.id.profile_image);
            userName=itemView.findViewById(R.id.userNameList);
            LastMessage=itemView.findViewById(R.id.LasMessage);
            imageon=itemView.findViewById(R.id.imgon);
            imageoff=itemView.findViewById(R.id.imgoff);

        }
    }

}
