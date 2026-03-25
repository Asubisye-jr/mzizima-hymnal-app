package com.example.mzizimahymnal;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "complaints")
public class Complaint {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String userEmail;
    public String complaintText;
    public String feedback;

    public Complaint(String userEmail, String complaintText, String feedback) {
        this.userEmail = userEmail;
        this.complaintText = complaintText;
        this.feedback = feedback;
    }
}