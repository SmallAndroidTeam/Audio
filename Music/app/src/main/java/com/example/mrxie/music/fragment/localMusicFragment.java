package com.example.mrxie.music.fragment;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.mrxie.music.R;
import com.example.mrxie.music.Service.MusicService;
import com.example.mrxie.music.SongListInformation.App;
import com.example.mrxie.music.SongListInformation.Music;
import com.example.mrxie.music.SongListInformation.MusicUtils;
import com.example.mrxie.music.Toast.OnlyOneToast;
import com.example.mrxie.music.convertPXAndDP.DensityUtil;
import com.example.mrxie.music.ui.LrcView;

import java.util.ArrayList;

public class localMusicFragment extends Fragment implements View.OnClickListener {
    private String TAG="Music";
    private ImageView mAddLikeMusicButton;
    private ImageView mSingleCycleMusicButton;
    private ImageView mPlayModeButton;
    private ImageView mPrevMusicButton;
    private ImageView mPlayMusicButton;
    private ImageView mNextMusicButton;
    private TextView mPlayMusicStartTimeTextView;
    private TextView mPlayMusicStopTimeTextView;
    public ImageView MusicImage;
    private SeekBar mPlayMusicSeekBar;
    public static boolean isPlay=false;
    public static boolean isLike=false;
    public static String[] playMode=new String[]{"list_mode","circulate_mode","singlecycle_mode","randomplay_mode"};//播放模式
    public static String currentPlayMode=playMode[3];//当前的音乐播放模式
    public static Activity activity;
    public static TextView musicTitle;
    public static ArrayList<Music> sMusicList = new ArrayList<Music>(); // 存放歌曲列表
    private LrcView showLrcView;
    private float marginleft=40;//歌单和专辑图片距离左边的距离(单位dp)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       View view=inflater.inflate(R.layout.localmusic,container,false);
       initViews(view);
        //根据屏幕的宽高来初始化控件的位置和大小
        initImageIconPositionAndSize();
       initEvents();
       initPlayMusic(view);
       return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    //根据屏幕的宽高来初始化控件的位置和大小
    private  void  initImageIconPositionAndSize(){
        WindowManager windowManager=activity.getWindowManager();
        long screenHeigt=windowManager.getDefaultDisplay().getHeight();//屏幕的高度
        long screenWidth=windowManager.getDefaultDisplay().getWidth();
        RelativeLayout.LayoutParams layoutParams= (RelativeLayout.LayoutParams) musicTitle.getLayoutParams();
        layoutParams.width=(int)(1.0*screenWidth*3/7)- DensityUtil.dip2px(activity,marginleft);
        musicTitle.setLayoutParams(layoutParams);

       // Log.i(TAG, (musicTitle!=null)+"initImageIconPositionAndSize: "+"screentHeight:"+screenHeigt+"/screenWidth:"+screenWidth+"//"+(int)(1.0*screenWidth*3/7));

    }

    private void initPlayMusic(View view) {
        App.sContext=view.getContext();//设置上下文的环境
        MusicUtils.initMusicList();
       sMusicList=MusicUtils.sMusicList;
        MusicService.sMusicList=sMusicList;
        if(sMusicList.size()>0){
            MusicService.playingMusicIndex=0;
        }
        MusicService.initMusic();
        Log.i(TAG, "Music数量"+sMusicList.size());
       for(Music music:sMusicList){
           Log.i(TAG, "initPlayMusic: "+music.getUri());
       }

    }

     private void setLrc(){
         String path=sMusicList.get(MusicService.playingMusicIndex).getLrcpath();
         showLrcView.setLrcPath(path);
     }
    private void initEvents() {
        mPlayMusicButton.setOnClickListener(this);//设置点击播放按钮事件
        mAddLikeMusicButton.setOnClickListener(this);//设置添加喜欢音乐的按钮单击事件
        mPlayModeButton.setOnClickListener(this);
        mSingleCycleMusicButton.setOnClickListener(this);
        mPrevMusicButton.setOnClickListener(this);
        mNextMusicButton.setOnClickListener(this);
        mSingleCycleMusicButton.setOnClickListener(this);


        mPlayMusicSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
             MusicService.mediaPlayer.seekTo(mPlayMusicSeekBar.getProgress());
             showLrcView.onDrag(mPlayMusicSeekBar.getProgress());//绘画
            }
        });
        mPlayMusicButton.setOnClickListener(this);//设置点击播放按钮事件
        mAddLikeMusicButton.setOnClickListener(this);//设置添加喜欢音乐的按钮单击事件
        mPlayModeButton.setOnClickListener(this);
        mSingleCycleMusicButton.setOnClickListener(this);
    }

    private void initViews(View view) {
        mAddLikeMusicButton = (ImageView)view.findViewById(R.id.addLikeMusicButton);
        mSingleCycleMusicButton = (ImageView)view.findViewById(R.id.singleCycleMusicButton);
        mPlayModeButton = (ImageView)view.findViewById(R.id.playModeButton);
        mPrevMusicButton = (ImageView)view.findViewById(R.id.prevMusicButton);
        mPlayMusicButton = (ImageView)view.findViewById(R.id.playMusicButton);
        mNextMusicButton = (ImageView)view.findViewById(R.id.nextMusicButton);
        mPlayMusicStartTimeTextView = (TextView)view.findViewById(R.id.playMusicStartTimeTextView);
        mPlayMusicStopTimeTextView = (TextView)view.findViewById(R.id.playMusicStopTimeTextView);
        mPlayMusicSeekBar = (SeekBar)view.findViewById(R.id.playMusicSeekBar);
        MusicImage=(ImageView)view.findViewById(R.id.albumCover);
        showLrcView = (LrcView)view.findViewById(R.id.show_lyric);
        MusicService.showLrcView=showLrcView;
        MusicService.MusicImage=MusicImage;
        MusicService.mPlayMusicStartTimeTextView=mPlayMusicStartTimeTextView;
        MusicService.mPlayMusicStopTimeTextView=mPlayMusicStopTimeTextView;
        MusicService.mPlayMusicSeekBar=mPlayMusicSeekBar;
        MusicService.mPlayMusicButton=mPlayMusicButton;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.playMusicButton:
                if(isPlay){
                mPlayMusicButton.setImageResource(R.drawable.pause_image);
                isPlay=false;
            }else{
                mPlayMusicButton.setImageResource(R.drawable.play_music);
                isPlay=true;
            }
                Bundle bundle=new Bundle();
                bundle.putSerializable("key", MusicService.playAction.start);
                Intent intent=new Intent(activity,MusicService.class);
                intent.putExtras(bundle);
                view.getContext().startService(intent);
            break;
            case R.id.nextMusicButton:
                mPlayMusicButton.setImageResource(R.drawable.play_music);
                isPlay=true;
                Bundle bundle1=new Bundle();
                bundle1.putSerializable("key", MusicService.playAction.next);
                Intent intent1=new Intent(activity,MusicService.class);
                intent1.putExtras(bundle1);
                view.getContext().startService(intent1);
                break;
            case R.id.prevMusicButton:
                    mPlayMusicButton.setImageResource(R.drawable.play_music);
                    isPlay=true;
                Bundle bundle2=new Bundle();
                bundle2.putSerializable("key", MusicService.playAction.prev);
                Intent intent2=new Intent(activity,MusicService.class);
                intent2.putExtras(bundle2);
                view.getContext().startService(intent2);
                break;
            case R.id.addLikeMusicButton:
               if(isLike){
                 mAddLikeMusicButton.setImageResource(R.drawable.like_image);
                 OnlyOneToast.makeText(view.getContext(),"已取消喜欢");
                 isLike=false;
               }else{
                  mAddLikeMusicButton.setImageResource(R.drawable.like_image_selected);
                   OnlyOneToast.makeText(view.getContext(),"已添加到我喜欢的音乐");
                  isLike=true;
               }
                break;
            case R.id.playModeButton:
                int i;
                for(i=0;i<playMode.length;i++){
                    if(currentPlayMode.contentEquals(playMode[i])){
                        break;
                    }
                }
                if(i>=playMode.length)
                {
                    i=playMode.length-1;
                }
                int index=(i+1)%(playMode.length);
                currentPlayMode=playMode[index];
                switch (index){
                    case 0:
                        mPlayModeButton.setImageResource(R.drawable.list_mode);
                        OnlyOneToast.makeText(view.getContext(),"顺序播放");
                        break;
                    case 1:
                        mPlayModeButton.setImageResource(R.drawable.circulate_mode);
                        OnlyOneToast.makeText(view.getContext(),"列表循环");
                        break;
                    case 2:
                        mPlayModeButton.setImageResource(R.drawable.singlecycle_mode);
                        OnlyOneToast.makeText(view.getContext(),"单曲循环");
                        break;
                        case 3:
                            mPlayModeButton.setImageResource(R.drawable.randomplay);
                            OnlyOneToast.makeText(view.getContext(),"随机播放");
                        break;
                        default:
                            break;
                }
                break;
            case R.id.singleCycleMusicButton:
//                OnlyOneToast.makeText(view.getContext(),"单曲循环");
//                currentPlayMode=playMode[2];
                break;
            default:break;
        }
    }

}
