package com.example.mzizimahymnal;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {User.class,Audio.class, Song.class, Complaint.class, History.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract SongDao songDao();
    public abstract ComplaintDao complaintDao();
    public abstract HistoryDao historyDao();
    public abstract AudioDao audioDao();


    private static AppDatabase INSTANCE;

    public static synchronized AppDatabase getInstance(android.content.Context context) {
        if (INSTANCE == null) {
            INSTANCE = androidx.room.Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "app-database"
                    )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return INSTANCE;
    }
    public static AppDatabase createDatabase(android.content.Context context) {
        return androidx.room.Room.databaseBuilder(context, AppDatabase.class, "app-database")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .fallbackToDestructiveMigration()
                .build();
    }

    // Migration from version 1 to 2 (adding resetRequested column)
    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE users ADD COLUMN resetRequested INTEGER NOT NULL DEFAULT 0");
        }
    };

    // Migration from version 2 to 3 (adding Complaint table with correct constraints)
    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS complaints (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "userEmail TEXT NOT NULL," +
                    "complaintText TEXT NOT NULL," +
                    "feedback TEXT)");
        }
    };

    // Migration from version 3 to 4 (adding History table)
    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS history (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "songId INTEGER NOT NULL," +
                    "timestamp TEXT NOT NULL)");
        }
    };
}