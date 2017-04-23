package comquintonj.github.star;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A SQLite database helper to store messages
 */
public class MessagesSQLiteHelper extends SQLiteOpenHelper {

    /**
     * Version of the database
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Name of the database
     */
    private static final String DATABASE_NAME = "messagesManager";

    /**
     * A table to store the messages
     */
    private static final String TABLE_MESSAGES = "messages";

    /**
     * Column name for sender of message
     */
    private static final String KEY_SENDER = "sender";

    /**
     * Column name for text of message
     */
    private static final String KEY_MESSAGE = "message";

    /**
     * Column name for text of message
     */
    private static final String KEY_TO = "toWho";

    /**
     * String to create the table
     */
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_MESSAGES + " ("
                    + KEY_SENDER + " TEXT, "
                    + KEY_MESSAGE + " TEXT, "
                    + KEY_TO + " TEXT)";

    /**
     * Constructor to create a database
     * @param context the context of the application
     */
    public MessagesSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Create the messages table
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
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        onCreate(db);
    }

    /**
     * Add a new message to the table
     * @param sender the sender of the mesage
     * @param message the text of the message
     */
    void addMessage(String sender, String message, String to) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_SENDER, sender);
        values.put(KEY_MESSAGE, message);
        values.put(KEY_TO, to);

        // Insert a new row
        db.insert(TABLE_MESSAGES, null, values);
        db.close();
    }

    /**
     * Get all the senders in the database
     * @return a list of senders
     */
    public ArrayList<String> getSenders() {
        ArrayList<String> senderList = new ArrayList<String>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_MESSAGES;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                String sender = cursor.getString(0);
                if (!(senderList.contains(sender)) && !(sender.equals("User"))) {
                    senderList.add(sender);
                }
            } while (cursor.moveToNext());
        }

        // Reverse to put newest first
        Collections.reverse(senderList);

        return senderList;
    }

    /**
     * Get the previous messages from the specific sender
     * @param dbHelp the database to use
     * @param sender the sender of the conversation
     * @return the list of past messages sent
     */
    ArrayList<Message> getPastMessages(MessagesSQLiteHelper dbHelp, String sender) {
        ArrayList<Message> listOfMessages = new ArrayList<>();
        SQLiteDatabase database = dbHelp.getReadableDatabase();

        String whereClause = KEY_SENDER + "=?" + " OR "
                + KEY_SENDER + "=?";

        String[] whereArgs = new String[]{sender, "User"};

        //Cursor for SQL Database
        Cursor cursor = database.query(TABLE_MESSAGES,
                new String[] {KEY_SENDER, KEY_MESSAGE, KEY_TO},
                whereClause,
                whereArgs,
                null, null, null, null);

        try {
            while (cursor.moveToNext()) {
                String foundSender =
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_SENDER));
                String message =
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_MESSAGE));
                String to = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TO));

                if (to.equals(sender)) {
                    listOfMessages.add(new Message(foundSender, message, to));
                } else if (foundSender.equals(sender)) {
                    listOfMessages.add(new Message(foundSender, message, to));
                }
            }
        } finally {
            cursor.close();
        }

        return listOfMessages;
    }

    /**
     * Clear past messages from the specific sender
     * @param dbHelp the database to use
     * @param sender the sender of the conversation
     */
    void clearPastMessages(MessagesSQLiteHelper dbHelp, String sender) {
        ArrayList<Message> listOfMessages = new ArrayList<>();
        SQLiteDatabase database = dbHelp.getReadableDatabase();

        String whereClause = KEY_SENDER + "=?" + " OR "
                + KEY_SENDER + "=?";

        String[] whereArgs = new String[]{sender, "User"};

        //Cursor for SQL Database
        Cursor cursor = database.query(TABLE_MESSAGES,
                new String[] {KEY_SENDER, KEY_MESSAGE, KEY_TO},
                whereClause,
                whereArgs,
                null, null, null, null);

        try {
            while (cursor.moveToNext()) {
                String foundSender = cursor.getString(cursor.getColumnIndexOrThrow(KEY_SENDER));
                String message = cursor.getString(cursor.getColumnIndexOrThrow(KEY_MESSAGE));
                String to = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TO));
                if (foundSender.equals(sender)
                        || foundSender.equals("User") && to.equals(sender)) {
                    String rowId = cursor.getString(cursor.getColumnIndex(KEY_SENDER));
                    if (rowId.equals("User")) {
                        database.delete(TABLE_MESSAGES, KEY_SENDER + "=?" + " AND " + KEY_TO + "=?",
                                new String[]{rowId, sender});
                    } else{
                        database.delete(TABLE_MESSAGES, KEY_SENDER + "=?",
                                new String[]{sender});
                    }
                }
            }
        } finally {
            cursor.close();
        }

        // Add a new message to the database to keep the conversation
        dbHelp.addMessage(sender, "First", "First");
    }

    /**
     * Clear all past messages from the specific sender and delete history of the conversation
     * @param dbHelp the database to use
     * @param sender the sender of the conversation
     */
    void clearAllPastMessages(MessagesSQLiteHelper dbHelp, String sender) {
        ArrayList<Message> listOfMessages = new ArrayList<>();
        SQLiteDatabase database = dbHelp.getReadableDatabase();

        String whereClause = KEY_SENDER + "=?" + " OR "
                + KEY_SENDER + "=?";

        String[] whereArgs = new String[]{sender, "User"};

        //Cursor for SQL Database
        Cursor cursor = database.query(TABLE_MESSAGES,
                new String[] {KEY_SENDER, KEY_MESSAGE, KEY_TO},
                whereClause,
                whereArgs,
                null, null, null, null);

        try {
            while (cursor.moveToNext()) {
                String foundSender = cursor.getString(cursor.getColumnIndexOrThrow(KEY_SENDER));
                String to = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TO));

                if (to.equals(sender) || foundSender.equals(sender)) {
                    String rowId = cursor.getString(cursor.getColumnIndex(KEY_SENDER));
                    database.delete(TABLE_MESSAGES, KEY_SENDER + "=?",  new String[]{rowId});
                }
            }
        } finally {
            cursor.close();
        }
    }
}