package com.example.mzizimahymnal;

public class Utils {
    public static String getUsernameFromEmail(String email) {
        if (email == null || email.isEmpty()) {
            return "Unknown";
        }
        int atIndex = email.indexOf('@');
        return atIndex > 0 ? email.substring(0, atIndex) : email;
    }
}