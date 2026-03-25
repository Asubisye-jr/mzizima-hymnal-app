package com.example.mzizimahymnal;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ExportImportActivity extends AppCompatActivity {

    private AppDatabase db;
    private RecyclerView recyclerView;
    private SongExportAdapter adapter;
    private List<SongWithAudios> songs = new ArrayList<>();
    private List<SongWithAudios> selectedSongs = new ArrayList<>();

    private CheckBox selectAllCheckBox;
    private Button btnExport, btnImport;

    private ActivityResultLauncher<Intent> exportLauncher;
    private ActivityResultLauncher<Intent> importLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_import);

        db = AppDatabase.getInstance(this);

        recyclerView = findViewById(R.id.recyclerSongs);
        selectAllCheckBox = findViewById(R.id.selectAllCheckBox);
        btnExport = findViewById(R.id.btnExport);
        btnImport = findViewById(R.id.btnImport);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load songs in background
        new Thread(() -> {
            songs = db.songDao().getSongsWithAudios();
            runOnUiThread(() -> setupAdapter());
        }).start();

        selectAllCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            selectedSongs.clear();
            if (isChecked) selectedSongs.addAll(songs);
            adapter.notifyDataSetChanged();
        });

        // Export launcher
        exportLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) new Thread(() -> exportSelected(uri)).start();
                    }
                });

        btnExport.setOnClickListener(v -> {
            if (selectedSongs.isEmpty()) {
                Toast.makeText(this, "No songs selected", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.setType("application/zip");
            intent.putExtra(Intent.EXTRA_TITLE, "songs_export.zip");
            exportLauncher.launch(intent);
        });

        // Import launcher
        importLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) new Thread(() -> importZip(uri)).start();
                    }
                });

        btnImport.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/zip");
            importLauncher.launch(intent);
        });
    }

    private void setupAdapter() {
        adapter = new SongExportAdapter(songs, new SongExportAdapter.OnItemCheckListener() {
            @Override
            public void onItemCheck(SongWithAudios song) {
                selectedSongs.add(song);
            }

            @Override
            public void onItemUncheck(SongWithAudios song) {
                selectedSongs.remove(song);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void exportSelected(Uri uri) {
        try {
            File tempZip = new File(getCacheDir(), "songs_export.zip");
            ZipFile zipFile = new ZipFile(tempZip);

            for (SongWithAudios swa : selectedSongs) {
                File pdf = new File(swa.song.sheetPath);
                if (pdf.exists()) zipFile.addFile(pdf, new ZipParameters());

                if (swa.audios != null) {
                    for (Audio audio : swa.audios) {
                        File audioFile = new File(audio.audioPath);
                        if (audioFile.exists()) zipFile.addFile(audioFile, new ZipParameters());
                    }
                }
            }

            try (InputStream in = new FileInputStream(tempZip);
                 OutputStream out = getContentResolver().openOutputStream(uri)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) != -1) out.write(buf, 0, len);
            }

            runOnUiThread(() -> Toast.makeText(this, "Export successful", Toast.LENGTH_LONG).show());

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, "Export failed", Toast.LENGTH_SHORT).show());
        }
    }

    private void importZip(Uri uri) {
        try {
            File tempFile = new File(getCacheDir(), "temp_import.zip");
            try (InputStream in = getContentResolver().openInputStream(uri);
                 OutputStream out = new FileOutputStream(tempFile)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) != -1) out.write(buf, 0, len);
            }

            File tempDir = new File(getCacheDir(), "import");
            if (!tempDir.exists()) tempDir.mkdirs();

            ZipFile zipFile = new ZipFile(tempFile);
            zipFile.extractAll(tempDir.getAbsolutePath());

            File[] files = tempDir.listFiles();
            if (files == null) return;

            for (File file : files) {
                if (file.getName().endsWith(".pdf")) {
                    String title = file.getName().replace(".pdf", "");
                    Song existing = db.songDao().getSongByTitle(title);
                    if (existing != null) {
                        db.songDao().deleteSong(existing.songId);
                    }
                    db.songDao().insertSong(new Song(title, "", file.getAbsolutePath(), null));
                }

                if (file.getName().endsWith(".mp3")) {
                    String title = file.getName().split("_")[0];
                    Song song = db.songDao().getSongByTitle(title);
                    if (song != null) {
                        db.audioDao().insertAudio(new Audio(song.songId, file.getName(), file.getAbsolutePath()));
                    }
                }
            }

            // Reload songs in UI
            songs = db.songDao().getSongsWithAudios();
            runOnUiThread(() -> {
                selectedSongs.clear();
                setupAdapter();
                Toast.makeText(this, "Import completed", Toast.LENGTH_LONG).show();
            });

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, "Import failed", Toast.LENGTH_SHORT).show());
        }
    }
}