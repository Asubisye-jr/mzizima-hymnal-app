package com.example.mzizimahymnal;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "audios",
        foreignKeys = @ForeignKey(
                entity = Song.class,
                parentColumns = "songId",
                childColumns = "songOwnerId",
                onDelete = CASCADE
        ))
public class Audio {

    @PrimaryKey(autoGenerate = true)
    public int audioId;

    public int songOwnerId;
    public String displayName;
    public String audioPath;

    public Audio(int songOwnerId, String displayName, String audioPath) {
        this.songOwnerId = songOwnerId;
        this.displayName = displayName;
        this.audioPath = audioPath;
    }
}