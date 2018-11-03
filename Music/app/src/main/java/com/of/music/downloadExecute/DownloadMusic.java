package com.of.music.downloadExecute;

import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.of.music.Application.App;
import com.of.music.Application.AppCache;
import com.of.music.Application.Preferences;
import com.of.music.R;
import com.of.music.db.DownloadMusicOpenHelper;
import com.of.music.db.DownloadMusicOperater;
import com.of.music.db.MusicOperator;
import com.of.music.fragment.fragmentList.DownloadListFragment;
import com.of.music.fragment.fragmentList.FragmentAlter;
import com.of.music.fragment.fragmentList.RecentlyListFragment;
import com.of.music.info.MusicName;
import com.of.music.model.DownloadMusicInfo;
import com.of.music.model.IExecutor;
import com.of.music.services.MusicService;
import com.of.music.util.comparator.NetworkUtils;
import com.of.music.util.onlineUtil.FileUtils;
import com.of.music.util.onlineUtil.ToastUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by hzwangchenyan on 2017/1/20.
 */
public abstract class DownloadMusic implements IExecutor<Void> {
    private Activity mActivity;
    public static DownloadMusicOperater downloadMusicOperater;
    public static final ArrayList<DownloadMusicInfo> downloadMusicInfos=new ArrayList<>();
//    private  DownloadMusicOpenHelper dbHelper;
//    private SQLiteDatabase db;
    
    public DownloadMusic(Activity activity) {
        mActivity = activity;
        downloadMusicOperater=new DownloadMusicOperater(activity);
//        dbHelper = new DownloadMusicOpenHelper(context, "downloadmusicData", null, 1);
//        db = dbHelper.getWritableDatabase();
    }
    
    @Override
    public void execute() {
        checkNetwork();
    }
    
    private void checkNetwork() {
        Preferences.init(App.sContext);
        boolean mobileNetworkDownload = Preferences.enableMobileNetworkDownload();
        if (NetworkUtils.isActiveNetworkMobile(mActivity) && !mobileNetworkDownload) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setTitle(R.string.tips);
            builder.setMessage(R.string.download_tips);
            builder.setPositiveButton(R.string.download_tips_sure, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    downloadWrapper();
                }
            });
            builder.setNegativeButton(R.string.cancel, null);
            Dialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        } else {
            downloadWrapper();
        }
    }
    
    private void downloadWrapper() {
        onPrepare();
        download();
    }
    
    protected abstract void download();
    
    protected void downloadMusic(String url, String artist, String title, String coverPath) {
        try {
            
            String fileName = FileUtils.getMp3FileName(artist, title);
            Log.i("audio11","  "+fileName);
            Uri uri = Uri.parse(url);
            Log.i("audio11","  "+uri);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle(FileUtils.getFileName(artist, title));
            request.setDescription("正在下载…");
            request.setDestinationInExternalPublicDir(FileUtils.getRelativeMusicDir(), fileName);
            request.setMimeType(MimeTypeMap.getFileExtensionFromUrl(url));
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
            request.setAllowedOverRoaming(false); // 不允许漫游
            Log.i("audio11","11");
            DownloadManager downloadManager = (DownloadManager) App.sContext.getSystemService(Context.DOWNLOAD_SERVICE);
            long id = downloadManager.enqueue(request);
            Log.i("audio11","12");
            final String musicAbsPath = FileUtils.getMusicDir().concat(fileName);
//            MusicName musicName=new MusicName(artist,title,coverPath,musicAbsPath,FileUtils.getMusicDir()+FileUtils.getMp3FileName(artist, title));
            String data=String.valueOf(System.currentTimeMillis());
            Log.i("audio11",data);
            if(!downloadMusicOperater.Dataexist(title,musicAbsPath,coverPath,artist,FileUtils.getLrcDir()+FileUtils.getLrcFileName(artist, title))) {
                //修改数据库中的数据
                DownloadMusicInfo downloadMusicInfo = new DownloadMusicInfo(title, musicAbsPath, coverPath, artist, FileUtils.getLrcDir() + FileUtils.getLrcFileName(artist, title), data);
                downloadMusicOperater.add(downloadMusicInfo);
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
        if(new File(musicAbsPath).exists()){
            downloadMusicOperater.keepDownloadVideoTotalSize(musicAbsPath);
            this.cancel();
        }
                    }
                },0,100);
                if(FragmentAlter.getDownloadFragmenet()!=null)
                ((DownloadListFragment)FragmentAlter.getDownloadFragmenet()).DownloadAlter();
            }
            Log.i("downloadf","///////"+downloadMusicInfos.size());
        } catch (Throwable th) {
            th.printStackTrace();
            Log.i("audio11", "downloadMusic: "+th.getMessage());
            ToastUtils.show("下载失败");
        }
    }
   
    
}
