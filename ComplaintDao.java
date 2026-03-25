package com.example.mzizimahymnal;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ComplaintDao {
    @Insert
    void insertComplaint(Complaint complaint);

    @Query("SELECT * FROM complaints")
    List<Complaint> getAllComplaints();

    @Query("UPDATE complaints SET feedback = :feedback WHERE id = :complaintId")
    void updateFeedback(int complaintId, String feedback);

    @Query("SELECT * FROM complaints WHERE userEmail = :userEmail")
    List<Complaint> getComplaintsByUserEmail(String userEmail);
}