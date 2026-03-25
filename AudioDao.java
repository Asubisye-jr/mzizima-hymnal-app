package com.example.mzizimahymnal;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AudioDao {

    @Insert
    void insertAudio(Audio audio);

    @Query("SELECT * FROM audios WHERE songOwnerId = :songId")
    List<Audio>getAudiosForSong(int songId);
}