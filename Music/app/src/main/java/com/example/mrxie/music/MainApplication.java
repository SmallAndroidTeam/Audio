package com.example.mrxie.music;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;

import com.example.mrxie.music.provider.PlaylistInfo;
import com.example.mrxie.music.ui.IConstants;
import com.example.mrxie.music.ui.PreferencesUtility;



/**
 * Created by wm on 2016/2/23.
 */
public class MainApplication extends Application  {
    public static Context context;
    //    private RefWatcher refWatcher;
    private static int MAX_MEM = (int) Runtime.getRuntime().maxMemory() / 4;
    //private static int MAX_MEM = 60 * ByteConstants.MB;
    private long favPlaylist = IConstants.FAV_PLAYLIST;



    @Override
    public void onCreate() {

        super.onCreate();
//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            // This process is dedicated to LeakCanary for heap analysis.
//            // You should not init your app in this process.
//            return;
//        }
       context = this;
  if (!PreferencesUtility.getInstance(this).getFavriateMusicPlaylist()) {
            PlaylistInfo.getInstance(this).addPlaylist(favPlaylist, getResources().getString(R.string.my_fav_playlist),
                    0, "res:/" + R.mipmap.lay_protype_default, "local");
            PreferencesUtility.getInstance(this).setFavriateMusicPlaylist(true);
        }
    }


}