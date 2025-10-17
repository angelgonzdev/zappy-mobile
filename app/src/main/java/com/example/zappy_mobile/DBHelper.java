package com.example.zappy_mobile;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * DBHelper: Clase que gestiona la base de datos local SQLite de Zappy.
 * Permite almacenar y recuperar cómics en formato PDF subidos por los usuarios.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "zappy.db";
    private static final int DB_VERSION = 1;

    // Nombre de la tabla y columnas
    public static final String TABLE_COMICS = "comics";
    public static final String COL_ID = "id";
    public static final String COL_TITLE = "title";
    public static final String COL_AUTHOR = "author";
    public static final String COL_PATH = "path";
    public static final String COL_CREATED = "created_at";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_COMICS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TITLE + " TEXT NOT NULL, " +
                COL_AUTHOR + " TEXT, " +
                COL_PATH + " TEXT NOT NULL, " +
                COL_CREATED + " TEXT" +
                ")";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMICS);
        onCreate(db);
    }

    /**
     * Inserta un nuevo cómic en la base de datos.
     *
     * @param title     Título del cómic.
     * @param author    Autor (opcional).
     * @param path      Ruta local del archivo PDF.
     * @param createdAt Fecha de creación o subida.
     * @return ID del registro insertado o -1 si falla.
     */
    public long insertComic(String title, String author, String path, String createdAt) {
        SQLiteDatabase db = null;
        long result = -1;
        try {
            db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COL_TITLE, title);
            values.put(COL_AUTHOR, author);
            values.put(COL_PATH, path);
            values.put(COL_CREATED, createdAt);

            result = db.insert(TABLE_COMICS, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) db.close();
        }
        return result;
    }

    /**
     * Obtiene todos los cómics guardados en la base de datos.
     *
     * @return Lista de objetos Comic.
     */
    public List<Comic> getAllComics() {
        List<Comic> comics = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = getReadableDatabase();
            cursor = db.query(
                    TABLE_COMICS,
                    null,
                    null,
                    null,
                    null,
                    null,
                    COL_ID + " DESC"
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE));
                    String author = cursor.getString(cursor.getColumnIndexOrThrow(COL_AUTHOR));
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(COL_PATH));
                    String created = cursor.getString(cursor.getColumnIndexOrThrow(COL_CREATED));

                    comics.add(new Comic(id, title, author, path, created));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }

        return comics;
    }
}
