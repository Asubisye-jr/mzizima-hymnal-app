package com.example.mzizimahymnal;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.room.Room;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.List;

public class DashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "DashboardActivity";
    private ListView songsListView;
    private DrawerLayout drawer;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_dashboard);
        } catch (Exception e) {
            Log.e(TAG, "Failed to set content view: " + e.getMessage(), e);
            FirebaseCrashlytics.getInstance().recordException(new RuntimeException("Failed to set content view: " + e.getMessage(), e));
            Toast.makeText(this, "Error loading UI", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "app-database").build();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        TextView welcomeTextView = findViewById(R.id.welcomeTextView);
        songsListView = findViewById(R.id.songsListView);

        // Set a generic welcome message
        welcomeTextView.setText("Welcome to the Hymnal App!");

        updateNavigationMenu();
        loadSongs();
    }

    private void loadSongs() {

        new AsyncTask<Void, Void, List<SongWithAudios>>() {

            @Override
            protected List<SongWithAudios> doInBackground(Void... voids) {
                try {
                    return db.songDao().getSongsWithAudios();
                } catch (Exception e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                    return new java.util.ArrayList<>();
                }
            }

            @Override
            protected void onPostExecute(List<SongWithAudios> songs) {

                List<String> displayList = new java.util.ArrayList<>();

                for (SongWithAudios s : songs) {
                    int audioCount = (s.audios != null) ? s.audios.size() : 0;
                    displayList.add(s.song.title + " (" + audioCount + " audios)");
                }

                ArrayAdapter<String> adapter =
                        new ArrayAdapter<>(DashboardActivity.this,
                                android.R.layout.simple_list_item_1,
                                displayList);

                songsListView.setAdapter(adapter);

                songsListView.setOnItemClickListener((parent, view, position, id) -> {
                    SongWithAudios selected = songs.get(position);
                    Intent intent = new Intent(DashboardActivity.this,
                            LyricsViewActivity.class);
                    intent.putExtra("SONG_ID", selected.song.songId);
                    startActivity(intent);
                });
            }

        }.execute();
    }
    private void updateNavigationMenu() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.getMenu().clear();
        // Always inflate the standard user menu
        navigationView.inflateMenu(R.menu.user_menu);
        Log.d(TAG, "Updated navigation menu for standard user.");
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent = null;
        int id = item.getItemId();
        Log.d(TAG, "Navigation item selected: " + item.getTitle());

        // Keep navigation for features that don't require login
        if (id == R.id.nav_upload_songs) {
            intent = new Intent(this, UploadSongsActivity.class);
        } else if (id == R.id.nav_delete_songs) {
            intent = new Intent(this, DeleteSongsActivity.class);
        } else if (id == R.id.nav_contact_us) {
            intent = new Intent(this, ContactUsActivity.class);
        } else if (id == R.id.nav_privacy_policy){
            intent = new Intent(this, PrivacyPolicyActivity.class);
        } else if (id == R.id.nav_history) {
            intent = new Intent(this, HistoryActivity.class);
        }else if (id == R.id.nav_export_import) {
            intent = new Intent(this, ExportImportActivity.class);
        }else {
            // Removed admin and account-specific items
            Log.w(TAG, "Action not implemented or removed: " + item.getTitle());
        }

        if (intent != null) {
            try {
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Failed to start activity: " + e.getMessage(), e);
                FirebaseCrashlytics.getInstance().recordException(e);
                Toast.makeText(this, "Failed to open " + item.getTitle(), Toast.LENGTH_SHORT).show();
            }
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null && db.isOpen()) {
            db.close();
            Log.d(TAG, "Database closed");
        }
    }
}
