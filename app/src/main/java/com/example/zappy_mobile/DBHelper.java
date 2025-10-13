package com.example.zappy_mobile;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "zappy.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_COMICS = "comics";
    public static final String COL_ID = "id";
    public static final String COL_TITLE = "title";
    public static final String COL_AUTHOR = "author";
    public static final String COL_PATH = "path";
    public static final String COL_CREATED = "created_at";

    public DBHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String create = "CREATE TABLE " + TABLE_COMICS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_TITLE + " TEXT," +
                COL_AUTHOR + " TEXT," +
                COL_PATH + " TEXT," +
                COL_CREATED + " TEXT" +
                ")";
        db.execSQL(create);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMICS);
        onCreate(db);
    }

    public long insertComic(String title, String author, String path, String createdAt) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TITLE, title);
        cv.put(COL_AUTHOR, author);
        cv.put(COL_PATH, path);
        cv.put(COL_CREATED, createdAt);
        return db.insert(TABLE_COMICS, null, cv);
    }

    public List<Comic> getAllComics() {
        List<Comic> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_COMICS, null, null, null, null, null, COL_ID + " DESC");
        if (c != null) {
            while (c.moveToNext()) {
                long id = c.getLong(c.getColumnIndexOrThrow(COL_ID));
                String title = c.getString(c.getColumnIndexOrThrow(COL_TITLE));
                String author = c.getString(c.getColumnIndexOrThrow(COL_AUTHOR));
                String path = c.getString(c.getColumnIndexOrThrow(COL_PATH));
                String created = c.getString(c.getColumnIndexOrThrow(COL_CREATED));
                list.add(new Comic(id, title, author, path, created));
            }
            c.close();
        }
        return list;
    }
}
