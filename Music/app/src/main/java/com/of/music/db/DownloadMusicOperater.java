package com.of.music.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.of.music.model.DownloadMusicInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DownloadMusicOperater{
    private  DownloadMusicOpenHelper dbHelper;
    private  SQLiteDatabase db;
    private final static float MAX_DOWNVIDEO_TOTAL_SIZE=10*1024*1024;
    private String TAG="downloadMusic";
    public DownloadMusicOperater(Context context) {
        dbHelper = new DownloadMusicOpenHelper(context, "downloadmusicData", null, 1);
       
        db = dbHelper.getWritableDatabase();
    }
    //如果下载的音乐大于1G则先把音乐下载下来，再通过子线程把最先下载的音乐删除,直到下载的总视频大小不大于1G
    public  synchronized void keepDownloadVideoTotalSize(final String DownLoadPath){
        if(DownLoadPath==null){
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file=new File(DownLoadPath);
                List<DownloadMusicInfo> downVideoPaths=new ArrayList<>();//存储视频的下载地址
                double currentDownVideoTotalSize=0;//当前下载视频的总的大小
                if(!file.exists()||file.isDirectory()){
                    Log.i("movie2", "下载路径获取失败"+DownLoadPath+"//"+file.exists()+"//"+file.isDirectory());
                }else{
                    Cursor cursor=db.rawQuery("select * from downloadmusicData order by data asc",null);
                    if(cursor.moveToFirst()){
                        do{
                            String path=cursor.getString(cursor.getColumnIndex("uri"));
                            String modify=cursor.getString(cursor.getColumnIndex("data"));//修改时间
                            File file1=new File(path);
                            if(file1.exists()&&file1.isFile())
                            {
                                Log.i("dsfsadf", "run: "+path+"//"+modify);
                                DownloadMusicInfo video=new DownloadMusicInfo();
                                video.setMusicPath(path);
                             
                                downVideoPaths.add(video);
                                currentDownVideoTotalSize+=file1.length();//得到总的下载视频大小
                            }
                            
                        }while (cursor.moveToNext());
                    }
                    Log.i("movie2", "当前下载视频总的大小为："+currentDownVideoTotalSize+"//"+currentDownVideoTotalSize/1024/1024+"M"+"\n总的视频个数为：" +
                            ""+downVideoPaths.size());
                    
                    if(currentDownVideoTotalSize>MAX_DOWNVIDEO_TOTAL_SIZE){//如果下载视频的总大小大于1G
                        for(DownloadMusicInfo video:downVideoPaths){
                            String path=video.getMusicPath();
                            File file1=new File(path);
                            long videoSize=file1.length();
                            if(file1.exists()&&file1.isFile()){
                                if(file1.delete())//如果删成功
                                {
                                    deleteSaveVideo(path);//删除存储的视频
                                    currentDownVideoTotalSize-=videoSize;
                                    if(currentDownVideoTotalSize<=MAX_DOWNVIDEO_TOTAL_SIZE){
                                        Log.i("movie2", "现在下载视频总的大小为："+currentDownVideoTotalSize+"//"+currentDownVideoTotalSize/1024/1024+"M");
                                        break;
                                    }
                                }
                            }
                            
                        }
                    }
                    
                }
            }
        }).start();
        
    }
    
    //删除存储的地址
    public synchronized  void deleteSaveVideo(String saveVideoPath){
        db.execSQL("delete from downloadmusicData where uri=?", new String[] { saveVideoPath });
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
    // 删除联系人根据歌名
    public void delete(String name) {
        db.execSQL("delete from downloadmusicData where name=?", new String[] { name });
    }
    
    // 查询联系人根据歌名
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
