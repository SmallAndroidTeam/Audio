package com.example.mrxie.music.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mrxie.music.R;
import com.example.mrxie.music.SongListInformation.App;
import com.example.mrxie.music.SongListInformation.Music;
import com.example.mrxie.music.SongListInformation.MusicIconLoader;
import com.example.mrxie.music.SongListInformation.MusicUtils;
import com.example.mrxie.music.Toast.OnlyOneToast;
import com.example.mrxie.music.activity.MainActivity;
import com.example.mrxie.music.fragment.localMusicFragment;
import com.example.mrxie.music.ui.LrcView;
import com.example.mrxie.music.widget.appwidget_provider;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public final class MusicService extends Service {
    public final static MediaPlayer mediaPlayer=new MediaPlayer();
    public   static ArrayList<Music> sMusicList = new ArrayList<Music>(); // 存放歌曲列表
    public static int playingMusicIndex=-1;//正在播放音乐的下标
    private NotificationManager notificationManager;
    private RemoteViews widgetRemoteViews;
    public static enum playAction{start,pause,next,prev,widet}
    public static TextView musicTitle;
    private String TAG="Music";
    public static TextView mPlayMusicStartTimeTextView;
    public static  TextView mPlayMusicStopTimeTextView;
    public static  SeekBar mPlayMusicSeekBar;
    private final Handler handler=new Handler();
    private  static Runnable runnable;
    public static ImageView mPlayMusicButton;
    public static ImageView MusicImage;//歌曲的专辑图片
    public static LrcView showLrcView;
    private Timer timer;
    public static boolean isPlay=false;
    public static boolean isLike=false;
    public static  final String SEND_PROGRESS="com.example.mrxie.music.progress";
    public static final String TOGGLEPAUSE_ACTION = "com.example.mrxie.music.togglepause";
    public static final String PREVIOUS_ACTION = "com.example.mrxie.music.previous";
    public static final String NEXT_ACTION = "com.example.mrxie.music.next";
    public static final String STOP_ACTION = "com.example.mrxie.music.STOP_ACTION";
    public static final String META_CHANGED = "com.example.mrxie.music.metachanged";
    public static final String MUSIC_CHANGED = "com.example.mrxie.music.change_music";
    private static final int NotificationId=1000;
    private static  Notification mNotification;
    private final static int UpdateForeground=0x1;
     private static RemoteViews remoteViews;
    private static   AppWidgetManager appWidgetManager;
    private static  ComponentName componentName;
    private static boolean ForegroundIsExist=false;//判断前台服务是否存在
    private final BroadcastReceiver mIntentReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          handleCommandIntent(intent);
        }
    };
    @Override
    public void onCreate() {
        //注册广播
     final IntentFilter intentFilter=new IntentFilter();
     intentFilter.addAction(TOGGLEPAUSE_ACTION);
     intentFilter.addAction(PREVIOUS_ACTION);
     intentFilter.addAction(SEND_PROGRESS);
     intentFilter.addAction(NEXT_ACTION);
     intentFilter.addAction(STOP_ACTION);
     registerReceiver(mIntentReceiver,intentFilter);
      widgetRemoteViews =new RemoteViews(this.getPackageName(),R.layout.notification);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                autoPlayMusic();

            }
        });

        runnable = new Runnable() {
            @Override
            public void run() {
                //设置widget里面的进度条
                notifyChange(SEND_PROGRESS);
                if (mPlayMusicSeekBar != null)
                    mPlayMusicSeekBar.setProgress(mediaPlayer.getCurrentPosition());
                if (mPlayMusicStartTimeTextView != null)
                    mPlayMusicStartTimeTextView.setText(changeDigitsToTwoDigits(mediaPlayer.getCurrentPosition() / 1000 / 60) + ":" + changeDigitsToTwoDigits(mediaPlayer.getCurrentPosition() / 1000 % 60));
                if (showLrcView != null && showLrcView.hasLrc()) {
                    showLrcView.changeCurrent(mediaPlayer.getCurrentPosition());
                }

                handler.postDelayed(this, 500);
            }
        };

        super.onCreate();
    }


    public static String changeDigitsToTwoDigits(int digit){//将一个数变为二位数
     if(digit<10){
         return "0"+digit;
     }else
     {
         return ""+digit;
     }
    }
    public    void  initMusic(){
        synchronized (this) {
            if (playingMusicIndex != -1) {
                mediaPlayer.reset();
                try {
                    mediaPlayer.setDataSource(sMusicList.get(playingMusicIndex).getUri());
                    mediaPlayer.prepare();
                    if (musicTitle != null)
                        musicTitle.setText(sMusicList.get(playingMusicIndex).getTitle());
                    if (mPlayMusicSeekBar != null)
                        mPlayMusicSeekBar.setMax(mediaPlayer.getDuration());
                    if (sMusicList.get(MusicService.playingMusicIndex).getImage() != null) {//如果音乐专辑图片存在
                        //  OnlyOneToast.makeText(localMusicFragment.activity,sMusicList.get(playingMusicIndex).getImage());
                        Bitmap bitmap = MusicIconLoader.getInstance().load(sMusicList.get(MusicService.playingMusicIndex).getImage());
                        if (MusicImage != null)
                            MusicImage.setImageBitmap(bitmap);

                    } else {
                        if (MusicImage != null)
                            MusicImage.setImageResource(R.drawable.image);
                        //OnlyOneToast.makeText(localMusicFragment.activity,"无图片");
                    }
                    if (showLrcView != null)
                        setLrc();//设置歌词的路径
                    if (mPlayMusicSeekBar != null)
                        mPlayMusicSeekBar.setProgress(mediaPlayer.getCurrentPosition());
                    if (mPlayMusicStartTimeTextView != null)
                        mPlayMusicStartTimeTextView.setText(changeDigitsToTwoDigits(mediaPlayer.getCurrentPosition() / 1000 / 60) + ":" + changeDigitsToTwoDigits(mediaPlayer.getCurrentPosition() / 1000 % 60));
                    if (mPlayMusicStopTimeTextView != null)
                        mPlayMusicStopTimeTextView.setText(changeDigitsToTwoDigits((mediaPlayer.getDuration()) / 1000 / 60) + ":" + changeDigitsToTwoDigits(mediaPlayer.getDuration() / 1000 % 60));

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                OnlyOneToast.makeText(localMusicFragment.activity, "暂无歌曲");
            }
        }
    }

    private static void setLrc(){//设置歌词的路径
        String path=sMusicList.get(MusicService.playingMusicIndex).getLrcpath();
        showLrcView.setLrcPath(path);
    }

    public void startMusic(){
        synchronized (this){
            if(playingMusicIndex==-1){
                OnlyOneToast.makeText(localMusicFragment.activity,"暂无歌曲");
                return;
            }

            if(mediaPlayer.isPlaying()){
                mediaPlayer.pause();
                handler.removeCallbacks(runnable);
            }
            else{
                mediaPlayer.start();
                handler.post(runnable);
            }
        }

    }
    public void autoPlayMusic(){
        synchronized (this) {
            if (playingMusicIndex == -1) {
                OnlyOneToast.makeText(getApplicationContext(), "暂无歌曲");
                return;
            }
            int i;
            for (i = 0; i < localMusicFragment.playMode.length; i++) {
                if (localMusicFragment.currentPlayMode.contentEquals(localMusicFragment.playMode[i])) {
                    break;
                }
            }
            if (i >= localMusicFragment.playMode.length) {
                i = localMusicFragment.playMode.length - 1;
            }
            //只改变播放音乐的下标
            switch (i) {
                case 0://顺序播放
                    if (playingMusicIndex == (localMusicFragment.sMusicList.size() - 1)) {
                        playingMusicIndex = 0;
                        initMusic();
                        mediaPlayer.pause();
                        notifyChange(MUSIC_CHANGED);
                        NotificationChange(NEXT_ACTION);
                        handler.removeCallbacks(runnable);
                    } else {
                        Intent intent3=new Intent(NEXT_ACTION);
                        handleCommandIntent(intent3);
                    }
                    break;
                case 1://列表循环
                   // nextMusic();
                    Intent intent3=new Intent(NEXT_ACTION);
                    handleCommandIntent(intent3);
                    break;
                case 2://单曲循环
                    initMusic();
                    mediaPlayer.start();
                    notifyChange(MUSIC_CHANGED);
                    NotificationChange(NEXT_ACTION);
                    handler.post(runnable);
                    break;
                case 3://随机播放
                    int MusicIndex = new Random().nextInt(sMusicList.size() - 1);
                    while (MusicIndex == playingMusicIndex) {
                        MusicIndex = new Random().nextInt(sMusicList.size() - 1);
                    }
                    playingMusicIndex = MusicIndex;
                    initMusic();
                    mediaPlayer.start();
                    notifyChange(MUSIC_CHANGED);
                    NotificationChange(NEXT_ACTION);
                    handler.post(runnable);
                    break;
                default:
                    break;
            }

            //Log.i(TAG, "autoPlayMusic: "+playingMusicIndex);
        }
    }
    public void stopMusic(){
        synchronized (this){
            mediaPlayer.stop();
            try {
                mediaPlayer.prepare();
                mediaPlayer.seekTo(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void nextMusic(){
        synchronized (this) {
            if (playingMusicIndex == -1) {
                OnlyOneToast.makeText(localMusicFragment.activity, "暂无歌曲");
                return;
            }
            int i;
            for (i = 0; i < localMusicFragment.playMode.length; i++) {
                if (localMusicFragment.currentPlayMode.contentEquals(localMusicFragment.playMode[i])) {
                    break;
                }
            }
            if (i >= localMusicFragment.playMode.length) {
                i = localMusicFragment.playMode.length - 1;
            }
            //只改变播放音乐的下标
            switch (i) {
                case 0://顺序播放
                case 1://列表循环
                case 2://单曲循环
                    playingMusicIndex = (playingMusicIndex == sMusicList.size() - 1) ? 0 : (playingMusicIndex + 1);
                    break;
                case 3://随机播放
                    int MusicIndex = new Random().nextInt(sMusicList.size() - 1);
                    while (MusicIndex == playingMusicIndex) {
                        MusicIndex = new Random().nextInt(sMusicList.size() - 1);
                    }
                    playingMusicIndex = MusicIndex;
                    break;
                default:
                    break;
            }
            Log.i(TAG, "NextMusic: " + playingMusicIndex);
            initMusic();
            mediaPlayer.start();
            handler.post(runnable);
        }

    }

    public void prevMusic(){
        synchronized (this) {
            if (playingMusicIndex == -1) {
                OnlyOneToast.makeText(localMusicFragment.activity, "暂无歌曲");
                return;
            }
            int i;
            for (i = 0; i < localMusicFragment.playMode.length; i++) {
                if (localMusicFragment.currentPlayMode.contentEquals(localMusicFragment.playMode[i])) {
                    break;
                }
            }
            if (i >= localMusicFragment.playMode.length) {
                i = localMusicFragment.playMode.length - 1;
            }
            //只改变播放音乐的下标
            switch (i) {
                case 0://顺序播放
                case 1://列表循环
                case 2://单曲循环
                    playingMusicIndex = (playingMusicIndex == 0) ? (sMusicList.size() - 1) : (playingMusicIndex - 1);
                    break;
                case 3://随机播放
                    int MusicIndex = new Random().nextInt(sMusicList.size() - 1);
                    while (MusicIndex == playingMusicIndex) {
                        MusicIndex = new Random().nextInt(sMusicList.size() - 1);
                    }
                    playingMusicIndex = MusicIndex;
                    break;
                default:
                    break;
            }
            initMusic();
            mediaPlayer.start();
            handler.post(runnable);
        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void handleCommandIntent(Intent intent) {
      String action=intent.getAction();
        //可能已经在桌面建立了widget，一启动后没初始化歌单列表
        if(sMusicList.size()==0)
        {
            App.sContext=getApplicationContext();
            MusicUtils.initMusicList();
            if(MusicUtils.sMusicList.size()>0)
            {
              sMusicList=MusicUtils.sMusicList;
                playingMusicIndex=0;
                initMusic();
            }
        }
      if(playingMusicIndex==-1)
      {   OnlyOneToast.makeText(getApplicationContext(),"暂无歌曲");
          return;
      }
      //widget创建或者刷新第一步都要执行这动作
      if(SEND_PROGRESS.equals(action)){//发送现在音乐的一些信息
          notifyChange(MUSIC_CHANGED);
          handler.post(runnable);
      }else if(TOGGLEPAUSE_ACTION.equals(action)){//按下widget中间的播放按钮

          if(mediaPlayer.isPlaying()){
              handler.removeCallbacks(runnable);
          }else{
              handler.post(runnable);
          }
          startMusic();
          if(localMusicFragment.getmPlayMusicButton()!=null){//如果是在widget点击了播放按钮，那么相应的app如果打开，那么里面的播放按钮的状态也随之改变
              if(mediaPlayer.isPlaying()){
                  localMusicFragment.getmPlayMusicButton().setImageResource(R.drawable.play_music);
              }else{
                  localMusicFragment.getmPlayMusicButton().setImageResource(R.drawable.pause_image);
              }
          }
          notifyChange(META_CHANGED);
          NotificationChange(TOGGLEPAUSE_ACTION);

      }else if(PREVIOUS_ACTION.equals(action)){//按下widget上一首按钮
          prevMusic();
          notifyChange(MUSIC_CHANGED);
         NotificationChange(PREVIOUS_ACTION);
          if(localMusicFragment.getmPlayMusicButton()!=null) {//如果是在widget点击了上一首按钮，那么相应的app如果打开，那么里面的播放按钮的状态也随之改变
              localMusicFragment.getmPlayMusicButton().setImageResource(R.drawable.play_music);
          }
      }else if(NEXT_ACTION.equals(action)){//按下widget下一首按钮
            nextMusic();
          notifyChange(MUSIC_CHANGED);
          NotificationChange(NEXT_ACTION);
           if(localMusicFragment.getmPlayMusicButton()!=null) {//如果是在widget点击了下一首按钮，那么相应的app如果打开，那么里面的播放按钮的状态也随之改变
              localMusicFragment.getmPlayMusicButton().setImageResource(R.drawable.play_music);
          }
      }else if(STOP_ACTION.equals(action)){//前台服务点击关闭按钮
          ForegroundIsExist=false;
          mNotification=null;
             stopMusic();
             handler.removeCallbacks(runnable);
             if (mPlayMusicSeekBar != null)
              mPlayMusicSeekBar.setProgress(0);
          if(localMusicFragment.getmPlayMusicButton()!=null){//播放按钮设为停止按钮
              localMusicFragment.getmPlayMusicButton().setImageResource(R.drawable.pause_image);
          }
          if (mPlayMusicStartTimeTextView != null)//播放时间设置0
              mPlayMusicStartTimeTextView.setText("00:00");
          stopForeground(true);
          notifyChange(STOP_ACTION);
      }
    }

    private   void NotificationChange(final String what){

        new Thread(new Runnable() {
            @Override
            public void run() {
                if(!ForegroundIsExist)
                {
                    return;
                }else  if(mNotification==null){
                    timer=new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                         if(mNotification!=null||!ForegroundIsExist)
                         {
                             timer.cancel();
                             NotificationChange(what);
                         }
                        }
                    },100);
                  return;
                }
                if(TOGGLEPAUSE_ACTION.equals(what)){
                    if(mediaPlayer.isPlaying()){
                        mNotification.contentView.setImageViewResource(R.id.widget_play,R.drawable.widget_pause_selector);
                    }else{
                        mNotification.contentView.setImageViewResource(R.id.widget_play,R.drawable.widget_play_selector);
                    }
                }else if(PREVIOUS_ACTION.equals(what)||NEXT_ACTION.equals(what))
                {  String widget_title = sMusicList.get(playingMusicIndex).getTitle();
                    mNotification.contentView.setTextViewText(R.id.widget_content, widget_title);//设置歌曲名
                    mNotification.contentView.setProgressBar(R.id.widget_progress, mediaPlayer.getDuration(), mediaPlayer.getCurrentPosition(), false);

                    if (sMusicList.get(MusicService.playingMusicIndex).getImage() != null) {//如果音乐专辑图片存在
                        Bitmap bitmap = MusicIconLoader.getInstance().load(sMusicList.get(MusicService.playingMusicIndex).getImage());
                        mNotification.contentView.setImageViewBitmap(R.id.widget_image, bitmap);
                    } else {
                        mNotification.contentView.setImageViewResource(R.id.widget_image, R.drawable.image);
                    }
                    //进度
                    //执行更新精度条的线程
                    handler.post(runnable);
                    if (mediaPlayer.isPlaying()) {
                        mNotification.contentView.setImageViewResource(R.id.widget_play, R.drawable.widget_pause_selector);
                    } else {
                        mNotification.contentView.setImageViewResource(R.id.widget_play, R.drawable.widget_play_selector);
                    }
                }
                notificationManager.notify(NotificationId,mNotification);
            }
        }).start();
    }

    private void notifyChange(final String what){
       if(appwidget_provider.isInUse==false)
       {
           return;
       }
        if(SEND_PROGRESS.equals(what)){
            final Intent intent = new Intent(SEND_PROGRESS);
            intent.putExtra("position", mediaPlayer.getCurrentPosition());
            intent.putExtra("duration",  mediaPlayer.getDuration());
            intent.setComponent(new ComponentName(getApplicationContext(),appwidget_provider.class));
           sendBroadcast(intent);
        }else if(MUSIC_CHANGED.equals(what)){
            final Intent intent1 = new Intent();
            intent1.setAction(MUSIC_CHANGED);
            intent1.putExtra("playing", mediaPlayer.isPlaying());
            intent1.putExtra("albumuri",  sMusicList.get(MusicService.playingMusicIndex).getImage());
            intent1.putExtra("MusicTitle",sMusicList.get(playingMusicIndex).getTitle());
            intent1.setComponent(new ComponentName(getApplicationContext(),appwidget_provider.class));
            sendBroadcast(intent1);
        }else if(META_CHANGED.equals(what)){
            final Intent intent1 = new Intent();
            intent1.setAction(META_CHANGED);
            intent1.putExtra("playing", mediaPlayer.isPlaying());
            intent1.setComponent(new ComponentName(getApplicationContext(),appwidget_provider.class));
            sendBroadcast(intent1);
        }else if(STOP_ACTION.equals(what)){
            final Intent intent = new Intent(STOP_ACTION);
            intent.putExtra("position",0);
            intent.putExtra("duration",  mediaPlayer.getDuration());
            intent.putExtra("playing", false);
            intent.setComponent(new ComponentName(getApplicationContext(),appwidget_provider.class));
            sendBroadcast(intent);
        }
    }
    //初始前台服务
    private void initNotification(){
            String widget_title = sMusicList.get(playingMusicIndex).getTitle();
            widgetRemoteViews.setTextViewText(R.id.widget_content, widget_title);//设置歌曲名
           // widgetRemoteViews.setProgressBar(R.id.widget_progress, mediaPlayer.getDuration(), mediaPlayer.getCurrentPosition(), false);

            if (sMusicList.get(MusicService.playingMusicIndex).getImage() != null) {//如果音乐专辑图片存在
                Bitmap bitmap = MusicIconLoader.getInstance().load(sMusicList.get(MusicService.playingMusicIndex).getImage());
                widgetRemoteViews.setImageViewBitmap(R.id.widget_image, bitmap);
            } else {
                widgetRemoteViews.setImageViewResource(R.id.widget_image, R.drawable.image);
            }
            //进度
            //执行更新精度条的线程
            handler.post(runnable);
            if (mediaPlayer.isPlaying()) {
                widgetRemoteViews.setImageViewResource(R.id.widget_play, R.drawable.widget_pause_selector);
            } else {
                widgetRemoteViews.setImageViewResource(R.id.widget_play, R.drawable.widget_play_selector);
            }
    }
    private Notification  getmNotification(){
        final int PAUSE_FLAG = 0x1;
        final int NEXT_FLAG = 0x2;
        final int PREV_FLAG = 0x3;
             final  Context ForegroundContext=this;
                initNotification();
                //设置前台服务的绑定事件
                Intent pauseIntent = new Intent(TOGGLEPAUSE_ACTION);
                //pauseIntent.putExtra("FLAG", PAUSE_FLAG);
                PendingIntent pausePIntent = PendingIntent.getBroadcast(ForegroundContext, 0, pauseIntent, 0);
                widgetRemoteViews.setOnClickPendingIntent(R.id.widget_play, pausePIntent);

                Intent nextIntent = new Intent(NEXT_ACTION);
                // nextIntent.putExtra("FLAG", NEXT_FLAG);
                PendingIntent nextPIntent = PendingIntent.getBroadcast(ForegroundContext, 0, nextIntent, 0);
                widgetRemoteViews.setOnClickPendingIntent(R.id.widget_next, nextPIntent);

                Intent preIntent = new Intent(PREVIOUS_ACTION);
                // preIntent.putExtra("FLAG", PREV_FLAG);
                PendingIntent prePIntent = PendingIntent.getBroadcast(ForegroundContext, 0, preIntent, 0);
                widgetRemoteViews.setOnClickPendingIntent(R.id.widget_pre, prePIntent);

                Intent stopIntent=new Intent(STOP_ACTION);
                PendingIntent stopPIntent=PendingIntent.getBroadcast(ForegroundContext,0,stopIntent,0);
                widgetRemoteViews.setOnClickPendingIntent(R.id.audio_stop,stopPIntent);

                final Intent nowPlayingIntent=new Intent(Intent.ACTION_MAIN);
                nowPlayingIntent.setAction(Intent.ACTION_MAIN);
                nowPlayingIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                nowPlayingIntent.setComponent(new ComponentName("com.example.mrxie.music","com.example.mrxie.music.activity.MainActivity"));
                nowPlayingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                PendingIntent click = PendingIntent.getActivity(ForegroundContext,0,nowPlayingIntent,PendingIntent.FLAG_UPDATE_CURRENT);
                if(mNotification==null){
                    if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){//sdk版本大于26
                        String id ="channel_1";
                        String description="143";
                        int improtance=NotificationManager.IMPORTANCE_LOW;
                        NotificationChannel channel=new NotificationChannel(id,description,improtance);
                        channel.enableLights(true);
                        channel.enableVibration(true);
                        notificationManager.createNotificationChannel(channel);
                        mNotification=new Notification.Builder(ForegroundContext,id).setContent(widgetRemoteViews)
                                .setSmallIcon(R.drawable.ic_notification)
                                .setContentIntent(click).setWhen(System.currentTimeMillis()).setAutoCancel(false)
                                .build();
                        //.setWhen(System.currentTimeMillis()).setShowWhen(true)
                    }
                    else{
                        NotificationCompat.Builder builder=new NotificationCompat.Builder(ForegroundContext).setContent(widgetRemoteViews).setAutoCancel(false)
                                .setSmallIcon(R.drawable.ic_notification).setContentIntent(click).setWhen(System.currentTimeMillis());
                        mNotification=builder.build();
                    }

                }else{
                    mNotification.contentView=remoteViews;
                }
                return mNotification;
    }
    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
       if(intent==null)
           return super.onStartCommand(intent, flags, startId);
                if(!ForegroundIsExist)
                { new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ForegroundIsExist=true;
                        startForeground(NotificationId,getmNotification());

                    }
                }).start();

                }
                    if(intent.getAction()!=null)
                    {
                        handleCommandIntent(intent);
                    }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {//当关闭服务时
        mediaPlayer.stop();
        unregisterReceiver(mIntentReceiver);
         stopForeground(true);
        super.onDestroy();
    }
}
