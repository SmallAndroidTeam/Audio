package com.example.mrxie.music.SongListInformation;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

/**
 * 2015年8月15日 16:34:37
 * 博文地址：http://blog.csdn.net/u010156024
 */
public class App extends Application {
	public static Context sContext;
	public static int sScreenWidth;
	public static int sScreenHeight;
	private String TAG="Music";
	@Override
	public void onCreate() {
		super.onCreate();
		//sContext =getApplicationContext();
		Log.i(TAG, "onCreate: "+(sContext!=null));
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		sScreenWidth = dm.widthPixels;
		sScreenHeight = dm.heightPixels;
	}
	
}
