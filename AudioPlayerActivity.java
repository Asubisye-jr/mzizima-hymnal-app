package com.example.mzizimahymnal;

import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import java.io.File;
import java.util.List;

public class AudioPlayerActivity extends AppCompatActivity {

    private AppDatabase db;
    private ListView audioListView;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);

        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "app-database").build();

        audioListView = findViewById(R.id.audioListView);

        int songId = getIntent().getIntExtra("SONG_ID", -1);

        if (songId != -1) {
            loadAudios(songId);
        } else {
            finish();
        }
    }

    private void loadAudios(int songId) {

        new AsyncTask<Void, Void, List<Audio>>() {

            @Override
            protected List<Audio> doInBackground(Void... voids) {
                return db.audioDao().getAudiosForSong(songId);
            }

            @Override
            protected void onPostExecute(List<Audio> audios) {

                if (audios == null || audios.isEmpty()) {
                    Toast.makeText(AudioPlayerActivity.this,
                            "No audio files available",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                List<String> names = new java.util.ArrayList<>();
                for (Audio a : audios) {
                    names.add(a.displayName);
                }

                ArrayAdapter<String> adapter =
                        new ArrayAdapter<>(AudioPlayerActivity.this,
                                android.R.layout.simple_list_item_1,
                                names);

                audioListView.setAdapter(adapter);

                audioListView.setOnItemClickListener((parent, view, position, id) -> {

                    Audio selected = audios.get(position);
                    playAudio(selected.audioPath);
                });
            }

        }.execute();
    }

    private void playAudio(String path) {

        try {

            if (mediaPlayer != null) {
                mediaPlayer.release();
            }

            File file = new File(path);

            if (!file.exists()) {
                Toast.makeText(this,
                        "Audio file not found",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            mediaPlayer.start();

        } catch (Exception e) {
            Toast.makeText(this,
                    "Error playing audio",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        if (db != null && db.isOpen()) {
            db.close();
        }
    }
}