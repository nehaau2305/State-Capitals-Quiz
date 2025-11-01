package edu.uga.cs.statesapitalsquiz;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "statesquiz.db";
    private static final int DB_VERSION = 1;

    private final Context context;
    private final String dbPath;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context.getApplicationContext();
        this.dbPath = this.context.getDatabasePath(DB_NAME).getPath();
    }

    /** Check if statesquiz.db exists. */
    public boolean checkDatabaseExists() {
        File dbFile = new File(dbPath);
        return dbFile.exists();
    }

    /** Copy tatesquiz.db into the app databases folder (once). */
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

    /** Open the prebuilt DB for read/write. */
    public SQLiteDatabase openDatabase() {
        return SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    @Override public void onCreate(SQLiteDatabase db) { /* prebuilt */ }
    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { /* prebuilt */ }
}
