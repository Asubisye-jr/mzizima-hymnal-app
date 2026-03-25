package com.example.mzizimahymnal;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "songs")
public class Song {
    @PrimaryKey(autoGenerate = true)
    public int songId;
    public String title;
    public String lyrics;
    public String sheetPath;
    public String audioPath;

    public Song(String title, String lyrics, String sheetPath, String audioPath) {
        this.title = title;
        this.lyrics = lyrics;
        this.sheetPath = sheetPath;
        this.audioPath = audioPath;
    }

    @Override
    public String toString() {
        return title; // Return the song title for display in the ListView
    }
}