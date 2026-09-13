package com.spritelab.bookmark.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.spritelab.bookmark.model.BookmarkModel;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "bookmark.db";
    private static final int DATABASE_VERSION = 1;

    // Tables
    private static final String TABLE_CONFIG = "config";
    private static final String TABLE_BOOKMARKS = "bookmarks";
    private static final String TABLE_TASKS = "tasks";

    // Config
    private static final String CONF_ID = "id";
    private static final String CONF_THEME = "theme";

    // Notes
    private static final String BOOK_ID = "id";
    private static final String BOOK_TITLE = "title";
    private static final String BOOK_DATE = "date";

    // Tasks
    private static final String TASK_ID = "id";
    private static final String TASK_TITLE = "title";
    private static final String TASK_STATUS = "status";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createConfig = "CREATE TABLE " + TABLE_CONFIG + "(" +
                CONF_ID + " INTEGER PRIMARY KEY, " +
                CONF_THEME + " INTEGER DEFAULT 2)";
        
        String createBookmarks = "CREATE TABLE " + TABLE_BOOKMARKS + "(" +
                BOOK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                BOOK_TITLE + " TEXT, " +
                BOOK_DATE + " TEXT)";

        String createTasks = "CREATE TABLE " + TABLE_TASKS + "(" +
                TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TASK_TITLE + " TEXT, " +
                TASK_STATUS + " INTEGER DEFAULT 0)";

        db.execSQL(createConfig);
        db.execSQL(createBookmarks);
        db.execSQL(createTasks);

        db.execSQL("INSERT INTO " + TABLE_CONFIG + " (" + CONF_ID + ", " + CONF_THEME + ") VALUES (1, 2)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONFIG);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKMARKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        onCreate(db);
    }

    public void setTheme(int theme) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CONF_THEME, theme);
        db.update(TABLE_CONFIG, values, CONF_ID + " = 1", null);
    }

    public int getTheme() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CONFIG, new String[]{CONF_THEME}, CONF_ID + " = 1", null, null, null, null);
        int theme = 2; // Default system
        if (cursor != null && cursor.moveToFirst()) {
            theme = cursor.getInt(0);
            cursor.close();
        }
        return theme;
    }

    public long addBookmark(BookmarkModel bookmark) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BOOK_TITLE, bookmark.getTitle());
        values.put(BOOK_DATE, bookmark.getDate());
        return db.insert(TABLE_BOOKMARKS, null, values);
    }

    public List<BookmarkModel> getAllBookmarks() {
        List<BookmarkModel> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_BOOKMARKS + " ORDER BY " + BOOK_ID + " DESC", null);

        if (cursor.moveToFirst()) {
            do {
                BookmarkModel b = new BookmarkModel(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2)
                );
                list.add(b);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public void deleteBookmark(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_BOOKMARKS, BOOK_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public void updateBookmarksOrder(List<BookmarkModel> list) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_BOOKMARKS, null, null);
            for (BookmarkModel b : list) {
                ContentValues v = new ContentValues();
                v.put(BOOK_TITLE, b.getTitle());
                v.put(BOOK_DATE, b.getDate());
                db.insert(TABLE_BOOKMARKS, null, v);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}
