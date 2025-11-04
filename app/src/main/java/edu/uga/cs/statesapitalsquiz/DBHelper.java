package edu.uga.cs.statesapitalsquiz;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
 * DBHelper initializes the database if it doesn't exist and simply loads it
 * if it already does.
 */
public class DBHelper extends SQLiteOpenHelper {

    // initialize variables
    private static final String DB_NAME = "statesquiz.db";
    private static final int DB_VERSION = 1;

    private final Context context;
    private final String dbPath;

    /**
     * DBHelper constructs a new instance of DBHelper by specifying the database
     * name, version, & path within the application.
     * @param context
     */
    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context.getApplicationContext();
        this.dbPath = this.context.getDatabasePath(DB_NAME).getPath();
    }

    /**
     * Check if statesquiz.db exists.
     */
    public boolean checkDatabaseExists() {
        File dbFile = new File(dbPath);
        return dbFile.exists();
    }


    /**
     * Copy statesquiz.db into the app databases folder (once).
     */
    public void copyDatabaseFromAssets() {
        try {
            // Ensure parent folder exists
            File dbFile = new File(dbPath);
            File parent = dbFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            InputStream in = context.getAssets().open(DB_NAME);
            OutputStream out = new FileOutputStream(dbPath);
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) > 0) {
                out.write(buf, 0, n);
            }
            out.flush();
            out.close();
            in.close();
        } catch (IOException ignored) { }
    }

    /**
     * openDatabase opens the prebuilt DB for read/write.
     * @return SQLiteDatabase
     */
    public SQLiteDatabase openDatabase() {
        return SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    /**
     * onCreate logic is unnecessary since the database is prebuilt.
     * @param db The database.
     */
    @Override public void onCreate(SQLiteDatabase db) { /* prebuilt */ }

    /**
     * onUpgrade logic is unnecessary since the database is prebuilt.
     * @param db The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { /* prebuilt */ }
}
