package com.example.mzizimahymnal;

import android.content.Context;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BackupManager {

    public static void exportSongs(Context context, Uri uri, AppDatabase db) {

        try {

            OutputStream os = context.getContentResolver().openOutputStream(uri);
            ZipOutputStream zos = new ZipOutputStream(os);

            List<SongWithAudios> songs = db.songDao().getSongsWithAudios();

            JSONArray songsArray = new JSONArray();

            for (SongWithAudios swa : songs) {

                Song song = swa.song;

                JSONObject obj = new JSONObject();
                obj.put("title", song.title);
                obj.put("lyrics", song.lyrics);
                obj.put("sheet", song.sheetPath);

                JSONArray audioArray = new JSONArray();

                for (Audio audio : swa.audios) {

                    File audioFile = new File(audio.audioPath);

                    if (!audioFile.exists()) continue;

                    String zipPath = "audio/" + audioFile.getName();

                    addFileToZip(audioFile, zipPath, zos);

                    audioArray.put(zipPath);
                }

                if (song.sheetPath != null) {

                    File sheetFile = new File(song.sheetPath);

                    if (sheetFile.exists()) {

                        String sheetZip = "sheets/" + sheetFile.getName();

                        addFileToZip(sheetFile, sheetZip, zos);

                        obj.put("sheet", sheetZip);
                    }
                }

                obj.put("audios", audioArray);

                songsArray.put(obj);
            }

            JSONObject root = new JSONObject();
            root.put("songs", songsArray);

            ZipEntry entry = new ZipEntry("metadata.json");
            zos.putNextEntry(entry);

            zos.write(root.toString().getBytes());

            zos.closeEntry();
            zos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addFileToZip(File file, String zipPath, ZipOutputStream zos) throws Exception {

        FileInputStream fis = new FileInputStream(file);

        ZipEntry entry = new ZipEntry(zipPath);
        zos.putNextEntry(entry);

        byte[] buffer = new byte[4096];
        int len;

        while ((len = fis.read(buffer)) > 0) {
            zos.write(buffer, 0, len);
        }

        fis.close();
        zos.closeEntry();
    }
}