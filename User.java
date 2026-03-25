package com.example.mzizimahymnal;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "users", indices = {@Index(value = {"email"}, unique = true)})
public class User {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String email;
    public String password;
    public String role;
    public String status;
    public boolean resetRequested; // New field

    public User(String email, String password, String role, String status, boolean resetRequested) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.status = status;
        this.resetRequested = resetRequested;
    }

    @Override
    public String toString() {
        return email;
    }
}