package com.of.music.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;
import com.of.music.R;
import com.of.music.services.MusicService;
import com.of.music.songListInformation.MusicIconLoader;

public class appwidget_provider extends AppWidgetProvider {

    private static int position,duration;
    private static String MusicTitle,albumuri;
    public static boolean isInUse=false;
    public static boolean isLove=false;
    private static boolean isFav ,isPlaying=false;
    private String TAG="Music";
    private final  static int updateWidet=0x10;
    //设置广播消息
    private PendingIntent getPendingIntent(Context context,int buttonId){
        Intent intent=new Intent();
        intent.setClass(context,appwidget_provider.class);
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        intent.setData(Uri.parse("harvic:"+buttonId));
        PendingIntent pendingIntent= PendingIntent.getBroadcast(context,0,intent,0);
         return pendingIntent;
    }
     private synchronized void updateAppWidget(final Context context, final AppWidgetManager appWidgetManager,
                                               boolean updateProgress) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                 RemoteViews remoteViews=new RemoteViews(context.getPackageName(),R.layout.appwidget_provider);
                //绑定按钮的点击事件
                remoteViews.setOnClickPendingIntent(R.id.widget_play,getPendingIntent(context,R.id.widget_play));
                remoteViews.setOnClickPendingIntent(R.id.widget_pre,getPendingIntent(context,R.id.widget_pre));
                remoteViews.setOnClickPendingIntent(R.id.widget_next,getPendingIntent(context,R.id.widget_next));
                remoteViews.setOnClickPendingIntent(R.id.widget_love,getPendingIntent(context,R.id.widget_love));
                //进入app
                Intent intent=new Intent(Intent.ACTION_MAIN);
                intent.setAction(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.setComponent(new ComponentName(context.getPackageName(),"com.of.music.activity.MainActivity"));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                PendingIntent pendingIntent_go=PendingIntent.getActivity(context,5,intent,PendingIntent.FLAG_UPDATE_CURRENT);
                remoteViews.setOnClickPendingIntent(R.id.widget_image,pendingIntent_go);
                remoteViews.setTextViewText(R.id.widget_content,MusicTitle);
                remoteViews.setProgressBar(R.id.widget_progress,(int)duration,(int)position,false);

                if(isPlaying){
                    remoteViews.setImageViewResource(R.id.widget_play, R.drawable.widget_pause_selector);
                }else{
                    remoteViews.setImageViewResource(R.id.widget_play, R.drawable.widget_play_selector);
                }

                if(isLove){
                 remoteViews.setImageViewResource(R.id.widget_love,R.drawable.like_image_selected);
                }else{
                   remoteViews.setImageViewResource(R.id.widget_love,R.drawable.like_image);
                }
                if(albumuri!=null){
                    Bitmap bitmap = MusicIconLoader.getInstance().load(albumuri);
                    remoteViews.setImageViewBitmap(R.id.widget_image, bitmap);
                }else{
                    remoteViews.setImageViewResource(R.id.widget_image, R.drawable.image);
                }
                ComponentName componentName=new ComponentName(context.getApplicationContext(),appwidget_provider.class);
                appWidgetManager.updateAppWidget(componentName,remoteViews);
            }
        }).start();

    }
    private  void pushAction(Context context,String action){
        Intent startIntent=new Intent(context,MusicService.class);
        startIntent.setAction(action);
        context.startService(startIntent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
      //for (int appWidgetId : appWidgetIds) {
          if(isInUse){
              pushAction(context,MusicService.SEND_PROGRESS);
          }

     // }

    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        //第一次加载到屏幕上时
        //开启服务
        isInUse=true;
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        isInUse=false;
    }

    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
        super.onRestored(context, oldWidgetIds, newWidgetIds);
    }
    //获取广播的回调函数
    @Override
    public void onReceive(Context context, Intent intent) {
        String action=intent.getAction();
       //Log.i(TAG, "onReceive: ----------------------"+action);
        if(AppWidgetManager.ACTION_APPWIDGET_ENABLED.equals(action)){
            this.onEnabled(context);
        }else if(AppWidgetManager.ACTION_APPWIDGET_DISABLED.equals(action)){
            this.onDisabled(context);
        }
        if(!isInUse)
        {
            return;
        }
        if(intent.hasCategory(Intent.CATEGORY_ALTERNATIVE)){
            Uri data=intent.getData();
            int buttonID=Integer.parseInt(data.getSchemeSpecificPart());
          switch (buttonID){
              case R.id.widget_play:
                  pushAction(context, MusicService.TOGGLEPAUSE_ACTION);
                  break;
              case R.id.widget_pre:
                  pushAction(context,MusicService.PREVIOUS_ACTION);
                  break;
              case R.id.widget_next:
                  pushAction(context,MusicService.NEXT_ACTION);
                  break;
                  case R.id.widget_love:
                      pushAction(context,MusicService.WIDGET_LOVE_ACTION);
                      break;
                      default:break;
          }
        }
        else if(action.equals(MusicService.META_CHANGED)){
            isPlaying = intent.getBooleanExtra("playing",false);
            updateAppWidget(context,AppWidgetManager.getInstance(context) ,false);
        } else if(action.equals(MusicService.SEND_PROGRESS)){
            duration = intent.getIntExtra("duration",0);
            position = intent.getIntExtra("position",0);
            updateAppWidget(context,AppWidgetManager.getInstance(context) ,true);
        } else if(action.equals(MusicService.MUSIC_CHANGED)){
            MusicTitle = intent.getStringExtra("MusicTitle");
            albumuri = intent.getStringExtra("albumuri");
            isPlaying = intent.getBooleanExtra("playing",false);
            isLove=intent.getBooleanExtra("love",false);
            updateAppWidget(context,AppWidgetManager.getInstance(context) ,false);
        }else if(action.equals(MusicService.STOP_ACTION)){
            isPlaying = intent.getBooleanExtra("playing",false);
            duration = intent.getIntExtra("duration",0);
            position = intent.getIntExtra("position",0);
            updateAppWidget(context,AppWidgetManager.getInstance(context) ,false);
        }
        else if(action.equals(MusicService.LOVE_ACTION)){
            isLove=intent.getBooleanExtra("love",false);
            Log.i(TAG, "onReceive: //////////////"+isLove);
            updateAppWidget(context,AppWidgetManager.getInstance(context) ,false);
        }
        super.onReceive(context, intent);
    }
}

