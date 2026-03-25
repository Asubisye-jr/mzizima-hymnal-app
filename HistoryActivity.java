package com.example.mzizimahymnal;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HistoryActivity extends AppCompatActivity {
    private static final String TAG = "HistoryActivity";
    private ListView historyListView;
    private AppDatabase db;
    private ExecutorService executorService;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        db = AppDatabase.createDatabase(getApplicationContext());
        if (db == null) {
            Log.e(TAG, "Database initialization failed");
            Toast.makeText(this, "Database error", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        historyListView = findViewById(R.id.historyListView);
        if (historyListView == null) {
            Log.e(TAG, "History list view not found");
            Toast.makeText(this, "UI initialization failed", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadHistory();
    }

    private void loadHistory() {
        executorService.execute(() -> {
            try {
                List<Song> historySongs = db.historyDao().getHistorySongs();
                mainHandler.post(() -> {
                    if (!historySongs.isEmpty()) {
                        ArrayAdapter<Song> adapter = new ArrayAdapter<>(HistoryActivity.this,
                                android.R.layout.simple_list_item_1, historySongs);
                        historyListView.setAdapter(adapter);
                        historyListView.setOnItemClickListener((parent, view, position, id) -> {
                            Song song = historySongs.get(position);
                            // Optional: Navigate to LyricsViewActivity if needed
                            // Intent intent = new Intent(HistoryActivity.this, LyricsViewActivity.class);
                            // intent.putExtra("SONG_ID", song.songId);
                            // startActivity(intent);
                        });
                    } else {
                        historyListView.setAdapter(null);
                        Toast.makeText(HistoryActivity.this, "No history available", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading history: " + e.getMessage(), e);
                mainHandler.post(() -> Toast.makeText(HistoryActivity.this, "Failed to load history", Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        if (db != null && db.isOpen()) {
            db.close();
            Log.d(TAG, "Database closed");
        }
    }
}