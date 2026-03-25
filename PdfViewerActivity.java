package com.example.mzizimahymnal;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Handler;
import android.os.Looper;

public class PdfViewerActivity extends AppCompatActivity {
    private static final String TAG = "PdfViewerActivity";
    private PDFView pdfView;
    private TextView songTitleTextView;
    private AppDatabase db;
    private ExecutorService executorService;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_pdfviewer);
        } catch (Exception e) {
            String errorMsg = "Failed to set content view: " + e.getMessage();
            Log.e(TAG, errorMsg, e);
            FirebaseCrashlytics.getInstance().recordException(new RuntimeException(errorMsg, e));
            Toast.makeText(this, "Error loading UI: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        db = AppDatabase.createDatabase(getApplicationContext());
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        pdfView = findViewById(R.id.pdfView);
        songTitleTextView = findViewById(R.id.songTitleTextView);
        if (pdfView == null) {
            Log.e(TAG, "PDFView not found in layout");
            Toast.makeText(this, "UI component missing", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Uri pdfUri = getIntent().getData();
        int songId = getIntent().getIntExtra("SONG_ID", -1);
        if (pdfUri == null) {
            Log.e(TAG, "No PDF URI provided");
            Toast.makeText(this, "No PDF file specified", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (songId != -1) {
            fetchSongTitle(songId);
        } else {
            Log.w(TAG, "No song ID provided, skipping title");
            if (songTitleTextView != null) {
                songTitleTextView.setText("Sheet Music");
            }
        }

        try {
            pdfView.fromUri(pdfUri)
                    .enableSwipe(true)
                    .enableDoubletap(true)
                    .defaultPage(0)
                    .onError(t -> {
                        String errorMsg = "Failed to load PDF: " + t.getMessage();
                        Log.e(TAG, errorMsg, t);
                        FirebaseCrashlytics.getInstance().recordException(new RuntimeException(errorMsg, t));
                        Toast.makeText(this, "Error loading PDF: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .load();
            Log.d(TAG, "Loading PDF from URI: " + pdfUri);
        } catch (Exception e) {
            String errorMsg = "Error accessing PDF: " + e.getMessage();
            Log.e(TAG, errorMsg, e);
            FirebaseCrashlytics.getInstance().recordException(new RuntimeException(errorMsg, e));
            Toast.makeText(this, "Error accessing PDF", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fetchSongTitle(int songId) {
        executorService.execute(() -> {
            try {
                Song song = db.songDao().getSongById(songId);
                mainHandler.post(() -> {
                    if (song != null && songTitleTextView != null) {
                        songTitleTextView.setText(song.title);
                    } else {
                        Log.w(TAG, "Song not found or title view missing");
                        if (songTitleTextView != null) {
                            songTitleTextView.setText("Sheet Music");
                        }
                    }
                });
            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                mainHandler.post(() -> {
                    if (songTitleTextView != null) {
                        songTitleTextView.setText("Sheet Music");
                    }
                });
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