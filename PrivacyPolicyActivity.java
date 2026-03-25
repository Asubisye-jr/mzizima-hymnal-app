package com.example.mzizimahymnal;

import android.os.Bundle;
import android.webkit.WebView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class PrivacyPolicyActivity extends AppCompatActivity {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        webView = findViewById(R.id.privacyWebView);
        if (webView == null) {
            Toast.makeText(this, "UI initialization failed", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String privacyPolicy = "<html><body>" +
                "<h3>Privacy Policy</h3>" +
                "<p>Last Updated: June 19, 2025</p>" +
                "<p>This Privacy Policy explains how MyApplication collects, uses, and protects your personal information.</p>" +
                "<h4>Information We Collect</h4>" +
                "<ul>" +
                "<li>User email addresses for account management.</li>" +
                "<li>Complaints and feedback submitted by users.</li>" +
                "</ul>" +
                "<h4>How We Use Your Information</h4>" +
                "<ul>" +
                "<li>To provide and improve our services.</li>" +
                "<li>To respond to user complaints and feedback.</li>" +
                "</ul>" +
                "<h4>Data Security</h4>" +
                "<p>We implement security measures to protect your data.</p>" +
                "<h2>Your Rights</h2>" +
                "<p>You may request access to or deletion of your data by contacting us.</p>" +
                "</body></html>";
        webView.loadDataWithBaseURL(null, privacyPolicy, "text/html", "utf-8", null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}