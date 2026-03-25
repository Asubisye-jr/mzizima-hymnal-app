package com.example.mzizimahymnal;

import androidx.room.Embedded;
import androidx.room.Relation;
import java.util.List;

public class SongWithAudios {

    @Embedded
    public Song song;

    @Relation(
            parentColumn = "songId",
            entityColumn = "songOwnerId"
    )
    public List<Audio> audios;
}