package com.example.test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.test.Models.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {
     Button signup;
     EditText username,mail,password;
    private FirebaseAuth mAuth;
    FirebaseDatabase database;
    ProgressDialog  progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //binding the button and edit texts with the variables
        signup=findViewById(R.id.signup);
        username=findViewById(R.id.username);
        mail=findViewById(R.id.loginMail);
        password=findViewById(R.id.loginPassword);

        //firbase initialisation
         mAuth=FirebaseAuth.getInstance();
         database=FirebaseDatabase.getInstance("https://msgapp-d31af-default-rtdb.europe-west1.firebasedatabase.app/");

         // hiding the action bar from this activity
         getSupportActionBar().hide();

         //set up the progress bar
        progressDialog=new ProgressDialog(SignUpActivity.this);
        progressDialog.setTitle("creating Account");
        progressDialog.setMessage("We are creating your account ");

        //creating the onclick event for the sign up btn
        this.signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if((username.getText().toString().isEmpty())&&(mail.getText().toString().isEmpty())&&(password.getText().toString().isEmpty())){
                    Toast.makeText(SignUpActivity.this,"enter credentials",Toast.LENGTH_LONG).show();
                }
                else{
                    progressDialog.show();
                    mAuth.createUserWithEmailAndPassword(mail.getText().toString(),password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressDialog.dismiss();
                              if(task.isSuccessful()){
                                  Users user= new Users(username.getText().toString(),mail.getText().toString(),password.getText().toString());
                                   String id=task.getResult().getUser().getUid();

                                   database.getReference().child("Users").child(id).setValue(user);
                                  Toast.makeText(SignUpActivity.this, "sign up succssesful", Toast.LENGTH_SHORT).show();


                                  mAuth.signInWithEmailAndPassword(mail.getText().toString(),password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                      @Override
                                      public void onComplete(@NonNull Task<AuthResult> task) {

                                          if(task.isSuccessful()){
                                              Intent i= new Intent(SignUpActivity.this,HomeActivity.class);
                                              startActivity(i);
                                          }
                                          else{
                                              Toast.makeText(SignUpActivity.this, "erreur", Toast.LENGTH_SHORT).show();
                                          }

                                      }
                                  });
                              }
                              else
                              {
                                  Toast.makeText(SignUpActivity.this, "erreur", Toast.LENGTH_SHORT).show();
                              }
                        }
                    });
                }
            }
        });



    }
}