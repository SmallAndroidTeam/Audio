package com.example.mrxie.music.fragment;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.mrxie.music.R;
import com.example.mrxie.music.services.MusicService;
import com.example.mrxie.music.songListInformation.App;
import com.example.mrxie.music.songListInformation.Music;
import com.example.mrxie.music.songListInformation.MusicIconLoader;
import com.example.mrxie.music.songListInformation.MusicUtils;
import com.example.mrxie.music.toast.OnlyOneToast;
import com.example.mrxie.music.convertPXAndDP.DensityUtil;
import com.example.mrxie.music.db.MusicOperator;
import com.example.mrxie.music.ui.LrcView;

import java.util.ArrayList;

public class LocalMusicFragment extends Fragment implements View.OnClickListener {
    private String TAG="Music";
    public static  ImageView mAddLikeMusicButton;
    private ImageView mSingleCycleMusicButton;
    private ImageView mPlayModeButton;
    private static ImageView mPrevMusicButton;
    private static ImageView mPlayMusicButton;
    private static ImageView mNextMusicButton;
    private TextView mPlayMusicStartTimeTextView;
    private TextView mPlayMusicStopTimeTextView;
    public ImageView MusicImage;
    private SeekBar mPlayMusicSeekBar;
    public  boolean isLike=false;
    public static String[] playMode=new String[]{"list_mode","circulate_mode","singlecycle_mode","randomplay_mode"};//播放模式
    public static String currentPlayMode=playMode[3];//当前的音乐播放模式
    public static Activity activity;
    public static TextView musicTitle;
    public static ArrayList<Music> sMusicList = new ArrayList<Music>(); // 存放歌曲列表
    private LrcView showLrcView;
    private float marginleft=40;//歌单和专辑图片距离左边的距离(单位dp)
    private LinearLayout PlayBoundaryFragment;
    private RelativeLayout controlMusicButtonLinearLayout;
    private LinearLayout lrcLinearLayout;
    private RelativeLayout RightPalyBoundary;
    public static MusicOperator lxrOperator;
    private static final int SET_RANKINGBEAN = 123;
    private LovingListFragment oneFragment=new LovingListFragment();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.localmusic,container,false);
        initViews(view);
        //根据屏幕的宽高来初始化控件的位置和大小
        initImageIconPositionAndSize();

        initEvents();
        initPlayMusic(view);
        lxrOperator=new MusicOperator(getActivity());
         initLovebutton();//初始化喜欢的图标
        return view;
    }

    private void initLovebutton() {
        boolean isLike=false;
        if(LocalMusicFragment.lxrOperator!=null) {
            isLike = LocalMusicFragment.lxrOperator.CheckIsDataAlreadyInDBorNot(musicTitle.getText().toString());
            if (isLike == true) {
                mAddLikeMusicButton.setImageResource(R.drawable.like_image_selected);

            } else {
                mAddLikeMusicButton.setImageResource(R.drawable.like_image);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public  static ImageView getmPlayMusicButton() {
        return mPlayMusicButton;
    }

    public  static ImageView getmPrevMusicButton() {
        return mPrevMusicButton;
    }

    public static ImageView getmNextMusicButton() {
        return mNextMusicButton;
    }

    //根据屏幕的宽高来初始化控件的位置和大小
    private  void  initImageIconPositionAndSize(){
        WindowManager windowManager=activity.getWindowManager();
        long screenHeigt=windowManager.getDefaultDisplay().getHeight();//屏幕的高度
        long screenWidth=windowManager.getDefaultDisplay().getWidth();
        int marginLeft=(int)(1.0*screenHeigt/10);

        FrameLayout.LayoutParams PlayBoundaryFragmentLayoutParams=new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT);
        PlayBoundaryFragmentLayoutParams.setMargins(marginLeft,0,marginLeft,marginLeft);
        PlayBoundaryFragment.setLayoutParams(PlayBoundaryFragmentLayoutParams);

        RelativeLayout.LayoutParams controlMusicButtonLinearLayoutLayoutParams=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        controlMusicButtonLinearLayoutLayoutParams.topMargin= (int) (7.0*5*screenHeigt/(10*7));
        controlMusicButtonLinearLayoutLayoutParams.leftMargin=(int)((screenWidth*4/7-screenWidth*1/10)/13);
        controlMusicButtonLinearLayoutLayoutParams.rightMargin=(int)((screenWidth*4/7-screenWidth*1/10)/13);
        controlMusicButtonLinearLayout.setLayoutParams(controlMusicButtonLinearLayoutLayoutParams);

        ViewGroup.LayoutParams lrcLinearLayoutLinearLayoutLayoutParams=(ViewGroup.LayoutParams)lrcLinearLayout.getLayoutParams();
        lrcLinearLayoutLinearLayoutLayoutParams.height=(int) (7.0*5*screenHeigt/(10*7));

        LinearLayout.LayoutParams PlayBoundaryFragmentLayoutParmas=new LinearLayout.LayoutParams((int)(screenWidth*4/7-screenWidth*1/10),LinearLayout.LayoutParams.MATCH_PARENT);
        PlayBoundaryFragmentLayoutParmas.setMargins((int)(marginLeft/2),0,0,0);
        RightPalyBoundary.setLayoutParams(PlayBoundaryFragmentLayoutParmas);

        //设置播放按钮和其他按钮的大小和位置
        RelativeLayout.LayoutParams mPrevMusicButtonLayoutParams=(RelativeLayout.LayoutParams)mPrevMusicButton.getLayoutParams();
        mPrevMusicButtonLayoutParams.width=marginLeft;
        mPrevMusicButtonLayoutParams.height=marginLeft;
        mPrevMusicButton.setLayoutParams(mPrevMusicButtonLayoutParams);

        RelativeLayout.LayoutParams mPlayMusicButtonLayoutParams=(RelativeLayout.LayoutParams)mPlayMusicButton.getLayoutParams();
        mPlayMusicButtonLayoutParams.width=marginLeft;
        mPlayMusicButtonLayoutParams.height=marginLeft;
        mPlayMusicButton.setLayoutParams(mPlayMusicButtonLayoutParams);

        RelativeLayout.LayoutParams mNextMusicButtonLayoutParams=(RelativeLayout.LayoutParams)mNextMusicButton.getLayoutParams();
        mNextMusicButtonLayoutParams.width=marginLeft;
        mNextMusicButtonLayoutParams.height=marginLeft;
        mNextMusicButton.setLayoutParams(mNextMusicButtonLayoutParams);
        //设置左边专辑图片上的按钮大小
        RelativeLayout.LayoutParams like_imageLayoutParams=(RelativeLayout.LayoutParams)mAddLikeMusicButton.getLayoutParams();
        like_imageLayoutParams.width=marginLeft;
        like_imageLayoutParams.height=marginLeft;
        like_imageLayoutParams.leftMargin=marginLeft/2;
        like_imageLayoutParams.bottomMargin=marginLeft/3;
        mAddLikeMusicButton.setLayoutParams(like_imageLayoutParams);

        RelativeLayout.LayoutParams mSingleCycleMusicButtonLayoutParams=(RelativeLayout.LayoutParams)mSingleCycleMusicButton.getLayoutParams();
        mSingleCycleMusicButtonLayoutParams.width=marginLeft;
        mSingleCycleMusicButtonLayoutParams.height=marginLeft;
        mSingleCycleMusicButtonLayoutParams.bottomMargin=marginLeft/3;
        mSingleCycleMusicButton.setLayoutParams(mSingleCycleMusicButtonLayoutParams);

        RelativeLayout.LayoutParams mPlayModeButtonLayoutParams=(RelativeLayout.LayoutParams)mPlayModeButton.getLayoutParams();
        mPlayModeButtonLayoutParams.width=marginLeft;
        mPlayModeButtonLayoutParams.height=marginLeft;
        mPlayModeButtonLayoutParams.rightMargin=marginLeft/2;
        mPlayModeButtonLayoutParams.bottomMargin=marginLeft/3;
        mPlayModeButton.setLayoutParams(mPlayModeButtonLayoutParams);

        // 设置歌词字体大小
//         Paint paint=new Paint();
//         paint.setTextSize(20);
//        new Canvas().drawText();
        //设置播放时间字体大小
        mPlayMusicStartTimeTextView.setTextSize(DensityUtil.px2sp(activity,marginLeft/4));
        mPlayMusicStopTimeTextView.setTextSize(DensityUtil.px2sp(activity,marginLeft/4));

    }

    private void initPlayMusic(View view) {
        //判断服务是否以正在运行
        if(MusicService.playingMusicIndex!=-1){

            musicTitle.setText(sMusicList.get(MusicService.playingMusicIndex).getTitle());

            mPlayMusicSeekBar.setMax(MusicService.mediaPlayer.getDuration());
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
                mPlayMusicSeekBar.setProgress(MusicService.mediaPlayer.getCurrentPosition());
            if (mPlayMusicStartTimeTextView != null)
                mPlayMusicStartTimeTextView.setText(MusicService.changeDigitsToTwoDigits(MusicService.mediaPlayer.getCurrentPosition() / 1000 / 60) + ":" + MusicService.changeDigitsToTwoDigits(MusicService.mediaPlayer.getCurrentPosition() / 1000 % 60));
            if (mPlayMusicStopTimeTextView != null)
                mPlayMusicStopTimeTextView.setText(MusicService.changeDigitsToTwoDigits((MusicService.mediaPlayer.getDuration()) / 1000 / 60) + ":" + MusicService.changeDigitsToTwoDigits(MusicService.mediaPlayer.getDuration() / 1000 % 60));
            if(MusicService.mediaPlayer.isPlaying()){
                mPlayMusicButton.setImageResource(R.drawable.play_music);
            }

        }else{
            App.sContext=view.getContext();//设置上下文的环境
            MusicUtils.initMusicList();
            sMusicList=MusicUtils.sMusicList;
            if(sMusicList.size()>0){
                MusicService.playingMusicIndex=0;
            }
            new MusicService().initMusic();
        }


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
                if(sMusicList.size()==0){
                    OnlyOneToast.makeText(activity,"暂无歌曲");
                    mPlayMusicSeekBar.setProgress(0);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mPlayMusicStartTimeTextView.setText(MusicService.changeDigitsToTwoDigits(seekBar.getProgress()/1000/60)+":"+MusicService.changeDigitsToTwoDigits(seekBar.getProgress()/1000%60));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mPlayMusicStartTimeTextView.setText(MusicService.changeDigitsToTwoDigits(seekBar.getProgress()/1000/60)+":"+MusicService.changeDigitsToTwoDigits(seekBar.getProgress()/1000%60));
                MusicService.mediaPlayer.seekTo(mPlayMusicSeekBar.getProgress());
                showLrcView.onDrag(mPlayMusicSeekBar.getProgress());//绘画
            }
        });


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
        PlayBoundaryFragment=(LinearLayout)view.findViewById(R.id.PlayBoundaryFragment);
        controlMusicButtonLinearLayout = (RelativeLayout) view.findViewById(R.id.controlMusicButtonLinearLayout);
        lrcLinearLayout = (LinearLayout) view.findViewById(R.id.lrcLinearLayout);
        RightPalyBoundary=(RelativeLayout)view.findViewById(R.id.RightPalyBoundary);
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
                if(sMusicList.size()==0){
                    OnlyOneToast.makeText(view.getContext(),"暂无歌曲");
                    return;
                }
                Intent intent=new Intent(view.getContext(),MusicService.class);
                intent.setAction(MusicService.TOGGLEPAUSE_ACTION);
                view.getContext().startService(intent);
                break;
            case R.id.nextMusicButton:

                if(sMusicList.size()==0){
                    OnlyOneToast.makeText(view.getContext(),"暂无歌曲");

                    return;
                }
                Intent intent1=new Intent(activity,MusicService.class);
                intent1.setAction(MusicService.NEXT_ACTION);
                view.getContext().startService(intent1);
                break;
            case R.id.prevMusicButton:
                if(sMusicList.size()==0){
                    OnlyOneToast.makeText(view.getContext(),"暂无歌曲");
                    return;
                }
                Intent intent2=new Intent(activity,MusicService.class);
                intent2.setAction(MusicService.PREVIOUS_ACTION);
                view.getContext().startService(intent2);
                break;
            case R.id.addLikeMusicButton:
                final  Intent loveIntent=new Intent(view.getContext(),MusicService.class);
                loveIntent.setAction(MusicService.WIDGET_LOVE_ACTION);
                view.getContext().startService(loveIntent);
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
