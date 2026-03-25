package com.example.mzizimahymnal;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface HistoryDao {
    @Insert
    void insertHistory(History history);

    @Query("SELECT * FROM history ORDER BY timestamp DESC")
    List<History> getAllHistory();

    @Query("SELECT s.* FROM songs s INNER JOIN history h ON s.songId = h.songId ORDER BY h.timestamp DESC")
    List<Song> getHistorySongs();
}