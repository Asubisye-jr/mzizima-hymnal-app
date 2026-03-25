package com.example.mzizimahymnal;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class ContactUsActivity extends AppCompatActivity {
    private EditText messageEditText;
    private Button sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);

        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);

        sendButton.setOnClickListener(v -> {
            String message = messageEditText.getText().toString();
            if (!message.isEmpty()) {
                // TODO: Send message to server or store in database
                Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
                FirebaseCrashlytics.getInstance().recordException(new IllegalArgumentException("Empty message"));
            }
        });
    }
}