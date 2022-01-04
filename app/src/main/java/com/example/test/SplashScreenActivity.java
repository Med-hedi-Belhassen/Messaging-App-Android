package com.example.test;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;


import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class SplashScreenActivity extends AppCompatActivity {
ImageView logo ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //get the Imageview by it's ID
        logo=findViewById(R.id.logo);
        //Hide the Action Bar
        getSupportActionBar().hide();

        // the we write this code to make our Activity show app for 5s
        Handler handler = new Handler();
        setContentView(R.layout.activity_splash_screen);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Runnable runnable= new Runnable() {
            @Override
            public void run() {
                //after 5s we will move to login Activity in this Intent
                Intent i= new Intent(SplashScreenActivity.this,MainActivity.class);
                getIntent().setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
            }
        };
        handler.postDelayed(runnable,5000);
    }




}