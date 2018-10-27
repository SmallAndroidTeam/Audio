package com.of.music.downloadExecute;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import com.of.music.http.HttpCallback;
import com.of.music.http.HttpClient;
import com.of.music.model.DownloadInfo;
import com.of.music.model.OnlineMusic;
import com.of.music.songListInformation.Music;
import com.of.music.songListInformation.MusicUtils;
import com.of.music.util.onlineUtil.FileUtils;

import java.io.File;


/**
 * 下载音乐
 * Created by wcy on 2016/1/3.
 */
public abstract class DownloadOnlineMusic extends DownloadMusic {
    private OnlineMusic mOnlineMusic;
    public static Music music=new Music();
    public DownloadOnlineMusic(Activity activity, OnlineMusic onlineMusic) {
        super(activity);
        mOnlineMusic = onlineMusic;
    }

    @Override
    protected void download() {
        final String artist = mOnlineMusic.getArtist_name();
        final String title = mOnlineMusic.getTitle();
        music.setTitle(title);
        music.setArtist(artist);
        // 下载歌词
        String lrcFileName = FileUtils.getLrcFileName(artist, title);
        File lrcFile = new File(FileUtils.getLrcDir() + lrcFileName);
        if (!TextUtils.isEmpty(mOnlineMusic.getLrclink()) && !lrcFile.exists()) {
            HttpClient.downloadFile(mOnlineMusic.getLrclink(), FileUtils.getLrcDir(), lrcFileName, null);
        }
        music.setLrcpath(FileUtils.getLrcDir()+lrcFileName);
        Log.i("musicsize","   "+FileUtils.getLrcDir()+lrcFileName+"     ");
        // 下载封面
        String albumFileName = FileUtils.getAlbumFileName(artist, title);
        final File albumFile = new File(FileUtils.getAlbumDir(), albumFileName);
        String picUrl = mOnlineMusic.getPic_big();
        if (TextUtils.isEmpty(picUrl)) {
            picUrl = mOnlineMusic.getPic_small();
        }
        if (!albumFile.exists() && !TextUtils.isEmpty(picUrl)) {
         HttpClient.downloadFile(picUrl, FileUtils.getAlbumDir(), albumFileName, null);
        }
        music.setAlbum(FileUtils.getAlbumDir()+albumFileName);
        // 获取歌曲下载链接
        HttpClient.getMusicDownloadInfo(mOnlineMusic.getSong_id(), new HttpCallback<DownloadInfo>() {
            @Override
            public void onSuccess(DownloadInfo response) {
                if (response == null || response.getBitrate() == null) {
                    onFail(null);
                 
                    return;
                }
                downloadMusic(response.getBitrate().getFile_link(), artist, title, albumFile.getPath());
                onExecuteSuccess(null);
            }
            @Override
            public void onFail(Exception e) {
                onExecuteFail(e);
            }
        });
    music.setUri(FileUtils.getMusicDir()+FileUtils.getMp3FileName(artist, title));
    Log.i("musicsize",music.getUri());
        MusicUtils.sMusicList.add(music);
    Log.i("musicsize","   "+MusicUtils.sMusicList.size()+"     ");
    }
}
