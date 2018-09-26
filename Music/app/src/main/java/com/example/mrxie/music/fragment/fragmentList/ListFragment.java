package com.example.mrxie.music.fragment.fragmentList;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.example.mrxie.music.songListInformation.App;
import com.example.mrxie.music.songListInformation.MusicUtils;
import com.example.mrxie.music.ui.IConstants;
import com.example.mrxie.music.util.CommonUtils;

public class ListFragment extends Fragment {

    Context mContext = App.sContext;

    //设置音乐overflow条目
    private void setMusicInfo() {

        if (CommonUtils.isLollipop() && ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            loadCount(false);
        } else {
            loadCount(true);
        }
    }

    private void loadCount(boolean has) {
        int recentMusicCount = 0,favoriteMusicCount = 0, localMusicCount = 0, artistsCount = 0,albumCount = 0, downLoadCount = 0 ,usbCount = 0;
        if(has){
            try{
                localMusicCount = MusicUtils.queryMusic(mContext, IConstants.START_FROM_LOCAL).size();
                //recentMusicCount = TopTracksLoader.getCount(MainApplication.context, TopTracksLoader.QueryType.RecentSongs);
                //downLoadCount = DownFileStore.getInstance(mContext).getDownLoadedListAll().size();
                artistsCount = MusicUtils.queryArtist(mContext).size();
                albumCount = MusicUtils.queryAlbums(mContext).size();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
