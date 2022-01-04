package com.example.test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.test.Adapters.FragmentAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

public class HomeActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    ViewPager pager;
    TabLayout tablayout;
    String me="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mAuth=FirebaseAuth.getInstance();
        // Define ActionBar object
        ActionBar actionBar;
        actionBar = getSupportActionBar();
        // Define ColorDrawable object and parse color
        // using parseColor method
        // with color hash code as its parameter
        ColorDrawable colorDrawable
                = new ColorDrawable(Color.parseColor("#492B7C"));
        // Set BackgroundDrawable
        actionBar.setBackgroundDrawable(colorDrawable);

        //
        pager=findViewById(R.id.viewPager);
        tablayout=findViewById(R.id.tabLayout);

        me=mAuth.getUid();
        FirebaseMessaging.getInstance().subscribeToTopic(me);
        //
        pager.setAdapter(new FragmentAdapter(getSupportFragmentManager()));
        tablayout.setupWithViewPager(pager);
        //firbase initialisation


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.appbarmenu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.settings :
               // Toast.makeText(HomeActivity.this, "settings is clicked", Toast.LENGTH_SHORT).show();
                Intent il= new Intent(HomeActivity.this,SettingsActivity.class);
                startActivity(il);
                break;

            case R.id.groupchat:
                //Toast.makeText(HomeActivity.this, "group chat is clicked", Toast.LENGTH_SHORT).show();
                Intent in= new Intent(HomeActivity.this,GroupChatActivity.class);
                startActivity(in);
                break;

            case R.id.logout:
                status("offline");
                mAuth.signOut();
                Intent i= new Intent(HomeActivity.this,MainActivity.class);
                startActivity(i);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    public void status(String status){
        DatabaseReference refrence = FirebaseDatabase.getInstance("https://msgapp-d31af-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users").child(me);
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("status",status);
        refrence.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    }
}