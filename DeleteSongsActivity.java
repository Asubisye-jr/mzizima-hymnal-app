package com.example.mzizimahymnal;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import java.io.File;
import java.util.List;

public class DeleteSongsActivity extends AppCompatActivity {
    private AppDatabase db;
    private ListView songsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_songs);

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "app-database").build();
        songsListView = findViewById(R.id.songsListView);

        loadSongs();
    }

    private void loadSongs() {
        new AsyncTask<Void, Void, List<Song>>() {
            @Override
            protected List<Song> doInBackground(Void... voids) {
                try {
                    return db.songDao().getAllSongs();
                } catch (Exception e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                    return new java.util.ArrayList<>();
                }
            }

            @Override
            protected void onPostExecute(List<Song> songs) {
                ArrayAdapter<Song> adapter = new ArrayAdapter<>(DeleteSongsActivity.this,
                        android.R.layout.simple_list_item_1, songs);
                songsListView.setAdapter(adapter);
                songsListView.setOnItemClickListener((parent, view, position, id) -> {
                    Song song = songs.get(position);
                    new DeleteSongTask().execute(song);
                });
            }
        }.execute();
    }

    private class DeleteSongTask extends AsyncTask<Song, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Song... songs) {
            Song song = songs[0];
            try {
                if (song.sheetPath != null) {
                    File pdfFile = new File(song.sheetPath);
                    if (pdfFile.exists()) {
                        pdfFile.delete();
                    }
                }
                db.songDao().deleteSong(song.songId);
                return true;
            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(DeleteSongsActivity.this, "Song deleted successfully", Toast.LENGTH_SHORT).show();
                loadSongs();
            } else {
                Toast.makeText(DeleteSongsActivity.this, "Failed to delete song", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }
}