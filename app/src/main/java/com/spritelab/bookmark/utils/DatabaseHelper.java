package com.spritelab.bookmark.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.spritelab.bookmark.model.BookmarkModel;
import com.spritelab.bookmark.model.TaskModel;
import com.spritelab.bookmark.model.TaskPointModel;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "bookmark.db";
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_CONFIG = "config";
    private static final String TABLE_BOOKMARKS = "bookmarks";
    private static final String TABLE_TASKS = "tasks";

    private static final String CONF_ID = "id";
    private static final String CONF_THEME = "theme";

    private static final String BOOK_ID = "id";
    private static final String BOOK_TITLE = "title";
    private static final String BOOK_DATE = "date";

    private static final String TASK_ID = "id";
    private static final String TASK_TITLE = "title";
    private static final String TASK_DATE = "date";
    private static final String TASK_POINTS = "points";
    private static final String TASK_STATUS = "status";

    private static DatabaseHelper instance;
    private final Gson gson = new Gson();

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
        db.execSQL("CREATE TABLE " + TABLE_CONFIG + "(" + CONF_ID + " INTEGER PRIMARY KEY, " + CONF_THEME + " INTEGER DEFAULT 2)");
        db.execSQL("CREATE TABLE " + TABLE_BOOKMARKS + "(" + BOOK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + BOOK_TITLE + " TEXT, " + BOOK_DATE + " TEXT)");
        db.execSQL("CREATE TABLE " + TABLE_TASKS + "(" + TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + TASK_TITLE + " TEXT, " + TASK_DATE + " TEXT, " + TASK_POINTS + " TEXT, " + TASK_STATUS + " INTEGER DEFAULT 0)");
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
        int theme = 2;
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
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_BOOKMARKS + " ORDER BY " + BOOK_ID + " ASC", null);
        if (cursor.moveToFirst()) {
            do {
                list.add(new BookmarkModel(cursor.getInt(0), cursor.getString(1), cursor.getString(2)));
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
                long newId = db.insert(TABLE_BOOKMARKS, null, v);
                b.setId((int) newId);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public long addTask(TaskModel task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TASK_TITLE, task.getTitle());
        values.put(TASK_DATE, task.getDate());
        values.put(TASK_POINTS, gson.toJson(task.getPoints()));
        values.put(TASK_STATUS, 0);
        return db.insert(TABLE_TASKS, null, values);
    }

    public List<TaskModel> getAllTasks() {
        List<TaskModel> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TASKS + " ORDER BY " + TASK_ID + " ASC", null);
        if (cursor.moveToFirst()) {
            Type type = new TypeToken<List<TaskPointModel>>(){}.getType();
            do {
                String pointsJson = cursor.getString(3);
                List<TaskPointModel> points = gson.fromJson(pointsJson, type);
                if (points == null) points = new ArrayList<>();
                list.add(new TaskModel(cursor.getInt(0), cursor.getString(1), cursor.getString(2), points));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public void updateTask(TaskModel task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TASK_TITLE, task.getTitle());
        values.put(TASK_DATE, task.getDate());
        values.put(TASK_POINTS, gson.toJson(task.getPoints()));
        db.update(TABLE_TASKS, values, TASK_ID + " = ?", new String[]{String.valueOf(task.getId())});
    }

    public void deleteTask(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS, TASK_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public void updateTasksOrder(List<TaskModel> list) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_TASKS, null, null);
            for (TaskModel t : list) {
                ContentValues v = new ContentValues();
                v.put(TASK_TITLE, t.getTitle());
                v.put(TASK_DATE, t.getDate());
                v.put(TASK_POINTS, gson.toJson(t.getPoints()));
                long newId = db.insert(TABLE_TASKS, null, v);
                t.setId((int) newId);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}
