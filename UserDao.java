package com.example.mzizimahymnal;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT * FROM users WHERE status = 'pending'")
    List<User> getPendingUsers();

    @Query("SELECT * FROM users WHERE status = 'approved'")
    List<User> getApprovedUsers();

    @Query("SELECT * FROM users WHERE resetRequested = 1")
    List<User> getResetRequestedUsers();

    @Query("DELETE FROM users WHERE email = :email")
    void deleteUser(String email);

    @Query("UPDATE users SET role = 'ADMIN' WHERE email = :email")
    int makeAdmin(String email);

    @Query("UPDATE users SET status = 'approved' WHERE email = :email")
    int approveUser(String email);

    @Query("UPDATE users SET resetRequested = 1 WHERE email = :email")
    int requestPasswordReset(String email);

    @Query("UPDATE users SET resetRequested = 0 WHERE email = :email")
    int clearResetRequest(String email);

    @Query("SELECT * FROM users WHERE email = :email")
    User getUserByEmail(String email);

    @Update
    void updateUser(User user);

    @Insert
    void insertUser(User user);

    @Query("SELECT * FROM users WHERE email = :email AND password = :password")
    User validateUser(String email, String password);
}