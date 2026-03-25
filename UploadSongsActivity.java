package com.example.mzizimahymnal;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class UploadSongsActivity extends AppCompatActivity {

    private static final int PICK_PDF_REQUEST = 1;
    private static final int PICK_AUDIO_REQUEST = 2;

    private AppDatabase db;

    private EditText titleEditText, lyricsEditText;
    private Button selectPdfButton, uploadButton, addAudioButton;
    private LinearLayout audioContainer;

    private Uri pdfUri;

    private List<Uri> audioUris = new ArrayList<>();
    private List<String> audioNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_songs);

        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "app-database").build();

        titleEditText = findViewById(R.id.titleEditText);
        lyricsEditText = findViewById(R.id.lyricsEditText);
        selectPdfButton = findViewById(R.id.selectPdfButton);
        uploadButton = findViewById(R.id.uploadButton);
        addAudioButton = findViewById(R.id.addAudioButton);
        audioContainer = findViewById(R.id.audioContainer);

        selectPdfButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf");
            startActivityForResult(intent, PICK_PDF_REQUEST);
        });

        addAudioButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("audio/*");
            startActivityForResult(intent, PICK_AUDIO_REQUEST);
        });

        uploadButton.setOnClickListener(v -> {
            String title = titleEditText.getText().toString();
            String lyrics = lyricsEditText.getText().toString();

            if (!title.isEmpty() && !lyrics.isEmpty() && pdfUri != null) {
                new UploadSongTask().execute(title, lyrics);
            } else {
                Toast.makeText(this,
                        "Fill all fields & select PDF",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_PDF_REQUEST && resultCode == RESULT_OK && data != null) {
            pdfUri = data.getData();
            selectPdfButton.setText("PDF Selected");
        }

        if (requestCode == PICK_AUDIO_REQUEST && resultCode == RESULT_OK && data != null) {

            Uri audioUri = data.getData();

            EditText input = new EditText(this);
            input.setHint("Audio Display Name");

            new AlertDialog.Builder(this)
                    .setTitle("Enter Audio Name")
                    .setView(input)
                    .setPositiveButton("Add", (dialog, which) -> {

                        String name = input.getText().toString();

                        audioUris.add(audioUri);
                        audioNames.add(name);

                        TextView label = new TextView(this);
                        label.setText("🎵 " + name);
                        label.setPadding(8, 8, 8, 8);

                        audioContainer.addView(label);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }

    private class UploadSongTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {

            String title = params[0];
            String lyrics = params[1];

            try {
                File pdfFile = new File(
                        getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                        title + ".pdf"
                );

                try (InputStream input = getContentResolver().openInputStream(pdfUri);
                     FileOutputStream output = new FileOutputStream(pdfFile)) {

                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = input.read(buffer)) != -1) {
                        output.write(buffer, 0, bytesRead);
                    }
                }

                Song song = new Song(title, lyrics, pdfFile.getAbsolutePath(), "");
                long songId = db.songDao().insertSong(song);

                for (int i = 0; i < audioUris.size(); i++) {

                    Uri uri = audioUris.get(i);
                    String name = audioNames.get(i);

                    File audioFile = new File(
                            getExternalFilesDir(Environment.DIRECTORY_MUSIC),
                            title + "_" + name + ".mp3"
                    );

                    try (InputStream input =
                                 getContentResolver().openInputStream(uri);
                         FileOutputStream output =
                                 new FileOutputStream(audioFile)) {

                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = input.read(buffer)) != -1) {
                            output.write(buffer, 0, bytesRead);
                        }
                    }

                    Audio audio = new Audio((int) songId,
                            name,
                            audioFile.getAbsolutePath());

                    db.audioDao().insertAudio(audio);
                }

                return true;

            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(UploadSongsActivity.this,
                        "Song Uploaded Successfully",
                        Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(UploadSongsActivity.this,
                        "Upload Failed",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }
}