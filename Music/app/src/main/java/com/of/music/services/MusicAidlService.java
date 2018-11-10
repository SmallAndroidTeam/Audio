package com.of.music.services;

import android.annotation.SuppressLint;
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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Visualizer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;

import com.of.music.R;
import com.of.music.Application.App;
import com.of.music.SharedPreferencesToSaveEQSeekBar.SharedPreferencesHelper;
import com.of.music.Toast.OnlyOneToast;
import com.of.music.db.DownloadMusicOperater;
import com.of.music.db.MusicOperator;
import com.of.music.defineViewd.VisualizerView;
import com.of.music.fragment.LocalMusicFragment;
import com.of.music.fragment.fragmentList.DownloadListFragment;
import com.of.music.fragment.fragmentList.FragmentAlter;
import com.of.music.fragment.fragmentList.RecentlyListFragment;
import com.of.music.info.FavouriteMusicListInfo;
import com.of.music.info.MusicName;
import com.of.music.info.RecentlyMusicListInfo;
import com.of.music.songListInformation.Music;
import com.of.music.songListInformation.MusicController;
import com.of.music.songListInformation.MusicIconLoader;
import com.of.music.songListInformation.MusicPlayProgressListener;
import com.of.music.songListInformation.MusicUtils;
import com.of.music.ui.LrcView;
import com.of.music.widget.appwidget_provider;

import org.litepal.LitePal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MusicAidlService extends Service {
    public static MediaPlayer mediaPlayer;
    public static int playingMusicIndex=-1;//正在播放音乐的下标
    private static NotificationManager notificationManager;
    private RemoteViews widgetRemoteViews;
    public static enum playAction{start,pause,next,prev,widet}
    private String TAG="Music";
    private  static Runnable runnable;
    private Timer timer;
    public static boolean isPlay=false;
    public  String musicArtist;
    public  String musicIcon;
    public  String musicUri;
    public   String musicLrcpath;
    public static  final String SEND_PROGRESS="com.of.music.aidl.progress";
    public static final String TOGGLEPAUSE_ACTION = "com.of.music.aidl.togglepause";
    public static final String PREVIOUS_ACTION = "com.of.music.aidl.previous";
    public static final String NEXT_ACTION = "com.of.music.aidl.next";
    public static final String STOP_ACTION = "com.of.music.aidl.STOP_ACTION";
    public static final String META_CHANGED = "com.of.music.aidl.metachanged";
    public static final String MUSIC_CHANGED = "com.of.music.aidl.change_music";
    public static final String MY_BROCAST = "com.of.music.aidl.MY_BROADCAST";
    public static final  String WIDGET_LOVE_ACTION="com.of.music.aidl.WIDGET_LOVE_BROADCAST";
    public static final  String LOVE_ACTION="com.of.music.aidl.LOVE_BROADCAST";
    public static final  String NOTIFICATION_LOVE_ACTION="com.of.music.aidl.NOTIFICATION_LOVE_BROADCAST";
    public static final String PLAYVIEW_LOVE_ACTION="com.of.music.aidl.ADD_LOVE_ACTION";
    public static final String RECENTLY_ADDACTION = "com.of.music.aidl.recentlyfragment";
    public static final String UPDATE_ACTION="com.of.music.aidl.update";
    public final static String[] playMode=new String[]{"list_mode","circulate_mode","singlecycle_mode","randomplay_mode"};//播放模式
    public  String currentPlayMode=playMode[3];//当前的音乐播放模式
    private static final int NotificationId=1001;
    private static  Notification mNotification;
    private final static int UpdateForeground=0x1;
    private static RemoteViews remoteViews;
    private static   AppWidgetManager appWidgetManager;
    private static  ComponentName componentName;
    private DownloadMusicOperater downloadMusicOperater;
    private static boolean ForegroundIsExist=false;//判断前台服务是否存在
    private boolean isInitComplete=false;//判断是否第一次初始化服务完成
    public static  ArrayList<Music> musicList=new ArrayList<>();//歌曲列表
    // 定义系统的频谱
    public Visualizer mVisualizer;
    // 定义系统的均衡器
    public Equalizer mEqualizer;
    public VisualizerView mVisualizerView;
    public static final float VISUALIZER_HEIGHT_DIP = 50f;
    private SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(App.sContext,"EqualizerSeekbar");
    public int progress_pre=0;
    private final static int INIT_MUSCIC_SERVICE=0;//初始化music服务
    private  boolean isExecutionInit=false;//判断是否正在执行初始化函数
    private String thirdApplicationPackageName;//调用此服务的第三方应用的包名
    private MusicOperator lxrOperator;
    @SuppressLint("HandlerLeak")
    private final Handler handler=new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case INIT_MUSCIC_SERVICE://初始化服务
                    try{
                        initMusic();
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    break;
                default:
                    break;
            }

        }

    };
    private final BroadcastReceiver mIntentReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

       String processName=getProcessName();
            Log.i("hz111111", "onReceive: "+processName+"//package:"+getPackageName());
       //判断进程名
       if(!TextUtils.isEmpty(processName)&&processName.contentEquals(getPackageName()+":remote"))
       {
           Log.i("hz111111", "onReceive: ");
           handleCommandIntent(intent);
       }

       }
    };

    //获取进程名
    public  String getProcessName() {
        try {
            File file = new File("/proc/" + android.os.Process.myPid() + "/" + "cmdline");
            BufferedReader mBufferedReader = new BufferedReader(new FileReader(file));
            String processName = mBufferedReader.readLine().trim();
            mBufferedReader.close();
            return processName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


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
        mediaPlayer=new MediaPlayer();
        isInitComplete=false;//第一次初始化服务未完成
        widgetRemoteViews =new RemoteViews(this.getPackageName(),R.layout.notification);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                autoPlayMusic();
            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.i("hz111", "onError: ");
                nextMusic();
                return false;
            }
        });
        runnable = new Runnable() {
            @Override
            public void run() {
                //设置widget里面的进度条
                notifyChange(SEND_PROGRESS);
                //执行进度条监听
                int n=remoteCallbackList.beginBroadcast();//获取监听事件的个数
                for(int i=0;i<n;i++){
                    MusicPlayProgressListener musicPlayProgressListener=remoteCallbackList.getBroadcastItem(i);
                    if(musicPlayProgressListener!=null){
                        try {
                            if(mediaPlayer!=null) {
                                //if(mediaPlayer.isPlaying()){
                                if(isInitComplete) {    //如果第一次初始化服务完成（如果初始化没完成，调用getCurrentPosition会出错)
                                   //没有在执行initMusic函数
                                    if(!isExecutionInit){
                                        musicPlayProgressListener.musicProgressListener(mediaPlayer.getCurrentPosition());//向客户端发送数据
                                    }

                                }else{
                                    musicPlayProgressListener.musicProgressListener(0);//向客户端发送数据
                                }

                            }
                            else{
                                musicPlayProgressListener.musicProgressListener(0);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                remoteCallbackList.finishBroadcast();
                handler.postDelayed(this, 500);
            }
        };
        lxrOperator=new MusicOperator(getApplicationContext());
        super.onCreate();
    }

    //设置音乐列表
    public static void setMusicList(ArrayList<Music> musicList) {
        MusicAidlService.musicList.clear();
        MusicAidlService.musicList.addAll(musicList);
        Log.i("hz11111", "setMusicList:   "+musicList.size());
    }

    public    boolean  initMusic(){
        synchronized (this) {
            if (isSatisfyingPlayConditions()) {
                if(mediaPlayer==null){
                    mediaPlayer=new MediaPlayer();
                }

                try {
                    isExecutionInit=true;//用于判断是否正在执行此函数
                    mediaPlayer.reset();
                    Log.i("download11", "initMusic: "+musicList.get(playingMusicIndex).getUri()+"//"+
                            musicList.size());
                    String musicAddress=musicList.get(playingMusicIndex).getUri();
                    while(!musicAddress.toLowerCase().startsWith("http")&&!new File(musicAddress).exists()){
                        Log.i("hz11111", "initMusic: 删除");
                        musicList.remove(playingMusicIndex);
                        if(musicList.size()==0){
                            playingMusicIndex=-1;
                            stopMusic();
                            return false;
                        }
                        Log.i("download11", "initMusic: "+musicList.size());
                        playingMusicIndex=playingMusicIndex>=musicList.size()-1?0:playingMusicIndex+1;
                        musicAddress=musicList.get(playingMusicIndex).getUri();
                    }
                    Log.i("hz11111", "initMusic: "+musicList.get(playingMusicIndex).getUri());
                    mediaPlayer.setDataSource(musicList.get(playingMusicIndex).getUri());
                    mediaPlayer.prepare();
                    musicArtist=musicList.get(playingMusicIndex).getArtist();                        //获取歌曲信息
                    musicIcon=musicList.get(playingMusicIndex).getImage();
                    musicUri=musicList.get(playingMusicIndex).getUri();
                    musicLrcpath=musicList.get(playingMusicIndex).getLrcpath();
                    Intent UpdateIntent=new Intent(UPDATE_ACTION);
                    handleCommandIntent(UpdateIntent);

                    //        每次播放歌曲都会调用initMusic()，添加到最近播放列表里
                    RecentlyMusicListInfo recentlyMusicListInfo=new RecentlyMusicListInfo();
                    recentlyMusicListInfo.setImage(musicIcon);
                    recentlyMusicListInfo.setArtist(musicArtist);
                    recentlyMusicListInfo.setLrc_uri(musicLrcpath);
                    recentlyMusicListInfo.setUri(musicUri);
                    recentlyMusicListInfo.setName(musicList.get(playingMusicIndex).getTitle());
                    recentlyMusicListInfo.setPlayTime(String.valueOf(System.currentTimeMillis()));
                    //        过滤播放相同的歌曲，将之前那首歌的播放记录更新为最新的记录
                    Cursor cursor=LitePal.findBySQL("select count(*) from RecentlyMusicListInfo where name = ?", recentlyMusicListInfo.getName());
                    Log.i("audio11", "initMusic: ");
                    if(cursor.moveToFirst()){
                        Log.i("audio11", "initMusic:1 "+cursor.getInt(0));
                        if(cursor.getInt(0)>0){
                            recentlyMusicListInfo.updateAll("name = ?",recentlyMusicListInfo.getName());
                        }else{
                            recentlyMusicListInfo.save();
                        }
                    }else{
                        Log.i("audio11", "initMusic:2 ");
                        recentlyMusicListInfo.save();
                    }


                    //        RecentlyListFragment recentlyListFragment=new RecentlyListFragment();
                    //      recentlyListFragment.AlterAdapter();
                    if(FragmentAlter.getRecentlyFragment()!=null)
                    {
                        ((RecentlyListFragment)FragmentAlter.getRecentlyFragment()).AlterAdapter();
                    }

                    if(FragmentAlter.getDownloadFragmenet()!=null){
                        DownloadListFragment.downloadMusicOperater.alter(musicList.get(playingMusicIndex).getTitle(),String.valueOf(System.currentTimeMillis()));
                        Log.i("altertime",String.valueOf(System.currentTimeMillis())+"//"+"b");
                        ((DownloadListFragment)FragmentAlter.getDownloadFragmenet()).DownloadAlter();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    isInitComplete=true;//第一次初始化服务完成
                    isExecutionInit=false;//用于判断是否正在执行此函数
                    return false;
                }
                isInitComplete=true;//第一次初始化服务完成
                isExecutionInit=false;//用于判断是否正在执行此函数
            } else {
                isInitComplete=true;//第一次初始化服务完成
                isExecutionInit=false;//用于判断是否正在执行此函数
                Log.i("hz11111", "initMusic: ");
                OnlyOneToast.makeText(MusicAidlService.this, "暂无歌曲");
            }

        }
        isExecutionInit=false;//用于判断是否正在执行此函数
        isInitComplete=true;//第一次初始化服务完成
        return true;
    }

    //判断是否满足播放条件
    private boolean isSatisfyingPlayConditions(){
        int size=musicList.size();
        Log.i("hz111", "isSatisfyingPlayConditions: "+musicList.size()+"//"+playingMusicIndex);
        if(playingMusicIndex<0||size==0||playingMusicIndex>size-1&&mediaPlayer!=null){
            return  false;
        }
        return  true;
    }

    /**
     * 返回当前播放的进度
     * @return
     */
    public   int getMusicCurrentPosition(){
        if(isSatisfyingPlayConditions()&&mediaPlayer!=null){
            return mediaPlayer.getCurrentPosition();
        }else
        {
            return 0;
        }
    }

    /**
     *获取当前播放音乐的总的播放时间
     * @return
     */
    public int getMusicDuration(){
        if(isSatisfyingPlayConditions()&&mediaPlayer!=null){
            return mediaPlayer.getDuration();
        }else
        {
            return 0;
        }
    }

    /**
     *
     * @return (当前正在播放返回true,否则返回false）
     */
    public boolean isPlaying(){
        if(isSatisfyingPlayConditions()&&mediaPlayer!=null){
            return  mediaPlayer.isPlaying();
        }else{
            return  false;
        }
    }



    public void startMusic(){
        synchronized (this){
            if(!isSatisfyingPlayConditions()){
                OnlyOneToast.makeText(MusicAidlService.this,"暂无歌曲");
                return;
            }

            if(mediaPlayer.isPlaying()){
                mediaPlayer.pause();
              //  handler.removeCallbacks(runnable);
            }
            else{
                //重点
                mediaPlayer.start();
                handler.post(runnable);
                setForeground();//建立前台服务
            }
        }

    }
    public void autoPlayMusic(){
        synchronized (this) {
            if (!isSatisfyingPlayConditions()) {
                Log.i("hz11111", "autoPlayMusic: ");
                OnlyOneToast.makeText(getApplicationContext(), "暂无歌曲");
                return;
            }
            int i;
            for (i = 0; i <playMode.length; i++) {
                if (currentPlayMode.contentEquals(playMode[i])) {
                    break;
                }
            }
            if (i >= playMode.length) {
                i = playMode.length - 1;
            }
            //只改变播放音乐的下标
            switch (i) {
                case 0://顺序播放
                    if (playingMusicIndex == (musicList.size() - 1)) {
                        playingMusicIndex = 0;
                        if(!initMusic()){
                            OnlyOneToast.makeText(getApplicationContext(),"当前无歌曲");
                            return;
                        }
                        mediaPlayer.pause();
                        notifyChange(MUSIC_CHANGED);
                        NotificationChange(NEXT_ACTION);
                     //   handler.removeCallbacks(runnable);
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
                    int MusicIndex =new Random().nextInt(musicList.size());
                    while (MusicIndex == playingMusicIndex&&musicList.size()>1) {
                        MusicIndex = new Random().nextInt(musicList.size());
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

            try {
                mediaPlayer.stop();
                mediaPlayer.prepare();
                mediaPlayer.seekTo(0);
               // handler.removeCallbacks(runnable);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void nextMusic(){
        synchronized (this) {
            if (!isSatisfyingPlayConditions()) {
                Log.i("hz11111", "nextMusic: ");

                OnlyOneToast.makeText(MusicAidlService.this, "暂无歌曲");
                return;
            }
            int i;
            for (i = 0; i < playMode.length; i++) {
                if (currentPlayMode.contentEquals(playMode[i])) {
                    break;
                }
            }
            if (i >= playMode.length) {
                i = playMode.length - 1;
            }
            //只改变播放音乐的下标
            switch (i) {
                case 0://顺序播放
                case 1://列表循环
                case 2://单曲循环
                    playingMusicIndex = (playingMusicIndex ==musicList.size() - 1) ? 0 : (playingMusicIndex + 1);
                    break;
                case 3://随机播放
                    int MusicIndex =new Random().nextInt(musicList.size());
                    while (MusicIndex == playingMusicIndex&&musicList.size()>1) {
                        MusicIndex = new Random().nextInt(musicList.size());
                    }
                    playingMusicIndex = MusicIndex;
                    break;
                default:
                    break;
            }
            Log.i(TAG, "NextMusic: " + playingMusicIndex);
            if(!initMusic()){
                Log.i("hz11111", "nextMusic: 222");
                //  OnlyOneToast.makeText(getApplicationContext(),"当前无歌曲");
                return;
            }
            mediaPlayer.start();
           // handler.post(runnable);
            setForeground();//建立前台服务
        }

    }


    public void prevMusic(){
        synchronized (this) {
            if (!isSatisfyingPlayConditions()) {
                Log.i("hz11111", "prevMusic: ");
                OnlyOneToast.makeText(MusicAidlService.this, "暂无歌曲");
                return;
            }
            int i;
            for (i = 0; i <playMode.length; i++) {
                if (currentPlayMode.contentEquals(playMode[i])) {
                    break;
                }
            }
            if (i >= playMode.length) {
                i = playMode.length - 1;
            }
            //只改变播放音乐的下标
            switch (i) {
                case 0://顺序播放
                case 1://列表循环
                case 2://单曲循环
                    playingMusicIndex = (playingMusicIndex == 0) ? (musicList.size() - 1) : (playingMusicIndex - 1);
                    break;
                case 3://随机播放
                    int MusicIndex =new Random().nextInt(musicList.size());
                    while (MusicIndex == playingMusicIndex&&musicList.size()>1) {
                        MusicIndex = new Random().nextInt(musicList.size());
                    }
                    playingMusicIndex = MusicIndex;
                    break;
                default:
                    break;
            }
            if(!initMusic()){
                //OnlyOneToast.makeText(getApplicationContext(),"当前无歌曲");
                return;
            }
            mediaPlayer.start();
           // handler.post(runnable);
            setForeground();//建立前台服务
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

    /**
     * 绑定服务
     * @param intent
     * @return
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i("hz1111", "onBind: 00");
        if(intent!=null&&intent.getAction().contentEquals("com.android.oflim.action")){//只有发出特定消息才会绑定服务
            Log.i("hz1111", "onBind: 11");
            App.sContext=getApplicationContext();
            MusicUtils.initMusicList();
            return stub;
        }
        Log.i("hz1111", "onBind: 22");
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i("hz1111111", "onUnbind: ");
         stopSelf();//如果解除绑定则摧毁服务
        return super.onUnbind(intent);
    }

    /**
     * 判断播放下标是否满足播放条件
     * @param position
     * @return
     */
    private boolean isSatisfyPlayCondition(int position){
        if(position<0||position>musicList.size()-1){
            return false;
        }else{
            return true;
        }
    }

    //创建前台服务
    public void setForeground(){
        if(!ForegroundIsExist)
        { new Thread(new Runnable() {
            @Override
            public void run() {
                ForegroundIsExist=true;
                startForeground(NotificationId,getmNotification());

            }
        }).start();

        }
    }

    //RemoteCallbackList是专门用于删除跨进程listener的接口，它是一个泛型，支持管理任意的AIDL接口
    private RemoteCallbackList<MusicPlayProgressListener> remoteCallbackList=new RemoteCallbackList<>();
    private final MusicController.Stub stub=new MusicController.Stub() {
        @Override
        public List<Music> getLocalMusicList() throws RemoteException {//获取本地的音乐列表
            return MusicUtils.sMusicList;
        }

        @Override
        public List<Music> getCurrentMusicList() throws RemoteException {//获取当期的播放列表
            return  musicList;
        }

        @Override
        public void setCurrentMusicList(List<Music> list) throws RemoteException {//设置当前的播放列表
            musicList.clear();
            musicList.addAll(list);
        }

        @Override
        public boolean setCurrentPlayIndex(int position) throws RemoteException {//设置当前的播放下标
            if(!isSatisfyPlayCondition(position)){
                return false;
            }else{
                playingMusicIndex=position;
                return true;
            }
        }

        @Override
        public int getCurrentPlayIndex() throws RemoteException {//获取当前的播放下标
            return playingMusicIndex;
        }

        @Override
        public List<Music> getUsbMusicList() throws RemoteException {//获取U盘音乐列表(目前没实现)
            return null;
        }

        @Override
        public void playCurrentSelectedMusic(int position) throws RemoteException {//播放选中的音乐下标的歌曲
            if(isSatisfyPlayCondition(position)){
                playingMusicIndex=position;
                if(initMusic()){//如果初始化成功
                    if(mediaPlayer!=null){
                        mediaPlayer.start();
                        handler.post(runnable);
                        setForeground();//建立前台服务
                        notifyChange(META_CHANGED);//更新widget
                        NotificationChange(TOGGLEPAUSE_ACTION);//更新前台服务
                    }
                }
            }
        }

        @Override
        public Music getPlayMusicInfo() throws RemoteException {//获取当前播放的音乐的信息
            if(isSatisfyPlayCondition(playingMusicIndex)){
                return musicList.get(playingMusicIndex);
            }
            return null;
        }

        @Override
        public boolean musicIsPlaying() throws RemoteException {//判断音乐是否正在播放
            if(mediaPlayer!=null){
                return mediaPlayer.isPlaying();
            }
            return false;
        }

        @Override
        public void startCurrentPlayIndexMusic() throws RemoteException {//播放当前音乐下标的歌曲
            if(mediaPlayer!=null&&!mediaPlayer.isPlaying()){
                mediaPlayer.start();
                handler.post(runnable);
                setForeground();//建立前台服务
                notifyChange(META_CHANGED);//更新widget
                NotificationChange(TOGGLEPAUSE_ACTION);//更新前台服务
            }
        }
        @Override
        public void pauseCurrentPlayIndexMusic() throws RemoteException {//暂停当前的音乐下标的歌曲
            if(mediaPlayer!=null&&mediaPlayer.isPlaying()){
                mediaPlayer.pause();
                notifyChange(META_CHANGED);//更新widget
                NotificationChange(TOGGLEPAUSE_ACTION);//更新前台服务
               // handler.removeCallbacks(runnable);
            }
        }

        @Override
        public void stopCurrentPlayIndexMusic() throws RemoteException {//停止当前音乐下标的歌曲
            if(mediaPlayer!=null){
                mediaPlayer.stop();

                ForegroundIsExist=false;
                mNotification=null;
                stopForeground(true);//关闭前台服务
                notifyChange(STOP_ACTION);//更新widget

                //handler.removeCallbacks(runnable);
            }
        }

        @Override
        public void of_nextMusic() throws RemoteException {//下一首歌
            nextMusic();
            notifyChange(MUSIC_CHANGED);
            NotificationChange(NEXT_ACTION);
        }

        @Override
        public void of_prevMusic() throws RemoteException {//上一首歌
            prevMusic();
            notifyChange(MUSIC_CHANGED);
            NotificationChange(PREVIOUS_ACTION);
        }

        @Override
        public int of_getDuration() throws RemoteException {//当前播放歌曲的总的时长
            try{
                if(mediaPlayer!=null){
                    if(isInitComplete){//如果第一次初始化服务完成（如果初始化没完成，调用getDuration方法会报错)
                        return mediaPlayer.getDuration();
                    }else{
                        return 0;
                    }
                }else{
                    return 0;
                }
            }catch (Exception e){
                e.printStackTrace();
                return 0;
            }

        }

        @Override
        public int of_getCurrentPosition() throws RemoteException {//当前播放歌曲的进度
            try{
                if(mediaPlayer!=null){
                    if(isInitComplete){//如果第一次初始化服务完成（如果初始化没完成，getCurrentPosition)
                        return mediaPlayer.getCurrentPosition();
                    }else{
                        return 0;
                    }

                }else{
                    return 0;
                }
            }catch (Exception e){
                e.printStackTrace();
                return 0;
            }

        }

        @Override
        public void of_setCurrent(int pos) throws RemoteException {//设置当前播放的歌曲的进度
            try{
                if(mediaPlayer!=null){
                    mediaPlayer.seekTo(pos);
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }

        @Override
        public void initMusicService() throws RemoteException {
            handler.sendEmptyMessage(INIT_MUSCIC_SERVICE);
        }

        @Override
        public boolean setPlayMode(int type) throws RemoteException {//设置播放模式

            if(type<0||type>playMode.length){
                return false;
            }else{
               currentPlayMode=playMode[type];
                return true;
            }
        }

        @Override
        public int getCurrentMusicListSize() throws RemoteException {//获取当前的音乐列表大小
           return  musicList.size();
        }

        @Override
        public void setMusicPlayProgressListener(MusicPlayProgressListener musicPlayProgressListener) throws RemoteException {
            //设置播放进度监听
            remoteCallbackList.register(musicPlayProgressListener);
        }

        @Override
        public void cancelMusicPlayProgressListener(MusicPlayProgressListener musicPlayProgressListener) throws RemoteException {
            //取消播放进度监听
            remoteCallbackList.unregister(musicPlayProgressListener);
        }

        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {//只有指定包名的应用才能调用服务
            String packageName=null;
            int callingPid=getCallingPid();
            int callingUid=getCallingUid();
            String[] packagesForUid=MusicAidlService.this.getPackageManager().getPackagesForUid(callingUid);
            if(packagesForUid!=null&&packagesForUid.length>0){
                packageName=packagesForUid[0];
            }

               if(TextUtils.isEmpty(packageName)||!"of.media.hz".contentEquals(packageName)){
                return  false;
               }


            return super.onTransact(code, data, reply, flags);
        }
    };

    private void handleCommandIntent(Intent intent) {

        String action=intent.getAction();
        //可能已经在桌面建立了widget，一启动后没初始化歌单列表
        if(musicList.size()==0)
        {
            App.sContext=getApplicationContext();
            MusicUtils.initMusicList();
            if(MusicUtils.sMusicList.size()>0)
            {
                setMusicList(MusicUtils.sMusicList);
                playingMusicIndex=0;
                initMusic();
            }
        }

        if(playingMusicIndex==-1)
        {
            Log.i("hz11111", "handleCommandIntent: ");
            OnlyOneToast.makeText(getApplicationContext(),"暂无歌曲");
            return;
        }
        //widget创建或者刷新第一步都要执行这动作
        if(SEND_PROGRESS.equals(action)){//发送现在音乐的一些信息
            notifyChange(MUSIC_CHANGED);
            handler.post(runnable);
        }else if(TOGGLEPAUSE_ACTION.equals(action)){//按下widget中间的播放按钮

            if(mediaPlayer.isPlaying()){
              //  handler.removeCallbacks(runnable);
            }else{
                handler.post(runnable);
            }
            startMusic();
            notifyChange(META_CHANGED);
            NotificationChange(TOGGLEPAUSE_ACTION);
        }else if(PREVIOUS_ACTION.equals(action)){//按下widget上一首按钮
            prevMusic();
            notifyChange(MUSIC_CHANGED);
            NotificationChange(PREVIOUS_ACTION);
        }else if(NEXT_ACTION.equals(action)){//按下widget下一首按钮
            nextMusic();
            notifyChange(MUSIC_CHANGED);
            NotificationChange(NEXT_ACTION);
        }else if(STOP_ACTION.equals(action)){//前台服务点击关闭按钮
            ForegroundIsExist=false;
            mNotification=null;
            stopMusic();
           // handler.removeCallbacks(runnable);
            stopForeground(true);
            notifyChange(STOP_ACTION);
        }
        else if(WIDGET_LOVE_ACTION.equals(action)||NOTIFICATION_LOVE_ACTION.equals(action)){
            notifyChange(action);
            NotificationChange(action);
            boolean isLike=false;
            String song= musicList.get(playingMusicIndex).getTitle();
            String artist=musicList.get(playingMusicIndex).getArtist();
            String song_Image=musicList.get(playingMusicIndex).getImage();
            String uri=musicList.get(playingMusicIndex).getUri();
            String Lrc_uri=musicList.get(playingMusicIndex).getLrcpath();
            isLike= lxrOperator.CheckIsDataAlreadyInDBorNot(song);
            Log.i(TAG, "onClick: "+isLike);
            if(musicList.size()==0){
                OnlyOneToast.makeText(getApplicationContext(),song);
                return;
            }
            if(isLike==true){
               lxrOperator.delete(song);
                LitePal.deleteAll(FavouriteMusicListInfo.class,"name=?",song);
                OnlyOneToast.makeText(getApplicationContext(),"已取消喜欢");
            }else{
                MusicName lxr = new MusicName(song,artist,song_Image,uri,Lrc_uri);
                lxrOperator.add(lxr);
                //LitePal框架存储到数据库
                FavouriteMusicListInfo favouriteMusicListInfo1 = new FavouriteMusicListInfo();
                favouriteMusicListInfo1.setName(song);
                favouriteMusicListInfo1.setArtist(artist);
                favouriteMusicListInfo1.setImage(song_Image);
                favouriteMusicListInfo1.setUri(uri);
                favouriteMusicListInfo1.setLrc_uri(Lrc_uri);
                favouriteMusicListInfo1.save();
                OnlyOneToast.makeText(getApplicationContext(),"已添加到我喜欢的音乐");
            }

            //修改数据库中的数据
            final Intent loveIntent = new Intent(MusicAidlService.MY_BROCAST);
            sendBroadcast(loveIntent);
        }else if(UPDATE_ACTION.equals(action)){
            boolean isLike=false;
            if(lxrOperator!=null) {
                isLike =lxrOperator.CheckIsDataAlreadyInDBorNot(musicList.get(playingMusicIndex).getTitle().toString());
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
                {  String widget_title = musicList.get(playingMusicIndex).getTitle();
                    mNotification.contentView.setTextViewText(R.id.widget_content, widget_title);//设置歌曲名
                    mNotification.contentView.setProgressBar(R.id.widget_progress, mediaPlayer.getDuration(), mediaPlayer.getCurrentPosition(), false);

                    if (musicList.get(playingMusicIndex).getImage() != null) {//如果音乐专辑图片存在
                        Bitmap bitmap = MusicIconLoader.getInstance().load(musicList.get(playingMusicIndex).getImage());
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
                    if(lxrOperator!=null) {
                        isLike = lxrOperator.CheckIsDataAlreadyInDBorNot(musicList.get(playingMusicIndex).getTitle().toString());

                        if (isLike) {   //修改前台服务的的喜欢图标
                            mNotification.contentView.setImageViewResource(R.id.widget_love,R.drawable.like_image);
                        } else {
                            mNotification.contentView.setImageViewResource(R.id.widget_love,R.drawable.like_image_selected);
                        }
                    }
                }
                else if(UPDATE_ACTION.equals(what)){//更新喜欢的图标的状态
                    boolean isLike=false;
                    if(lxrOperator!=null) {
                        isLike = lxrOperator.CheckIsDataAlreadyInDBorNot(musicList.get(playingMusicIndex).getTitle().toString());
                        if (isLike) {   //设置前台服务的的喜欢图标
                            mNotification.contentView.setImageViewResource(R.id.widget_love,R.drawable.like_image_selected);
                        } else {
                            mNotification.contentView.setImageViewResource(R.id.widget_love,R.drawable.like_image);

                        }
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
            intent1.putExtra("albumuri", musicList.get(playingMusicIndex).getImage());
            intent1.putExtra("MusicTitle",musicList.get(playingMusicIndex).getTitle());
            boolean isLike=false;
            if(lxrOperator!=null) {
                isLike = lxrOperator.CheckIsDataAlreadyInDBorNot(musicList.get(playingMusicIndex).getTitle().toString());
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
            if(lxrOperator!=null) {
                isLike = lxrOperator.CheckIsDataAlreadyInDBorNot(musicList.get(playingMusicIndex).getTitle().toString());
                Intent loveIntet=new Intent(LOVE_ACTION);
                loveIntet.putExtra("love",!isLike);
                loveIntet.setComponent(new ComponentName(getApplicationContext(),appwidget_provider.class));
                sendBroadcast(loveIntet);
            }
        }
        else if(UPDATE_ACTION.equals(what)){
            boolean isLike=false;
            if(lxrOperator!=null) {
                isLike = lxrOperator.CheckIsDataAlreadyInDBorNot(musicList.get(playingMusicIndex).getTitle().toString());
                Intent loveIntet=new Intent(LOVE_ACTION);
                loveIntet.putExtra("love",isLike);
                loveIntet.setComponent(new ComponentName(getApplicationContext(),appwidget_provider.class));
                sendBroadcast(loveIntet);
            }
        }
    }
    //初始前台服务
    private void initNotification(){
        String widget_title =musicList.get(playingMusicIndex).getTitle();
        widgetRemoteViews.setTextViewText(R.id.widget_content, widget_title);//设置歌曲名
        // widgetRemoteViews.setProgressBar(R.id.widget_progress, mediaPlayer.getDuration(), mediaPlayer.getCurrentPosition(), false);

        if (musicList.get(playingMusicIndex).getImage() != null) {//如果音乐专辑图片存在
            Bitmap bitmap = MusicIconLoader.getInstance().load(musicList.get(playingMusicIndex).getImage());
            widgetRemoteViews.setImageViewBitmap(R.id.widget_image, bitmap);
        } else {
            widgetRemoteViews.setImageViewResource(R.id.widget_image, R.drawable.image);
        }
        boolean isLike=false;
        if(lxrOperator!=null) {
            isLike = lxrOperator.CheckIsDataAlreadyInDBorNot(musicList.get(playingMusicIndex).getTitle().toString());
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
                       .setWhen(System.currentTimeMillis()).setAutoCancel(false)
                        .build();
                //.setWhen(System.currentTimeMillis()).setShowWhen(true)
            }
            else{
                NotificationCompat.Builder builder=new NotificationCompat.Builder(ForegroundContext).setContent(widgetRemoteViews).setAutoCancel(false)
                        .setSmallIcon(R.drawable.ic_notification).setWhen(System.currentTimeMillis());
                mNotification=builder.build();
            }

        }else{
            mNotification.contentView=remoteViews;
        }
        return mNotification;
    }
    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {//当关闭服务时
        Log.i("hz1111111", "onDestroy: ");
        try{
            ForegroundIsExist=false;
            mNotification=null;
            stopMusic();
            notifyChange(STOP_ACTION);
            mediaPlayer.release();
            unregisterReceiver(mIntentReceiver);
            stopForeground(true);
        }catch (Exception e){
            e.printStackTrace();
        }
        super.onDestroy();
    }
}
