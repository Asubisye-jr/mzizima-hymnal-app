package com.example.mzizimahymnal;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

public class AudioService extends Service {

    private MediaPlayer mediaPlayer;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.song1);
        }

        if (intent != null) {
            String action = intent.getAction();

            if ("PLAY".equals(action)) {
                mediaPlayer.start();

            } else if ("PAUSE".equals(action)) {
                mediaPlayer.pause();

            } else if ("FORWARD".equals(action)) {
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 10000);

            } else if ("REWIND".equals(action)) {
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 10000);
            }
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        super.onDestroy();
    }
}