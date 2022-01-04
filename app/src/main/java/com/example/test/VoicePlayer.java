package com.example.test;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;

public class VoicePlayer {
    private static VoicePlayer mInstance;
    private Context context;
    private ImageView imgPlay, imgPause;
    private SeekBar seekBar;
    private TextView txtProcess;
    private MediaPlayer mediaPlayer;

    public VoicePlayer(Context context) {
        this.context = context;
    }

    public static synchronized VoicePlayer getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new VoicePlayer(context);
        }
        return mInstance;
    }
    public void init(String path, final ImageView imgPlay, final ImageView imgPause, final SeekBar seekBar, final TextView txtProcess){

        this.imgPlay = imgPlay;
        this.imgPause = imgPause;
        this.seekBar = seekBar;
        this.txtProcess = txtProcess;


        mediaPlayer = new MediaPlayer();
        if (path != null) {
            try {
                mediaPlayer.setDataSource(path);
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.prepare();
                mediaPlayer.setVolume(50, 50);
                //START and PAUSE are in other listeners
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        seekBar.setMax(mp.getDuration());
                        txtProcess.setText("00:00:00/"+convertTime(mp.getDuration() / 1000));
                    }
                });
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        imgPause.setVisibility(View.GONE);
                        imgPlay.setVisibility(View.VISIBLE);
                    }
                });
                Log.d("audioPLay", " audio is playing ");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        this.seekBar.setOnSeekBarChangeListener(seekBarListener);
        this.imgPlay.setOnClickListener(imgPlayClickListener);
        this.imgPause.setOnClickListener(imgPauseClickListener);
    }


    //Convert long milli seconds to a formatted String to display it

    private static String convertTime(long seconds) {
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;
        return String.format("%02d:%02d:%02d", h,m,s);
    }

    //These both functions to avoid mediaplayer errors

    public void onStop(){
        try{
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onPause(){
        try{
            if (mediaPlayer != null){
                if (mediaPlayer.isPlaying())
                    mediaPlayer.pause();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        imgPause.setVisibility(View.GONE);
        imgPlay.setVisibility(View.VISIBLE);
    }



    //Components' listeners

    View.OnClickListener imgPlayClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            imgPause.setVisibility(View.VISIBLE);
            imgPlay.setVisibility(View.GONE);
            mediaPlayer.start();
            try{
                update(mediaPlayer, txtProcess, seekBar, context);
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    };



    private SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser)
            {
                mediaPlayer.seekTo(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            imgPause.setVisibility(View.GONE);
            imgPlay.setVisibility(View.VISIBLE);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            imgPlay.setVisibility(View.GONE);
            imgPause.setVisibility(View.VISIBLE);
            mediaPlayer.start();

        }
    };

    View.OnClickListener imgPauseClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            imgPause.setVisibility(View.GONE);
            imgPlay.setVisibility(View.VISIBLE);
            mediaPlayer.pause();
        }
    };



    //Updating seekBar in realtime
    private void update(final MediaPlayer mediaPlayer, final TextView time, final SeekBar seekBar, final Context context) {
        ((Activity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                if (mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition() > 100) {
                    time.setText(convertTime(mediaPlayer.getCurrentPosition() / 1000) + " / " + convertTime(mediaPlayer.getDuration() / 1000));
                }
                else {
                    time.setText(convertTime(mediaPlayer.getDuration() / 1000));
                    seekBar.setProgress(0);
                }
                Handler handler = new Handler();
                try{
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            try{
                                if (mediaPlayer.getCurrentPosition() > -1) {
                                    try {
                                        update(mediaPlayer, time, seekBar, context);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    };
                    handler.postDelayed(runnable, 2);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });
    }
}
