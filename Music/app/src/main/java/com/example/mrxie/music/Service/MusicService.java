package com.example.mrxie.music.Service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.mrxie.music.R;
import com.example.mrxie.music.SongListInformation.Music;
import com.example.mrxie.music.fragment.localMusicFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class MusicService extends Service {
    public  static MediaPlayer mediaPlayer=new MediaPlayer();
    public static ArrayList<Music> sMusicList = new ArrayList<Music>(); // 存放歌曲列表
    public static int playingMusicIndex=0;//正在播放音乐的下标
    public static enum playAction{start,pause,next,prev}
    public static TextView musicTitle;
    private String TAG="Music";
    public static TextView mPlayMusicStartTimeTextView;
    public static  TextView mPlayMusicStopTimeTextView;
    public static  SeekBar mPlayMusicSeekBar;
    private static Handler handler=new Handler();
    private  static Runnable runnable;
    public static  ImageButton mPlayMusicButton;
    @Override
    public void onCreate() {
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                autoPlayMusic();
            }
        });
        super.onCreate();
    }
    private static String changeDigitsToTwoDigits(int digit){//将一个数变为二位数
     if(digit<10){
         return "0"+digit;
     }else
     {
         return ""+digit;
     }
    }
    public  static void  initMusic(){
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(sMusicList.get(playingMusicIndex).getUri());
            mediaPlayer.prepare();
            musicTitle.setText(sMusicList.get(playingMusicIndex).getTitle());
            mPlayMusicSeekBar.setMax(mediaPlayer.getDuration());
            mPlayMusicSeekBar.setProgress(mediaPlayer.getCurrentPosition());
            mPlayMusicStartTimeTextView.setText(changeDigitsToTwoDigits(mediaPlayer.getCurrentPosition()/1000/60)+":"+changeDigitsToTwoDigits(mediaPlayer.getCurrentPosition()/1000%60));
            mPlayMusicStopTimeTextView.setText(changeDigitsToTwoDigits((mediaPlayer.getDuration())/1000/60)+":"+changeDigitsToTwoDigits(mediaPlayer.getDuration()/1000%60));
        } catch (IOException e) {
            e.printStackTrace();
        }
        runnable=new Runnable() {
            @Override
            public void run() {
               mPlayMusicSeekBar.setProgress(mediaPlayer.getCurrentPosition());
                mPlayMusicStartTimeTextView.setText(changeDigitsToTwoDigits(mediaPlayer.getCurrentPosition()/1000/60)+":"+changeDigitsToTwoDigits(mediaPlayer.getCurrentPosition()/1000%60));
                handler.postDelayed(this,500);

            }
        };
    }
    public void startMusic(){
        if(mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            handler.removeCallbacks(runnable);
        }
        else{

            mediaPlayer.start();
            handler.post(runnable);
        }
    }
    public void autoPlayMusic(){
        int i;
        for(i=0; i< localMusicFragment.playMode.length; i++){
            if(localMusicFragment.currentPlayMode.contentEquals(localMusicFragment.playMode[i])){
                break;
            }
        }
        if(i>=localMusicFragment.playMode.length)
        {
            i=localMusicFragment.playMode.length-1;
        }
        //只改变播放音乐的下标
        switch (i){
            case 0://顺序播放
                if(playingMusicIndex==(localMusicFragment.sMusicList.size()-1))
                {
                    nextMusic();
                    handler.post(runnable);
                    mediaPlayer.pause();
                      mPlayMusicButton.setBackgroundResource(R.drawable.pause_image);//把播放图标变为暂停图标
                    localMusicFragment.isPlay=false;
                    handler.removeCallbacks(runnable);

                }
                else{
                    nextMusic();
                    handler.post(runnable);
                }
                break;
            case 1://列表循环
                nextMusic();
                handler.post(runnable);
                break;
            case 2://单曲循环
                initMusic();
                mediaPlayer.start();
                handler.post(runnable);
                break;
            case 3://随机播放
                int MusicIndex=new Random().nextInt(sMusicList.size()-1);
                while(MusicIndex==playingMusicIndex)
                {
                    MusicIndex=new Random().nextInt(sMusicList.size()-1);
                }
                playingMusicIndex=MusicIndex;
                initMusic();
                mediaPlayer.start();
                handler.post(runnable);
                break;
            default:
                break;
        }
        Log.i(TAG, "autoPlayMusic: "+playingMusicIndex);

    }
    public void nextMusic(){
        int i;
        for(i=0;i<localMusicFragment.playMode.length;i++){
            if(localMusicFragment.currentPlayMode.contentEquals(localMusicFragment.playMode[i])){
                break;
            }
        }
        if(i>=localMusicFragment.playMode.length)
        {
            i=localMusicFragment.playMode.length-1;
        }
        //只改变播放音乐的下标
        switch (i){
            case 0://顺序播放
            case 1://列表循环
            case 2://单曲循环
                playingMusicIndex=(playingMusicIndex==sMusicList.size()-1)?0:(playingMusicIndex+1);
                break;
            case 3://随机播放
                int MusicIndex=new Random().nextInt(sMusicList.size()-1);
                while(MusicIndex==playingMusicIndex)
                {
                    MusicIndex=new Random().nextInt(sMusicList.size()-1);
                }
                playingMusicIndex=MusicIndex;
                break;
            default:
                break;
        }
        Log.i(TAG, "NextMusic: "+playingMusicIndex);
        initMusic();
        mediaPlayer.start();
        handler.post(runnable);
    }

    public void prevMusic(){
        int i;
        for(i=0;i<localMusicFragment.playMode.length;i++){
            if(localMusicFragment.currentPlayMode.contentEquals(localMusicFragment.playMode[i])){
                break;
            }
        }
        if(i>=localMusicFragment.playMode.length)
        {
            i=localMusicFragment.playMode.length-1;
        }
        //只改变播放音乐的下标
        switch (i){
            case 0://顺序播放
            case 1://列表循环
            case 2://单曲循环
                playingMusicIndex=(playingMusicIndex==0)?(sMusicList.size()-1):(playingMusicIndex-1);
                break;
            case 3://随机播放
                int MusicIndex=new Random().nextInt(sMusicList.size()-1);
                while(MusicIndex==playingMusicIndex)
                {
                    MusicIndex=new Random().nextInt(sMusicList.size()-1);
                }
                playingMusicIndex=MusicIndex;
                break;
            default:
                break;
        }
        Log.i(TAG, "PrevMusic: "+playingMusicIndex);
        initMusic();
        mediaPlayer.start();
        handler.post(runnable);
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle=intent.getExtras();
        playAction action= (playAction) bundle.getSerializable("key");
        switch (action){
            case start:
                startMusic();
                break;
            case prev:
                prevMusic();
                break;
            case next:
                nextMusic();
                break;
                default:
                    break;
        }
        return super.onStartCommand(intent, flags, startId);
    }

}
