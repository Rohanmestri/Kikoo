package com.example.rohan.kikoo;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class FeedbackService extends Service {

    int mStartMode;

    public IBinder onBind(Intent arg0) {

        return null;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent,flags, startId);
        Bundle extras = intent.getExtras();
        if(extras == null)
            Log.d("Service","null");
        else {
            Log.d("Service", "not null");
            Float feedbackKey = (Float) extras.get("feedbackKey");
            int label = (int) extras.get("label");


            MediaPlayer mPlayer = new MediaPlayer();
            if (!mPlayer.isPlaying()) {
                if (feedbackKey == 1) {
                    mPlayer = MediaPlayer.create(FeedbackService.this, R.raw.wrong_answer);
                    mPlayer.start();
                    while (mPlayer.isPlaying()) ;
                    mPlayer.release();
                    Intent intent1 = new Intent(FeedbackService.this, DetectLabel.class);
                    intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent1);

                } else if (feedbackKey == 2) {
                    mPlayer = MediaPlayer.create(FeedbackService.this, R.raw.correct_answer);
                    mPlayer.start();
                    while (mPlayer.isPlaying()) ;
                    mPlayer.release();
                    Intent intent2 = new Intent(FeedbackService.this, DetectLabel.class);
                    intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent2);
                }else if (feedbackKey == 3) {
                    mPlayer = MediaPlayer.create(FeedbackService.this, R.raw.inside_box);
                    mPlayer.start();
                    while (mPlayer.isPlaying()) ;
                    mPlayer.release();
                    Intent intent2 = new Intent(FeedbackService.this, MainActivity.class);
                    intent2.putExtra("key",label);
                    intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent2);
                }
            }
        }
        return mStartMode;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }


}



