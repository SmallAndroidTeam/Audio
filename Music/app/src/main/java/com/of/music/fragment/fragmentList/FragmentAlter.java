package com.of.music.fragment.fragmentList;

import android.support.v4.app.Fragment;

public class FragmentAlter {
    public static Fragment downloadFragmenet=null;
    public static Fragment recentlyFragment=null;
    public static void setDownloadFragmenet(Fragment downloadfragmenet){
       downloadFragmenet=downloadfragmenet;
    }
    public static void setRecentlyFragment(Fragment recentlyfragmenet){
        recentlyFragment=recentlyfragmenet;
    }
    
    public static Fragment getDownloadFragmenet() {
        return downloadFragmenet;
    }
    
    public static Fragment getRecentlyFragment() {
        return recentlyFragment;
    }
}
