package com.example.test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.test.Models.Users;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
      TextView register;
      Button login;
      EditText mail,password;
      ImageView google;
      ImageView facebook;
     FirebaseAuth mAuth;
    FirebaseDatabase database;
    ProgressDialog progressDialog;
    GoogleSignInClient mGoogleSignInClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        register=findViewById(R.id.RegisterHere);
        login=findViewById(R.id.signup);
        mail=findViewById(R.id.loginMail);
        password=findViewById(R.id.loginPassword);
        google=findViewById(R.id.gmailButt);
        facebook=findViewById(R.id.fbButt);

        //firbase initialisation
        mAuth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance("https://msgapp-d31af-default-rtdb.europe-west1.firebasedatabase.app/");


        //set up the progress bar
        progressDialog=new ProgressDialog(MainActivity.this);
        progressDialog.setTitle("Login");
        progressDialog.setMessage("Please wait \n Validation in progress");

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("872238821599-7lbnu8k56qu748sjf0v48080s9oh9jhh.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //login Action :

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if((mail.getText().toString().isEmpty())&&(password.getText().toString().isEmpty())){
                    Toast.makeText(MainActivity.this, "check your mail or password", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    progressDialog.show();
                    mAuth.signInWithEmailAndPassword(mail.getText().toString(),password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressDialog.dismiss();
                            if(task.isSuccessful()){
                                Intent i= new Intent(MainActivity.this,HomeActivity.class);
                                startActivity(i);
                            }
                            else{
                                Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

                }
            }
        });

        //rester connecter
       if(mAuth.getCurrentUser()!=null){
           Intent i= new Intent(MainActivity.this,HomeActivity.class);
           startActivity(i);
       }
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i= new Intent(MainActivity.this,SignUpActivity.class);
                startActivity(i);
            }
        });

     google.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             signIn();
         }
     });
     facebook.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             Intent i= new Intent(MainActivity.this,FacebookAuthActivity.class);
             i.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
             startActivity(i);
         }
     });
    }
    int RC_SIGN_IN=65;
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("TAG", "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("TAG", "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            Users users= new Users();
                            users.setUserId(user.getUid());
                            users.setUserName(user.getDisplayName());
                            users.setMail(user.getEmail());
                            users.setProfilePic(user.getPhotoUrl().toString());
                            database.getReference().child("Users").child(user.getUid()).setValue(users);
                            Intent i= new Intent(MainActivity.this,HomeActivity.class);
                            startActivity(i);
                            Toast.makeText(MainActivity.this, "signed in with google", Toast.LENGTH_SHORT).show();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithCredential:failure", task.getException());

                        }
                    }
                });
    }

}