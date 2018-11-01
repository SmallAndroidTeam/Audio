package com.of.music.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DownloadMusicOpenHelper extends SQLiteOpenHelper {
    public static final String CREATE_DOWNLOADDATA = "create table if not exists downloadmusicData(name varchar(30),artist varchar(30),image varchar(30),uri vachar(30),Lrc_uri vachar(30),data varchar(40))";
    public final static String dbName = "DownloadMusic";
    private Context mContext;
    
    public DownloadMusicOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory cursorFactory, int version) {
        super(context, dbName, cursorFactory, version);
        mContext = context;
    }
    
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DOWNLOADDATA);
    }
    
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        String newtable = "create table if not exists newmusicData(name varchar(30))";
//        db.execSQL(newtable);
//        String droptable="drop table musicData";
//        db.execSQL(droptable);
//        String rename="ALTER TABLE newmusicData RENAME TO musicData";
//        db.execSQL(rename);
//        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}
