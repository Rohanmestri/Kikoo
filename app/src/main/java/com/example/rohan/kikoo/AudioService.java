package com.example.rohan.kikoo;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class AudioService extends Service {

    int mStartMode;
    int index = -1;

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
            Float classID = (Float) extras.get("key");

            if (classID == 1)
                set_audio(R.raw.a,1,true);
            else if (classID == 2)
                set_audio(R.raw.b,2,true);
            else if (classID == 3)
                set_audio(R.raw.c,3,true);
            else if (classID == 4)
                set_audio(R.raw.d,4,true);
            else if (classID == 5)
                set_audio(R.raw.e,5,true);
            else if (classID == 6)
                set_audio(R.raw.f,6,true);
            else if (classID == 7)
                set_audio(R.raw.g,7,true);
            else if (classID == 8)
                set_audio(R.raw.h,8,true);
            else if (classID == 9)
                set_audio(R.raw.i,9,true);
            else if (classID == 10)
                set_audio(R.raw.j,10,true);
            else if (classID == 11)
                set_audio(R.raw.k,11,true);
            else if (classID == 12)
                set_audio(R.raw.l,12,true);
            else if (classID == 13)
                set_audio(R.raw.m,13,true);
            else if (classID == 14)
                set_audio(R.raw.n,14,true);
            else if (classID == 15)
                set_audio(R.raw.o,15,true);
            else if (classID == 16)
                set_audio(R.raw.p,16,true);
            else if (classID == -1)
                set_audio(R.raw.toomany,-1,false);
            else
                set_audio(R.raw.toomany,-1,false);

        }
        return mStartMode;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }

    public void set_audio(int resid,int rec_index,boolean flag)
    {
        MediaPlayer mPlayer = new MediaPlayer();
        if (!mPlayer.isPlaying()){
            mPlayer = MediaPlayer.create(AudioService.this,resid );
            mPlayer.start();
            while (mPlayer.isPlaying()) ;
            mPlayer.release();
        }
        index = rec_index;

        if (flag == true) {
            Intent intent2 = new Intent(AudioService.this, MainActivity.class);
            intent2.putExtra("key",index);
            intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent2);
        }

        else if(flag == false)
        {
            Intent intent1 = new Intent(AudioService.this, DetectLabel.class);
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent1);
        }

    }


}



