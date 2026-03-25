package com.example.mzizimahymnal;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LyricsViewActivity extends AppCompatActivity {
    private static final String TAG = "LyricsViewActivity";
    private AppDatabase db;
    private TextView songTitleTextView;
    private TextView lyricsTextView;
    private ImageView sheetIcon;
    private ExecutorService executorService;
    private Handler mainHandler;
    private ImageView audioIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_lyrics_view);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(new RuntimeException("Failed to set content view: " + e.getMessage(), e));
            Toast.makeText(this, "Error loading UI", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        audioIcon = findViewById(R.id.audioIcon);

        db = AppDatabase.createDatabase(getApplicationContext());
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        songTitleTextView = findViewById(R.id.songTitleTextView);
        lyricsTextView = findViewById(R.id.lyricsTextView);
        sheetIcon = findViewById(R.id.sheetIcon);

        int songId = getIntent().getIntExtra("SONG_ID", -1);
        if (songId != -1) {
            fetchSong(songId);
            recordHistory(songId); // Record the song view in history
        } else {
            Toast.makeText(this, "Invalid song", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fetchSong(int songId) {
        setupAudioIcon(songId);
        executorService.execute(() -> {
            try {
                Song song = db.songDao().getSongById(songId);
                mainHandler.post(() -> {
                    if (song != null) {
                        songTitleTextView.setText(song.title);
                        lyricsTextView.setText(song.lyrics);
                        setupSheetIcon(song, songId);
                    } else {
                        Toast.makeText(this, "Song not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                mainHandler.post(() -> {
                    Toast.makeText(this, "Error loading song", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void setupAudioIcon(int songId) {

            audioIcon.setOnClickListener(v -> {

                Intent intent = new Intent(this, AudioPlayerActivity.class);
                intent.putExtra("SONG_ID", songId);
                startActivity(intent);
            });
    }

    private void setupSheetIcon(Song song, int songId) {
        sheetIcon.setOnClickListener(v -> {
            if (song.sheetPath != null && !song.sheetPath.isEmpty()) {
                Log.d(TAG, "Sheet path: " + song.sheetPath);
                launchPdfViewer(song.sheetPath, songId);
            } else {
                Toast.makeText(this, "No sheet available", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void launchPdfViewer(String pdfPath, int songId) {
        try {
            File pdfFile;
            if (pdfPath.startsWith("/storage/emulated/0/") || pdfPath.startsWith("/")) {
                pdfFile = new File(pdfPath);
            } else {
                pdfFile = new File(getExternalFilesDir("pdfs"), pdfPath);
            }

            if (!pdfFile.exists()) {
                Toast.makeText(this, "PDF file not found: " + pdfFile.getName(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "PDF file does not exist: " + pdfFile.getAbsolutePath());
                return;
            }

            Uri uri = FileProvider.getUriForFile(this, "com.example.mzizimahymnal.fileprovider", pdfFile);
            Intent intent = new Intent(this, PdfViewerActivity.class);
            intent.setData(uri);
            intent.putExtra("SONG_ID", songId);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
            Log.d(TAG, "Launched PdfViewerActivity with URI: " + uri + ", songId: " + songId);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            Toast.makeText(this, "Error opening PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error launching PdfViewerActivity: " + e.getMessage(), e);
        }
    }

    private void recordHistory(int songId) {
        executorService.execute(() -> {
            try {
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                History history = new History(songId, timestamp);
                db.historyDao().insertHistory(history);
                Log.d(TAG, "Recorded history for song ID: " + songId + " at " + timestamp);
            } catch (Exception e) {
                Log.e(TAG, "Error recording history: " + e.getMessage(), e);
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null && db.isOpen()) {
            db.close();
        }
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}