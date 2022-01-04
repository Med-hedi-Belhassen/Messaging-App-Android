package com.example.test.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test.ChatDetailActivity;
import com.example.test.Models.MessageModel;
import com.example.test.R;
import com.example.test.VoicePlayer;
import com.example.test.VoicePlayer1;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.SimpleTimeZone;

public class ChatAdapter extends RecyclerView.Adapter {
    ArrayList<MessageModel>  messageModels ;
    Context context;
    String reciverID;
    int SENDER_VIEW_TYPE=1;
    int RECIVER_VIEW_TYPE=2;
    int SENDER_RECORD_VIEW_TYPE=3;
    int RECIVER_RECORD_VIEW_TYPE=4;
    int SENDER_IMAGE_VIEW_TYPE=5;
    int RECIVER_IMAGE_VIEW_TYPE=6;



    public ChatAdapter(ArrayList<MessageModel> messageModels, Context context) {
        this.messageModels = messageModels;
        this.context = context;
    }

    public ChatAdapter(ArrayList<MessageModel> messageModels, Context context, String reciverID) {
        this.messageModels = messageModels;
        this.context = context;
        this.reciverID = reciverID;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType==SENDER_VIEW_TYPE){
            View view= LayoutInflater.from(context).inflate(R.layout.row_sender,parent,false);
            return new SenderViewHolder(view);
        }
        else if(viewType==SENDER_RECORD_VIEW_TYPE){
            View view= LayoutInflater.from(context).inflate(R.layout.row_sender_recording,parent,false);
            return new SenderRecordViewHolder(view);
        }
        else if(viewType==SENDER_IMAGE_VIEW_TYPE){
            View view= LayoutInflater.from(context).inflate(R.layout.row_sender_image,parent,false);
            return new SenderImageViewHolder(view);
        }
        else if(viewType==RECIVER_VIEW_TYPE) {
            View view= LayoutInflater.from(context).inflate(R.layout.row_reciver,parent,false);
            return new ReciverViewHolder(view);
        }
        else if(viewType==RECIVER_IMAGE_VIEW_TYPE){
            View view= LayoutInflater.from(context).inflate(R.layout.row_reciver_image,parent,false);
            return new ReciverImageViewHolder(view);
        }
        else{
            View view= LayoutInflater.from(context).inflate(R.layout.row_reciver_recording,parent,false);
            return new ReciverRecordViewHolder(view);
        }
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageModel messageModel =messageModels.get(position);

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("Delete Message")
                        .setMessage("Are you sure u want to delete this message ? ")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseDatabase database=FirebaseDatabase.getInstance("https://msgapp-d31af-default-rtdb.europe-west1.firebasedatabase.app/");
                                String senderRoom=FirebaseAuth.getInstance().getUid()+reciverID;

                                database.getReference().child("chats").child(senderRoom)
                                        .child(messageModel.getMessageId())
                                        .setValue(null);

                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
                return false;
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.getClass()==SenderViewHolder.class){
                    //location show hide
                    if(((SenderViewHolder)holder).senderLoc.getVisibility()==View.GONE)
                    ((SenderViewHolder)holder).senderLoc.setVisibility(View.VISIBLE);
                    else
                        ((SenderViewHolder)holder).senderLoc.setVisibility(View.GONE);
                }
                else if(holder.getClass()==ReciverViewHolder.class){
                    if(((ReciverViewHolder)holder).reciverLoc.getVisibility()==View.GONE)
                        ((ReciverViewHolder)holder).reciverLoc.setVisibility(View.VISIBLE);
                    else
                        ((ReciverViewHolder)holder).reciverLoc.setVisibility(View.GONE);
                }
            }
        });



        if (holder.getClass()==SenderViewHolder.class){
            if(messageModel.getType().equals("text")){
                Log.d("win","message sender");
            ((SenderViewHolder)holder).senderMsg.setText(messageModel.getMessage());
            Date date=new Date(messageModel.getTimeStamp());
            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("h:mm a");
            String strDate=simpleDateFormat.format(date);
            ((SenderViewHolder)holder).senderTime.setText(strDate.toString());}
            ((SenderViewHolder)holder).senderLoc.setText(messageModel.getLocalisation());


        }
        else if (holder.getClass()==ReciverViewHolder.class) {
            if(messageModel.getType().equals("text")) {
                Log.d("win","message reciver");
                ((ReciverViewHolder) holder).reciverMsg.setText(messageModel.getMessage());
                Date date = new Date(messageModel.getTimeStamp());
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm a");
                String strDate = simpleDateFormat.format(date);
                ((ReciverViewHolder) holder).reciverTime.setText(strDate.toString());
                ((ReciverViewHolder)holder).reciverLoc.setText(messageModel.getLocalisation());
            }
        }
        else if (holder.getClass()==SenderRecordViewHolder.class)
        {
            if(messageModel.getType().equals("recording")) {
                Log.d("win","audio sender");
                Date date = new Date(messageModel.getTimeStamp());
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm a");
                String strDate = simpleDateFormat.format(date);
                ((SenderRecordViewHolder) holder).senderTime.setText(strDate.toString());

                /*VoicePlayer.getInstance(context).init(
                        messageModel.getMessage(),
                        ((SenderRecordViewHolder) holder).imgPlay,
                        ((SenderRecordViewHolder) holder).imgPause,
                        ((SenderRecordViewHolder) holder).seekBar,
                        ((SenderRecordViewHolder) holder).txtProcess
                        );*/
                VoicePlayer1 x= new VoicePlayer1(context);
                x.init(
                        messageModel.getMessage(),
                        ((SenderRecordViewHolder) holder).imgPlay,
                        ((SenderRecordViewHolder) holder).imgPause,
                        ((SenderRecordViewHolder) holder).seekBar,
                        ((SenderRecordViewHolder) holder).txtProcess
                );
            }
        }
        else if(holder.getClass()==ReciverRecordViewHolder.class){
            if(messageModel.getType().equals("recording")) {
                Log.d("win","audio reciver ");
                Date date = new Date(messageModel.getTimeStamp());
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm a");
                String strDate = simpleDateFormat.format(date);
                ((ReciverRecordViewHolder) holder).reciverTime.setText(strDate.toString());
               /* VoicePlayer.getInstance(context).init(
                        messageModel.getMessage(),
                        ((ReciverRecordViewHolder) holder).imgPlay,
                        ((ReciverRecordViewHolder) holder).imgPause,
                        ((ReciverRecordViewHolder) holder).seekBar,
                        ((ReciverRecordViewHolder) holder).txtProcess
                );*/
                VoicePlayer1 x= new VoicePlayer1(context);
                 x.init(
                        messageModel.getMessage(),
                        ((ReciverRecordViewHolder) holder).imgPlay,
                        ((ReciverRecordViewHolder) holder).imgPause,
                        ((ReciverRecordViewHolder) holder).seekBar,
                        ((ReciverRecordViewHolder) holder).txtProcess
                );
            }
        }
        else if (holder.getClass()==SenderImageViewHolder.class){
            if(messageModel.getType().equals("image")){
                Log.d("win","message sender IMAGE");
                Date date=new Date(messageModel.getTimeStamp());
                SimpleDateFormat simpleDateFormat=new SimpleDateFormat("h:mm a");
                String strDate=simpleDateFormat.format(date);
                ((SenderImageViewHolder)holder).senderTime.setText(strDate.toString());}
               ((SenderImageViewHolder)holder).senderLoc.setText(messageModel.getLocalisation());
            Picasso.get()
                    .load(messageModel.getMessage())
                    .fit()
                    .placeholder(R.drawable.avatar)
                    .into(((SenderImageViewHolder)holder).senderMsg);

        }
        else if (holder.getClass()==ReciverImageViewHolder.class){
            if(messageModel.getType().equals("image")){
                Log.d("win","message sender IMAGE");
                Date date=new Date(messageModel.getTimeStamp());
                SimpleDateFormat simpleDateFormat=new SimpleDateFormat("h:mm a");
                String strDate=simpleDateFormat.format(date);
                ((ReciverImageViewHolder)holder).reciverTime.setText(strDate.toString());}
            ((ReciverImageViewHolder)holder).reciverLoc.setText(messageModel.getLocalisation());
            Picasso.get()
                    .load(messageModel.getMessage())
                    .placeholder(R.drawable.avatar)
                    .fit()
                    .into(((ReciverImageViewHolder)holder).reciverMsg);

        }
    }

    private String convertTime(int seconds) {
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;
        return String.format("%02d:%02d:%02d", h,m,s);
    }

    @Override
    public int getItemViewType(int position) {

        int x=5;
        if (messageModels.get(position).getUid().equals(FirebaseAuth.getInstance().getUid())){
            if(messageModels.get(position).getType().equals("text")){
              x= SENDER_VIEW_TYPE;}
            else if (messageModels.get(position).getType().equals("recording")) {
                x= SENDER_RECORD_VIEW_TYPE;
            }
            else {
                x=SENDER_IMAGE_VIEW_TYPE;
            }
        }
        else{
            if(messageModels.get(position).getType().equals("text"))
            {
                x= RECIVER_VIEW_TYPE;
            }
            else if(messageModels.get(position).getType().equals("recording")) {
                x=  RECIVER_RECORD_VIEW_TYPE;
            }
            else
                x=RECIVER_IMAGE_VIEW_TYPE;

        }
    return x;
    }

    @Override
    public int getItemCount() {
        return messageModels.size() ;
    }

    public class ReciverViewHolder extends RecyclerView.ViewHolder {
        TextView reciverMsg, reciverTime,reciverLoc;

        public ReciverViewHolder(@NonNull View itemView) {
            super(itemView);

            reciverMsg = itemView.findViewById(R.id.reciverText);
            reciverTime = itemView.findViewById(R.id.reciverTime);
            reciverLoc=itemView.findViewById(R.id.reciverLoc);
        }
    }
        public class SenderViewHolder extends RecyclerView.ViewHolder{
            TextView senderMsg,senderTime,senderLoc;

            public SenderViewHolder(@NonNull View itemView) {
                super(itemView);

                senderMsg=itemView.findViewById(R.id.senderText);
                senderTime=itemView.findViewById(R.id.senderTime);
                senderLoc=itemView.findViewById(R.id.senderLoc);
            }
    }

    public class SenderRecordViewHolder extends RecyclerView.ViewHolder{
        private ImageView imgPlay, imgPause;
        private SeekBar seekBar;
        private TextView txtProcess,senderTime;

        public SenderRecordViewHolder(@NonNull View itemView) {
            super(itemView);
            senderTime = itemView.findViewById(R.id.senderTime1);
            imgPlay = itemView.findViewById(R.id.imgPlay);
            imgPause = itemView.findViewById(R.id.imgPause);
            seekBar = itemView.findViewById(R.id.seekBar);
            txtProcess = itemView.findViewById(R.id.txtTime);


        }


    }

    public class ReciverRecordViewHolder extends RecyclerView.ViewHolder{
        private ImageView imgPlay, imgPause;
        private SeekBar seekBar;
        private TextView txtProcess,reciverTime;
        public ReciverRecordViewHolder(@NonNull View itemView) {
            super(itemView);
            reciverTime=itemView.findViewById(R.id.reciverTime1);
            imgPlay=itemView.findViewById(R.id.imgPlay);
            imgPause=itemView.findViewById(R.id.imgPause);
            seekBar=itemView.findViewById(R.id.seekBar);
            txtProcess=itemView.findViewById(R.id.txtTime);

        }

    }

    public class SenderImageViewHolder extends RecyclerView.ViewHolder{
        TextView senderTime,senderLoc;
        ImageView senderMsg;
        public SenderImageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMsg=itemView.findViewById(R.id.senderImage);
            senderTime=itemView.findViewById(R.id.senderTime);
            senderLoc=itemView.findViewById(R.id.senderLoc);
        }
    }

    public class ReciverImageViewHolder extends RecyclerView.ViewHolder{
        TextView reciverTime,reciverLoc;
        ImageView reciverMsg;
        public ReciverImageViewHolder(@NonNull View itemView) {
            super(itemView);
            reciverMsg = itemView.findViewById(R.id.reciverImage);
            reciverTime = itemView.findViewById(R.id.reciverTime);
            reciverLoc=itemView.findViewById(R.id.reciverLoc);
        }
    }

    }
