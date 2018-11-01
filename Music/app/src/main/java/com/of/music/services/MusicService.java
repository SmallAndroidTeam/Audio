package com.of.music.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Visualizer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;

import com.of.music.R;
import com.of.music.Application.App;
import com.of.music.SharedPreferencesToSaveEQSeekBar.SharedPreferencesHelper;
import com.of.music.Toast.OnlyOneToast;
import com.of.music.activity.EqualizerActivity;
import com.of.music.defineViewd.VisualizerView;
import com.of.music.fragment.LocalMusicFragment;
import com.of.music.info.MusicName;
import com.of.music.songListInformation.Music;
import com.of.music.songListInformation.MusicIconLoader;
import com.of.music.songListInformation.MusicUtils;
import com.of.music.ui.LrcView;
import com.of.music.widget.appwidget_provider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public final class MusicService extends Service {
    public static MediaPlayer mediaPlayer=new MediaPlayer();
    public static int playingMusicIndex=-1;//正在播放音乐的下标
    private static NotificationManager notificationManager;
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
    public static String s;
    public static String ss;
    public static String sss;
    public static  String ssss;
    public static  final String SEND_PROGRESS="com.of.music.progress";
    public static final String TOGGLEPAUSE_ACTION = "com.of.music.togglepause";
    public static final String PREVIOUS_ACTION = "com.of.music.previous";
    public static final String NEXT_ACTION = "com.of.music.next";
    public static final String STOP_ACTION = "com.of.music.STOP_ACTION";
    public static final String META_CHANGED = "com.of.music.metachanged";
    public static final String MUSIC_CHANGED = "com.of.music.change_music";
    public static final String MY_BROCAST = "com.of.music.MY_BROADCAST";
    public static final  String WIDGET_LOVE_ACTION="com.of.music.WIDGET_LOVE_BROADCAST";
    public static final  String LOVE_ACTION="com.of.music.LOVE_BROADCAST";
    public static final  String NOTIFICATION_LOVE_ACTION="com.of.music.NOTIFICATION_LOVE_BROADCAST";
    public static final String PLAYVIEW_LOVE_ACTION="ADD_LOVE_ACTION";
    public static final String UPDATE_ACTION="update";
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
    
    // 定义系统的频谱
    public Visualizer mVisualizer;
    // 定义系统的均衡器
    public Equalizer mEqualizer;
    public VisualizerView mVisualizerView;
    public static final float VISUALIZER_HEIGHT_DIP = 50f;
    private SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(App.sContext,"EqualizerSeekbar");
    public int progress_pre=0;
    @Override
    public void onCreate() {
        //注册广播
        final IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(TOGGLEPAUSE_ACTION);
        intentFilter.addAction(PREVIOUS_ACTION);
        intentFilter.addAction(SEND_PROGRESS);
        intentFilter.addAction(NEXT_ACTION);
        intentFilter.addAction(STOP_ACTION);
        intentFilter.addAction(WIDGET_LOVE_ACTION);
        intentFilter.addAction(NOTIFICATION_LOVE_ACTION);
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
    public    boolean  initMusic(){
        synchronized (this) {
            if (isSatisfyingPlayConditions()) {
                mediaPlayer.reset();
                try {
                    Log.i("download11", "initMusic: "+LocalMusicFragment.sMusicList.get(playingMusicIndex).getUri()+"//"+
                            LocalMusicFragment.sMusicList.size());
                    String musicAddress=LocalMusicFragment.sMusicList.get(playingMusicIndex).getUri();
                    while(!musicAddress.toLowerCase().startsWith("http")&&!new File(musicAddress).exists()){
                        ArrayList<Music> sMusicList= LocalMusicFragment.sMusicList;
                        sMusicList.remove(playingMusicIndex);
                        LocalMusicFragment.sMusicList=sMusicList;
                        if(sMusicList.size()==0){
                            playingMusicIndex=-1;
                            if (musicTitle != null) {
                                musicTitle.setText("无标题");
                            }
                            if (MusicImage != null)
                                MusicImage.setImageResource(R.drawable.image);
                            if (showLrcView != null)
                            {
                               
                               //不显示歌词，歌词设为无歌词（无完成)
                               
                            }
                            
                            if (mPlayMusicStopTimeTextView != null){
                                mPlayMusicStopTimeTextView.setText("00:00");
                            }
                            stopMusic();
                            return false;
                        }
                        Log.i("download11", "initMusic: "+sMusicList.size());
                        playingMusicIndex=playingMusicIndex>=sMusicList.size()-1?0:playingMusicIndex+1;
                        musicAddress=LocalMusicFragment.sMusicList.get(playingMusicIndex).getUri();
                    }
                    
                    mediaPlayer.setDataSource(LocalMusicFragment.sMusicList.get(playingMusicIndex).getUri());
                    mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                        @Override
                        public boolean onError(MediaPlayer mp, int what, int extra) {
                            nextMusic();
                            return false;
                        }
                    });
                    mediaPlayer.prepare();
                    if (musicTitle != null) {
                        musicTitle.setText(LocalMusicFragment.sMusicList.get(playingMusicIndex).getTitle());
                    }
                    s=LocalMusicFragment.sMusicList.get(playingMusicIndex).getArtist();                        //获取歌曲信息
                    ss=LocalMusicFragment.sMusicList.get(playingMusicIndex).getImage();
                    sss=LocalMusicFragment.sMusicList.get(playingMusicIndex).getUri();
                    ssss=LocalMusicFragment.sMusicList.get(playingMusicIndex).getLrcpath();
                    Intent UpdateIntent=new Intent(UPDATE_ACTION);
                    handleCommandIntent(UpdateIntent);
                    if (mPlayMusicSeekBar != null)
                        mPlayMusicSeekBar.setMax(mediaPlayer.getDuration());
                    if (LocalMusicFragment.sMusicList.get(MusicService.playingMusicIndex).getImage() != null) {//如果音乐专辑图片存在
                        //  OnlyOneToast.makeText(localMusicFragment.activity,sMusicList.get(playingMusicIndex).getImage());
                        
                        Bitmap bitmap = MusicIconLoader.getInstance().load(LocalMusicFragment.sMusicList.get(MusicService.playingMusicIndex).getImage());
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
                    return false;
                }
            } else {
                OnlyOneToast.makeText(LocalMusicFragment.activity, "暂无歌曲");
            }
        }
        return true;
    }
    
    //判断是否满足播放条件
    private boolean isSatisfyingPlayConditions(){
        int size=LocalMusicFragment.sMusicList.size();
        if(playingMusicIndex<0||size==0||playingMusicIndex>size-1){
            return  false;
        }
        return  true;
    }
    private static void setLrc(){//设置歌词的路径
        String path=LocalMusicFragment.sMusicList.get(MusicService.playingMusicIndex).getLrcpath();
        if(path!=null)
            showLrcView.setLrcPath(path);
    }
    
    public void startMusic(){
        synchronized (this){
            
            if(!isSatisfyingPlayConditions()){
                OnlyOneToast.makeText(LocalMusicFragment.activity,"暂无歌曲");
                return;
            }
            
            if(mediaPlayer.isPlaying()){
                mediaPlayer.pause();
                handler.removeCallbacks(runnable);
            }
            else{
                //重点
                mediaPlayer.start();
                handler.post(runnable);
            }
        }
        
    }
    public void autoPlayMusic(){
        synchronized (this) {
            if (!isSatisfyingPlayConditions()) {
                OnlyOneToast.makeText(getApplicationContext(), "暂无歌曲");
                return;
            }
            int i;
            for (i = 0; i < LocalMusicFragment.playMode.length; i++) {
                if (LocalMusicFragment.currentPlayMode.contentEquals(LocalMusicFragment.playMode[i])) {
                    break;
                }
            }
            if (i >= LocalMusicFragment.playMode.length) {
                i = LocalMusicFragment.playMode.length - 1;
            }
            //只改变播放音乐的下标
            switch (i) {
                case 0://顺序播放
                    if (playingMusicIndex == (LocalMusicFragment.sMusicList.size() - 1)) {
                        playingMusicIndex = 0;
                        if(!initMusic()){
                            OnlyOneToast.makeText(getApplicationContext(),"当前无歌曲");
                            return;
                        }
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
                    if(!initMusic()){
                        OnlyOneToast.makeText(getApplicationContext(),"当前无歌曲");
                        return;
                    }
                    mediaPlayer.start();
                    notifyChange(MUSIC_CHANGED);
                    NotificationChange(NEXT_ACTION);
                    handler.post(runnable);
                    break;
                case 3://随机播放
                    int MusicIndex =new Random().nextInt(LocalMusicFragment.sMusicList.size());
                    while (MusicIndex == playingMusicIndex&&LocalMusicFragment.sMusicList.size()>1) {
                        MusicIndex = new Random().nextInt(LocalMusicFragment.sMusicList.size());
                    }
                    playingMusicIndex = MusicIndex;
                    if(!initMusic()){
                        OnlyOneToast.makeText(getApplicationContext(),"当前无歌曲");
                        return;
                    }
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
                handler.removeCallbacks(runnable);
            } catch (Exception e) {
                e.printStackTrace();
            }
            LocalMusicFragment.getmPlayMusicButton().setImageResource(R.drawable.pause_image);
        }
    }
    public void nextMusic(){
        synchronized (this) {
            if (!isSatisfyingPlayConditions()) {
                OnlyOneToast.makeText(LocalMusicFragment.activity, "暂无歌曲");
                return;
            }
            int i;
            for (i = 0; i < LocalMusicFragment.playMode.length; i++) {
                if (LocalMusicFragment.currentPlayMode.contentEquals(LocalMusicFragment.playMode[i])) {
                    break;
                }
            }
            if (i >= LocalMusicFragment.playMode.length) {
                i = LocalMusicFragment.playMode.length - 1;
            }
            //只改变播放音乐的下标
            switch (i) {
                case 0://顺序播放
                case 1://列表循环
                case 2://单曲循环
                    playingMusicIndex = (playingMusicIndex == LocalMusicFragment.sMusicList.size() - 1) ? 0 : (playingMusicIndex + 1);
                    break;
                case 3://随机播放
                    int MusicIndex =new Random().nextInt(LocalMusicFragment.sMusicList.size());
                    while (MusicIndex == playingMusicIndex&&LocalMusicFragment.sMusicList.size()>1) {
                        MusicIndex = new Random().nextInt(LocalMusicFragment.sMusicList.size());
                    }
                    playingMusicIndex = MusicIndex;
                    break;
                default:
                    break;
            }
            Log.i(TAG, "NextMusic: " + playingMusicIndex);
            if(!initMusic()){
                OnlyOneToast.makeText(getApplicationContext(),"当前无歌曲");
                return;
            }
            mediaPlayer.start();
            handler.post(runnable);
        }
        
    }
    
    public void prevMusic(){
        synchronized (this) {
            if (!isSatisfyingPlayConditions()) {
                OnlyOneToast.makeText(LocalMusicFragment.activity, "暂无歌曲");
                return;
            }
            int i;
            for (i = 0; i < LocalMusicFragment.playMode.length; i++) {
                if (LocalMusicFragment.currentPlayMode.contentEquals(LocalMusicFragment.playMode[i])) {
                    break;
                }
            }
            if (i >= LocalMusicFragment.playMode.length) {
                i = LocalMusicFragment.playMode.length - 1;
            }
            //只改变播放音乐的下标
            switch (i) {
                case 0://顺序播放
                case 1://列表循环
                case 2://单曲循环
                    playingMusicIndex = (playingMusicIndex == 0) ? (LocalMusicFragment.sMusicList.size() - 1) : (playingMusicIndex - 1);
                    break;
                case 3://随机播放
                    int MusicIndex =new Random().nextInt(LocalMusicFragment.sMusicList.size());
                    while (MusicIndex == playingMusicIndex&&LocalMusicFragment.sMusicList.size()>1) {
                        MusicIndex = new Random().nextInt(LocalMusicFragment.sMusicList.size());
                    }
                    playingMusicIndex = MusicIndex;
                    break;
                default:
                    break;
            }
            if(!initMusic()){
                OnlyOneToast.makeText(getApplicationContext(),"当前无歌曲");
                return;
            }
            mediaPlayer.start();
            handler.post(runnable);
        }
    }
    
    public static void timing(int time){
        Timer nTimer = new Timer();
        nTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mediaPlayer.pause();
            }
        },time);
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    private void handleCommandIntent(Intent intent) {
        String action=intent.getAction();
        //可能已经在桌面建立了widget，一启动后没初始化歌单列表
        if(LocalMusicFragment.sMusicList.size()==0)
        {
            App.sContext=getApplicationContext();
            MusicUtils.initMusicList();
            if(MusicUtils.sMusicList.size()>0)
            {
                LocalMusicFragment.sMusicList=MusicUtils.sMusicList;
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
            if(LocalMusicFragment.getmPlayMusicButton()!=null){//如果是在widget点击了播放按钮，那么相应的app如果打开，那么里面的播放按钮的状态也随之改变
                if(mediaPlayer.isPlaying()){
                    LocalMusicFragment.getmPlayMusicButton().setImageResource(R.drawable.play_music);
                }else{
                    LocalMusicFragment.getmPlayMusicButton().setImageResource(R.drawable.pause_image);
                }
            }
            notifyChange(META_CHANGED);
            NotificationChange(TOGGLEPAUSE_ACTION);
        }else if(PREVIOUS_ACTION.equals(action)){//按下widget上一首按钮
            prevMusic();
            notifyChange(MUSIC_CHANGED);
            NotificationChange(PREVIOUS_ACTION);
            if(LocalMusicFragment.getmPlayMusicButton()!=null) {//如果是在widget点击了上一首按钮，那么相应的app如果打开，那么里面的播放按钮的状态也随之改变
                LocalMusicFragment.getmPlayMusicButton().setImageResource(R.drawable.play_music);
            }
        }else if(NEXT_ACTION.equals(action)){//按下widget下一首按钮
            nextMusic();
            notifyChange(MUSIC_CHANGED);
            NotificationChange(NEXT_ACTION);
            if(LocalMusicFragment.getmPlayMusicButton()!=null) {//如果是在widget点击了下一首按钮，那么相应的app如果打开，那么里面的播放按钮的状态也随之改变
                LocalMusicFragment.getmPlayMusicButton().setImageResource(R.drawable.play_music);
            }
        }else if(STOP_ACTION.equals(action)){//前台服务点击关闭按钮
            ForegroundIsExist=false;
            mNotification=null;
            stopMusic();
            handler.removeCallbacks(runnable);
            if (mPlayMusicSeekBar != null)
                mPlayMusicSeekBar.setProgress(0);
            if(LocalMusicFragment.getmPlayMusicButton()!=null){//播放按钮设为停止按钮
                LocalMusicFragment.getmPlayMusicButton().setImageResource(R.drawable.pause_image);
            }
            if (mPlayMusicStartTimeTextView != null)//播放时间设置0
                mPlayMusicStartTimeTextView.setText("00:00");
            stopForeground(true);
            notifyChange(STOP_ACTION);
        }
        else if(WIDGET_LOVE_ACTION.equals(action)||NOTIFICATION_LOVE_ACTION.equals(action)){
            notifyChange(action);
            NotificationChange(action);
            boolean isLike=false;
            String song=MusicService.musicTitle.getText().toString();
            String artist=MusicService.s;
            String song_Image=MusicService.ss;
            String uri=MusicService.sss;
            String Lrc_uri=MusicService.ssss;
            isLike= LocalMusicFragment.lxrOperator.CheckIsDataAlreadyInDBorNot(song);
            Log.i(TAG, "onClick: "+isLike);
            if(LocalMusicFragment.sMusicList.size()==0){
                OnlyOneToast.makeText(getApplicationContext(),song);
                return;
            }
            if(isLike==true){
                LocalMusicFragment.lxrOperator.delete(song);
                LocalMusicFragment.mAddLikeMusicButton.setImageResource(R.drawable.like_image);
                OnlyOneToast.makeText(getApplicationContext(),"已取消喜欢");
            }else{
                MusicName lxr = new MusicName(song,artist,song_Image,uri,Lrc_uri);
                LocalMusicFragment. lxrOperator.add(lxr);
                LocalMusicFragment.mAddLikeMusicButton.setImageResource(R.drawable.like_image_selected);
                OnlyOneToast.makeText(getApplicationContext(),"已添加到我喜欢的音乐");
            }
            
            //修改数据库中的数据
            final Intent loveIntent = new Intent(MusicService.MY_BROCAST);
            sendBroadcast(loveIntent);
        }else if(UPDATE_ACTION.equals(action)){
            boolean isLike=false;
            if(LocalMusicFragment.lxrOperator!=null) {
                isLike = LocalMusicFragment.lxrOperator.CheckIsDataAlreadyInDBorNot(musicTitle.getText().toString());
                if (isLike == true) {
                    LocalMusicFragment.mAddLikeMusicButton.setImageResource(R.drawable.like_image_selected);
                } else {
                    LocalMusicFragment.mAddLikeMusicButton.setImageResource(R.drawable.like_image);
                }
            }
            notifyChange(UPDATE_ACTION);
            NotificationChange(UPDATE_ACTION);
            
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
                {  String widget_title = LocalMusicFragment.sMusicList.get(playingMusicIndex).getTitle();
                    mNotification.contentView.setTextViewText(R.id.widget_content, widget_title);//设置歌曲名
                    mNotification.contentView.setProgressBar(R.id.widget_progress, mediaPlayer.getDuration(), mediaPlayer.getCurrentPosition(), false);
                    
                    if (LocalMusicFragment.sMusicList.get(MusicService.playingMusicIndex).getImage() != null) {//如果音乐专辑图片存在
                        Bitmap bitmap = MusicIconLoader.getInstance().load(LocalMusicFragment.sMusicList.get(MusicService.playingMusicIndex).getImage());
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
                }else if(WIDGET_LOVE_ACTION.equals(what)||NOTIFICATION_LOVE_ACTION.equals(what)){
                    
                    boolean isLike=false;
                    if(LocalMusicFragment.lxrOperator!=null) {
                        isLike = LocalMusicFragment.lxrOperator.CheckIsDataAlreadyInDBorNot(musicTitle.getText().toString());
                        
                        if (isLike) {   //修改前台服务的的喜欢图标
                            mNotification.contentView.setImageViewResource(R.id.widget_love,R.drawable.like_image);
                        } else {
                            mNotification.contentView.setImageViewResource(R.id.widget_love,R.drawable.like_image_selected);
                        }
                    }
                }
                else if(UPDATE_ACTION.equals(what)){//更新喜欢的图标的状态
                    boolean isLike=false;
                    if(LocalMusicFragment.lxrOperator!=null) {
                        isLike = LocalMusicFragment.lxrOperator.CheckIsDataAlreadyInDBorNot(musicTitle.getText().toString());
                        if (isLike) {   //设置前台服务的的喜欢图标
                            mNotification.contentView.setImageViewResource(R.id.widget_love,R.drawable.like_image_selected);
                        } else {
                            mNotification.contentView.setImageViewResource(R.id.widget_love,R.drawable.like_image);
                            
                        }
                    }
                }
                Log.i(TAG, "run:11111111111 "+(notificationManager==null));
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
            intent1.putExtra("albumuri",  LocalMusicFragment.sMusicList.get(MusicService.playingMusicIndex).getImage());
            intent1.putExtra("MusicTitle",LocalMusicFragment.sMusicList.get(playingMusicIndex).getTitle());
            boolean isLike=false;
            if(LocalMusicFragment.lxrOperator!=null) {
                isLike = LocalMusicFragment.lxrOperator.CheckIsDataAlreadyInDBorNot(musicTitle.getText().toString());
            }
            intent1.putExtra("love",isLike);
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
        else if(WIDGET_LOVE_ACTION.equals(what)||NOTIFICATION_LOVE_ACTION.equals(what)){
            boolean isLike=false;
            if(LocalMusicFragment.lxrOperator!=null) {
                isLike = LocalMusicFragment.lxrOperator.CheckIsDataAlreadyInDBorNot(musicTitle.getText().toString());
                Intent loveIntet=new Intent(LOVE_ACTION);
                loveIntet.putExtra("love",!isLike);
                loveIntet.setComponent(new ComponentName(getApplicationContext(),appwidget_provider.class));
                sendBroadcast(loveIntet);
            }
        }
        else if(UPDATE_ACTION.equals(what)){
            boolean isLike=false;
            if(LocalMusicFragment.lxrOperator!=null) {
                isLike = LocalMusicFragment.lxrOperator.CheckIsDataAlreadyInDBorNot(musicTitle.getText().toString());
                Intent loveIntet=new Intent(LOVE_ACTION);
                loveIntet.putExtra("love",isLike);
                loveIntet.setComponent(new ComponentName(getApplicationContext(),appwidget_provider.class));
                sendBroadcast(loveIntet);
            }
        }
    }
    //初始前台服务
    private void initNotification(){
        String widget_title = LocalMusicFragment.sMusicList.get(playingMusicIndex).getTitle();
        widgetRemoteViews.setTextViewText(R.id.widget_content, widget_title);//设置歌曲名
        // widgetRemoteViews.setProgressBar(R.id.widget_progress, mediaPlayer.getDuration(), mediaPlayer.getCurrentPosition(), false);
        
        if (LocalMusicFragment.sMusicList.get(MusicService.playingMusicIndex).getImage() != null) {//如果音乐专辑图片存在
            Bitmap bitmap = MusicIconLoader.getInstance().load(LocalMusicFragment.sMusicList.get(MusicService.playingMusicIndex).getImage());
            widgetRemoteViews.setImageViewBitmap(R.id.widget_image, bitmap);
        } else {
            widgetRemoteViews.setImageViewResource(R.id.widget_image, R.drawable.image);
        }
        boolean isLike=false;
        if(LocalMusicFragment.lxrOperator!=null) {
            isLike = LocalMusicFragment.lxrOperator.CheckIsDataAlreadyInDBorNot(musicTitle.getText().toString());
            if (isLike == true) {
                widgetRemoteViews.setImageViewResource(R.id.widget_love,R.drawable.like_image_selected);
                
            } else {
                widgetRemoteViews.setImageViewResource(R.id.widget_love,R.drawable.like_image);
            }
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
        
        Intent loveIntent=new Intent(NOTIFICATION_LOVE_ACTION);
        PendingIntent lovePIntent=PendingIntent.getBroadcast(ForegroundContext,0,loveIntent,0);
        widgetRemoteViews.setOnClickPendingIntent(R.id.widget_love,lovePIntent);
        
        
        final Intent nowPlayingIntent=new Intent(Intent.ACTION_MAIN);
        nowPlayingIntent.setAction(Intent.ACTION_MAIN);
        nowPlayingIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        nowPlayingIntent.setComponent(new ComponentName("com.of.music","com.of.music.activity.MainActivity"));
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
        if(!isSatisfyingPlayConditions()){
            OnlyOneToast.makeText(getApplication(),"暂无歌曲");
            return super.onStartCommand(intent, flags, startId);
        }
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
        mediaPlayer.release();
        unregisterReceiver(mIntentReceiver);
        stopForeground(true);
        super.onDestroy();
    }
}
