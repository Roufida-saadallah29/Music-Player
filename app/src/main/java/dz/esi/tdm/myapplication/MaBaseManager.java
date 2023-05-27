package dz.esi.tdm.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class MaBaseManager extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "favorisSongsDB";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "songs";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NOM = "nom";
    public MaBaseManager(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String sql = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_NOM + " varchar(200) NOT NULL UNIQUE)" ;
        sqLiteDatabase.execSQL(sql);
    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    boolean addFavoris(String nom) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NOM, nom);
        SQLiteDatabase db = getWritableDatabase();
        return db.insert(TABLE_NAME, null, contentValues) != -1;
    }
    Cursor getAllFav() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }
    int deleteFav(int id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(
                TABLE_NAME,
                COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }
}
