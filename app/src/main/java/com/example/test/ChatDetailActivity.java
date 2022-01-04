package com.example.test;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.devlomi.record_view.OnRecordListener;
import com.example.test.Adapters.ChatAdapter;
import com.example.test.Models.MessageModel;
import com.example.test.Models.Users;
import com.example.test.databinding.ActivityChatDetailBinding;
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
import com.squareup.picasso.Picasso;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class ChatDetailActivity extends AppCompatActivity {
    ActivityChatDetailBinding binding;
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    private Permission permissions;
    String audioPath;
    private MediaRecorder mediaRecorder;

    private String senderID;
    private String reciverID;
    Users me = new Users();
    private LocationManager localisations;
    private ChangementPosition changements = new ChangementPosition();
    private Geocoder geocodage;
    private String loc;
    String URL="https://fcm.googleapis.com/fcm/send";
    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatDetailBinding.inflate(getLayoutInflater());
        geocodage = new Geocoder(this, Locale.FRANCE);
        localisations = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(ChatDetailActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(ChatDetailActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(ChatDetailActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }else{
                ActivityCompat.requestPermissions(ChatDetailActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
        miseAJourDeLaPosition(localisations.getLastKnownLocation(LocationManager.GPS_PROVIDER));
        setContentView(binding.getRoot());


        requestQueue=Volley.newRequestQueue(this);
        getSupportActionBar().hide();
        permissions = new Permission();
        mAuth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance("https://msgapp-d31af-default-rtdb.europe-west1.firebasedatabase.app/");
        storage= FirebaseStorage.getInstance("gs://msgapp-d31af.appspot.com");
        senderID=mAuth.getUid();
        reciverID=getIntent().getStringExtra("userId");
        String username=getIntent().getStringExtra("username");
        String profilePc=getIntent().getStringExtra("profilePic");
        binding.username.setText(username);
        Picasso.get().load(profilePc).placeholder(R.drawable.avatar).into(binding.profileImage);

       database.getReference("users").child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshot) {
               me=snapshot.getValue(Users.class);
           }

           @Override
           public void onCancelled(@NonNull DatabaseError error) {

           }
       });

        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i= new Intent(ChatDetailActivity.this,HomeActivity.class);
                startActivity(i);
            }
        });

        binding.pluse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.msgdataAdd.getVisibility() == View.INVISIBLE)
                showLayout();
           else
                  hideLayout();

            }
        });



        final ArrayList<MessageModel> messageModels=new ArrayList<>();
        final ChatAdapter chatAdapter=new ChatAdapter(messageModels,this,reciverID);

        binding.chatRecycleView.setAdapter(chatAdapter);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        binding.chatRecycleView.setLayoutManager(layoutManager);

        final String senderRoom= senderID+reciverID;
        final String reciverRoom= reciverID+senderID;
        database.getReference().child("chats")
                .child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messageModels.clear();
                        for (DataSnapshot snapshot1:snapshot.getChildren()){
                            MessageModel model=snapshot1.getValue(MessageModel.class);
                            model.setMessageId(snapshot1.getKey());
                            messageModels.add(model);
                        }
                        chatAdapter.notifyDataSetChanged();
                        //scroll down when a new message is sent
                        if(messageModels.size()!=0)
                        binding.chatRecycleView.post(new Runnable() {
                            @Override
                            public void run() {
                                binding.chatRecycleView.smoothScrollToPosition(chatAdapter.getItemCount()-1);
                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = binding.enterMessage.getText().toString();
                if (!message.equals("")) {
                    final MessageModel model = new MessageModel(senderID, message,"text",loc);
                    model.setTimeStamp(new Date().getTime());
                    binding.enterMessage.setText("");

                    database.getReference().child("chats")
                            .child(senderRoom)
                            .push()
                            .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //sendNotification(model.getMessage());
                            database.getReference().child("chats")
                                    .child(reciverRoom)
                                    .push()
                                    .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {

                                }
                            });

                            
                        }
                    });
                }
            }
        });
        //set up the recording
        binding.recordButton.setRecordView(binding.recordView);
        binding.recordButton.setListenForRecord(false);
        binding.recordButton.setOnClickListener(view -> {
            //checking permessions
            if (permissions.isRecordingOk(ChatDetailActivity .this))
                if (permissions.isStorageOk(ChatDetailActivity .this))
                    binding.recordButton.setListenForRecord(true);
                else permissions.requestStorage(ChatDetailActivity .this);
            else permissions.requestRecording(ChatDetailActivity .this);
        });
        binding.recordView.setOnRecordListener(new OnRecordListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onStart() {
                //Start Recording..
                Log.d("RecordView", "onStart");
                setUpRecording();

                try {
                    mediaRecorder.prepare();
                    mediaRecorder.start();

                } catch (IOException e) {
                    e.printStackTrace();
                }


                binding.messageLayout.setVisibility(View.GONE);
                binding.recordView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancel() {
                //On Swipe To Cancel
                Log.d("RecordView", "onCancel");
                mediaRecorder.reset();
                mediaRecorder.release();

                File file = new File(audioPath);
                if (file.exists())
                    file.delete();

                //binding.messageLayout.setVisibility(View.VISIBLE);
                binding.messageLayout.setVisibility(View.VISIBLE);
                binding.recordView.setVisibility(View.GONE);



            }

            @Override
            public void onFinish(long recordTime) {

                Log.d("RecordView", "onFinish");


                try {
                    mediaRecorder.stop();
                    mediaRecorder.release();
                    Toast.makeText(ChatDetailActivity.this, "record stopped ", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }


                binding.recordView.setVisibility(View.GONE);
                binding.messageLayout.setVisibility(View.VISIBLE);
                sendRecodingMessage(audioPath);

            }

            @Override
            public void onLessThanSecond() {
                //When the record time is less than One Second
                Log.d("RecordView", "onLessThanSecond");
                mediaRecorder.reset();
                mediaRecorder.release();

                File file = new File(audioPath);
                if (file.exists())
                    file.delete();



                binding.recordView.setVisibility(View.GONE);
                binding.messageLayout.setVisibility(View.VISIBLE);


            }
        });


        binding.sendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent();
                i.setAction(Intent.ACTION_GET_CONTENT);
                i.setType("image/*");
                startActivityForResult(i,248);

            }
        });

    }

    private void sendNotification(String message) {
        JSONObject jsonObject=new JSONObject();
        try {
            jsonObject.put("to","/topics/"+reciverID);
            JSONObject jsonObject1=new JSONObject();
            jsonObject1.put("title","message from"+mAuth.getCurrentUser().getDisplayName());
            jsonObject1.put("body",message);
            jsonObject.put("notification",jsonObject);
            JsonObjectRequest request=new JsonObjectRequest(Request.Method.POST,URL,jsonObject, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                   Map<String,String> map=new HashMap<>();
                   map.put("content-type","application/json");
                   map.put("authorization","key=AAAAyxV8MN8:APA91bECVXMjPPI-6H_2xv_tT7E-NhQLMkvXz_8iahsolmyLXqLRBXkpsTkZF7qvwUu9yaR2aJ4di8wpPuM1-YKaFRhID5Y_reepbOSAGvRPhd7cPan2GwAAh6AL7Wp1waGTjmTirRgH");
                    return map;
                }
            };
            requestQueue.add(request);
        } catch (JSONException e) {
            Toast.makeText(ChatDetailActivity.this, "i am here bitch ", Toast.LENGTH_SHORT).show();
            Log.d("notif", "sendNotification: "+e.getStackTrace());

        }
    }

    private void sendRecodingMessage(String audioPath) {
        Uri audioFile = Uri.fromFile(new File(audioPath));
        final StorageReference reference=storage.getReference().child("Audio")
                .child(FirebaseAuth.getInstance().getUid()+"/"+System.currentTimeMillis());
        reference.putFile(audioFile).addOnSuccessListener(success->{
            Task<Uri> audioUrl = success.getStorage().getDownloadUrl();
               // Toast.makeText(ChatDetailActivity.this, audioUrl.toString(), Toast.LENGTH_SHORT).show();

                audioUrl.addOnCompleteListener(path->{
                    final String senderRoom= senderID+reciverID;
                    final String reciverRoom= reciverID+senderID;
                    if(path.isSuccessful())
                    {
                        String url = path.getResult().toString();
                        final MessageModel model = new MessageModel(FirebaseAuth.getInstance().getUid(),url, "recording",loc);
                        model.setTimeStamp(new Date().getTime());
                        database.getReference().child("chats")
                                .child(senderRoom)
                                .push()
                                .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                database.getReference().child("chats")
                                        .child(reciverRoom)
                                        .push()
                                        .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {

                                    }
                                });
                            }
                        });

                    }

                });
            });

    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setUpRecording() {

        mediaRecorder = new MediaRecorder();
        try {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "ChatMe/Media/Recording");





            if (!file.exists())
                file.mkdirs();
            audioPath = file.getAbsolutePath() + File.separator + System.currentTimeMillis() + ".m4a";
            File f1=new File(audioPath);
            mediaRecorder.setOutputFile(f1);
        }
        catch(Exception e){
            Log.d("audio","setup media record"+e.getMessage());
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 3000:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (this.permissions.isStorageOk(ChatDetailActivity.this))
                        binding.recordButton.setListenForRecord(true);
                    else this.permissions.requestStorage(ChatDetailActivity.this);

                } else
                    Toast.makeText(this, "Recording permission denied", Toast.LENGTH_SHORT).show();
                break;
            case 1000:

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    binding.recordButton.setListenForRecord(true);
                else
                    Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
                break;



            case 1:
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (ContextCompat.checkSelfPermission(ChatDetailActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }


    class ChangementPosition implements LocationListener {
        public void onLocationChanged(Location position) { miseAJourDeLaPosition(position); }
        public void onStatusChanged(String fournisseur, int statut, Bundle extras) { }
        public void onProviderEnabled(String fournisseur) { }
        public void onProviderDisabled(String fournisseur) { }
    }

    private void miseAJourDeLaPosition(Location position) {
        if (position!=null) {
            double lat = position.getLatitude();
            double lng = position.getLongitude();

            try {
                List<Address> adresses = geocodage.getFromLocation(lat, lng, 1);
                StringBuilder texte = new StringBuilder();
                if (adresses.size() > 0) {
                    Address une = adresses.get(0);

                    texte.append(une.getAddressLine(0));
                    //texte.append(une.getPostalCode()).append(" ").append(une.getLocality()).append("\n");
                    //texte.append(une.getCountryName());
                    Log.d("localisation", "miseAJourDeLaPosition: "+texte.toString());
                    //adresse.setText(texte.toString());
                    loc=texte.toString();
                }
            }
            catch (IOException ex) {
                Log.d("localisation",ex.getStackTrace().toString()+"");
            }
        }
        else
        Toast.makeText(ChatDetailActivity.this, "Nouvelle position", Toast.LENGTH_SHORT).show();
    }
    public void status(String status){
        DatabaseReference refrence = FirebaseDatabase.getInstance("https://msgapp-d31af-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users").child(mAuth.getUid());
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("status",status);
        refrence.updateChildren(hashMap);
    }
    @Override
    protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(ChatDetailActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(ChatDetailActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(ChatDetailActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }else{
                ActivityCompat.requestPermissions(ChatDetailActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
        localisations.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 2, changements);
        localisations.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5, changements);
    }
    @Override
    protected void onStop() {
        super.onStop();
        localisations.removeUpdates(changements);
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

    private void showLayout() {
        RelativeLayout view = binding.msgdataAdd;
        float radius = Math.max(view.getWidth(), view.getHeight());
        Animator animator = ViewAnimationUtils.createCircularReveal(view, view.getLeft(), view.getTop(), 0, radius * 2);
        animator.setDuration(800);
        view.setVisibility(View.VISIBLE);
        animator.start();

    }

    private void hideLayout() {

        RelativeLayout view = binding.msgdataAdd;
        float radius = Math.max(view.getWidth(), view.getHeight());
        Animator animator = ViewAnimationUtils.createCircularReveal(view, view.getLeft(), view.getTop(), radius * 2, 0);
        animator.setDuration(800);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    @Override
    public void onBackPressed() {

        if (binding.msgdataAdd.getVisibility() == View.VISIBLE)
            hideLayout();
        else
            super.onBackPressed();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final String senderRoom= senderID+reciverID;
        final String reciverRoom= reciverID+senderID;
        if(data.getData()!=null){
            Uri sFile=data.getData();
            //binding.profileImage.setImageURI(sFile);
            final StorageReference reference=storage.getReference().child("sentImages")
                    .child(FirebaseAuth.getInstance().getUid()+"/"+ System.currentTimeMillis());
            reference.putFile(sFile).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String message=uri.toString();
                            if (!message.equals("")) {
                                final MessageModel model = new MessageModel(senderID, message,"image",loc);
                                model.setTimeStamp(new Date().getTime());
                                binding.enterMessage.setText("");

                                database.getReference().child("chats")
                                        .child(senderRoom)
                                        .push()
                                        .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        database.getReference().child("chats")
                                                .child(reciverRoom)
                                                .push()
                                                .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {

                                            }
                                        });
                                    }
                                });
                            }
                        }
                    });

                }
            });
            hideLayout();


        }
    }
}