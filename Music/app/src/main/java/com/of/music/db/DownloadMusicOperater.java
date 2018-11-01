package com.of.music.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.of.music.model.DownloadMusicInfo;

import java.util.ArrayList;
import java.util.List;

public class DownloadMusicOperater{
    private DownloadMusicOpenHelper dbHelper;
    private SQLiteDatabase db;
    
    private String TAG="downloadMusic";
    public DownloadMusicOperater(Context context) {
        dbHelper = new DownloadMusicOpenHelper(context, "downloadmusicData", null, 1);
        db = dbHelper.getWritableDatabase();
    }
    
    // 添加联系人
    public void add(DownloadMusicInfo lxr) {
        Log.i(TAG, "add: name"+lxr.getTitle());
        Log.i(TAG, "add:artist"+lxr.getArtist());
        Log.i(TAG, "add path: "+lxr.getCoverPath());
        Log.i(TAG, "add path: "+lxr.getMusicPath());
        Log.i(TAG, "add path: "+lxr.getLrcPath());
        Log.i(TAG,"add path:"+lxr.getTime());
        db.execSQL("insert into downloadmusicData(name,artist,image,uri,Lrc_uri,data) values(?,?,?,?,?,?)",
                new Object[] { lxr.getTitle(),lxr.getArtist(),lxr.getCoverPath(),lxr.getMusicPath(),lxr.getLrcPath(),lxr.getTime()});
        
    }
    //更新时间
    public void alter(String title,String data){
        
        db.execSQL("Update downloadmusicData set data=? where name=?",new String[]{data,title});
    }
    // 删除联系人
    public void delete(String name) {
        db.execSQL("delete from downloadmusicData where name=?", new String[] { name });
    }
    
    // 查询联系人
    public DownloadMusicInfo queryOne(String name) {
        DownloadMusicInfo lxr = new DownloadMusicInfo();
        Cursor c = db.rawQuery("select * from downloadmusicData where name= ?", new String[] { name });
        while (c.moveToNext()) {
            lxr.setTitle(c.getString(0));
            lxr.setArtist(c.getString(1));
            lxr.setCoverPath(c.getString(2));
            lxr.setMusicPath(c.getString(3));
            lxr.setLrcPath(c.getString(4));
            lxr.setTime(c.getString(5));
        }
        c.close();
        return lxr;
    }
    
    public List<DownloadMusicInfo> queryAlllxr() {
        List<DownloadMusicInfo> lxrs = new ArrayList<DownloadMusicInfo>();
        Cursor c = db.rawQuery("select name from downloadmusicData", null);
        
        while (c.moveToNext()) {
            DownloadMusicInfo lxr = new DownloadMusicInfo();
            lxr.setTitle(c.getString(0));
            lxrs.add(lxr);
            Log.i(TAG, "queryAlllxr: "+c.getString(0));
        }
        c.close();
        return lxrs;
        
    }
    
    // 查询所有的联系人信息
    public List<DownloadMusicInfo> queryMany() {
        ArrayList<DownloadMusicInfo> lxrs = new ArrayList<DownloadMusicInfo>();
        Cursor c = db.rawQuery("select * from downloadmusicData", null);
        while (c.moveToNext()) {
            DownloadMusicInfo lxr = new DownloadMusicInfo();
            lxr.setTitle(c.getString(0));
            lxr.setArtist(c.getString(1));
            lxr.setCoverPath(c.getString(2));
            lxr.setMusicPath(c.getString(3));
            lxr.setLrcPath(c.getString(4));
            lxr.setTime(c.getString(5));
            lxrs.add(lxr);
        }
        c.close();
        return lxrs;
    }
    
    // 检验用户名是否已存在
    public  boolean CheckIsDataAlreadyInDBorNot(String value) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String Query = "Select * from downloadmusicData where name =?";
        Cursor cursor = db.rawQuery(Query, new String[] { value });
        Log.i(TAG, "add: "+cursor.getCount());
        if (cursor.getCount() > 0) {
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }
    
    // 判断信息是否已经存在
    public boolean Dataexist(String name1,String artist,String image,String uri,String Lrc_uri) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String Query = "Select name from downloadmusicData where name =? and artist=? and image=?and uri=?and Lrc_uri=? ";
        Cursor cursor = db.rawQuery(Query, new String[] { name1,artist ,image,uri,Lrc_uri});
        if (cursor.getCount() > 0) {
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }
}
