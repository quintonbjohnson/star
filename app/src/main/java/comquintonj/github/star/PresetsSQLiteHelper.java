package comquintonj.github.star;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * A SQLite database helper to store custom presets
 */
public class PresetsSQLiteHelper extends SQLiteOpenHelper {

    /**
     * Version of the databse
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Name of the database
     */
    private static final String DATABASE_NAME = "presetsManager";

    /**
     * A table to store the custom presets
     */
    private static final String TABLE_PRESETS = "presets";

    /**
     * Column name
     */
    private static final String KEY_PRESET = "preset";

    /**
     * String to create the table
     */
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_PRESETS + " ("
                    + KEY_PRESET + " TEXT)";

    /**
     * Constructor to create a database
     * @param context the context of the application
     */
    public PresetsSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Create the presets table
     * @param db the database
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    /**
     * Upgrade the database.
     * @param db the database to upgrade
     * @param oldVersion the old version of the database
     * @param newVersion the new version of the database
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRESETS);
        onCreate(db);
    }

    /**
     * Add a new custom preset
     * @param preset the preset to add
     */
    void addPreset(String preset) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PRESET, preset);

        // Insert a new row
        db.insert(TABLE_PRESETS, null, values);
        db.close();
    }

    /**
     * Get all the presets in the database
     * @return a list of presets
     */
    public ArrayList<String> getPresets() {
        ArrayList<String> presetList = new ArrayList<String>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_PRESETS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                // Adding a preset to list
                presetList.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }

        return presetList;
    }
}