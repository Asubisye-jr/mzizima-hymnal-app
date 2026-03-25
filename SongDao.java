package com.example.mzizimahymnal;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface SongDao {

    @Query("SELECT * FROM songs")
    List<Song> getAllSongs();

    @Query("SELECT * FROM songs WHERE songId = :songId")
    Song getSongById(int songId);

    @Query("SELECT * FROM songs WHERE title = :title LIMIT 1")
    Song getSongByTitle(String title);
    @Query("SELECT * FROM songs WHERE title = :title LIMIT 1")
    Song findByTitle(String title);

    @Insert
    long insertSong(Song song);

    @Query("DELETE FROM songs WHERE songId = :songId")
    void deleteSong(int songId);

    // 🔹 Load songs with their audios
    @Transaction
    @Query("SELECT * FROM songs")
    List<SongWithAudios> getSongsWithAudios();
}