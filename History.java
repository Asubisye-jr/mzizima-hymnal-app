package com.example.mzizimahymnal;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "history")
public class History {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int songId;
    public String timestamp;

    public History(int songId, String timestamp) {
        this.songId = songId;
        this.timestamp = timestamp;
    }
}