package com.example.mzizimahymnal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SongActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);

        TextView title = findViewById(R.id.songTitleTextView);
        TextView lyrics = findViewById(R.id.lyricsTextView);

        Button play = findViewById(R.id.btnPlay);
        Button pause = findViewById(R.id.btnPause);
        Button forward = findViewById(R.id.btnForward);
        Button rewind = findViewById(R.id.btnRewind);

        // Receive data
        String songTitle = getIntent().getStringExtra("title");
        String songLyrics = getIntent().getStringExtra("lyrics");

        title.setText(songTitle);
        lyrics.setText(songLyrics);

        play.setOnClickListener(v -> {
            Intent intent = new Intent(this, AudioService.class);
            intent.setAction("PLAY");
            startService(intent);
        });

        pause.setOnClickListener(v -> {
            Intent intent = new Intent(this, AudioService.class);
            intent.setAction("PAUSE");
            startService(intent);
        });

        forward.setOnClickListener(v -> {
            Intent intent = new Intent(this, AudioService.class);
            intent.setAction("FORWARD");
            startService(intent);
        });

        rewind.setOnClickListener(v -> {
            Intent intent = new Intent(this, AudioService.class);
            intent.setAction("REWIND");
            startService(intent);
        });
    }
}