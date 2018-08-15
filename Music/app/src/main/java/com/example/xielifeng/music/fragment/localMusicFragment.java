package com.example.xielifeng.music.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.xielifeng.music.MainActivity;
import com.example.xielifeng.music.R;
import com.example.xielifeng.music.Toast.OnlyOneToast;

public class localMusicFragment extends Fragment implements View.OnClickListener {
    private String TAG="Music";
    private ImageView mAddLikeMusicButton;
    private ImageView mSingleCycleMusicButton;
    private ImageView mPlayModeButton;
    private ImageButton mPrevMusicButton;
    private ImageButton mPlayMusicButton;
    private ImageButton mNextMusicButton;
    private TextView mPlayMusicStartTimeTextView;
    private TextView mPlayMusicStopTimeTextView;
    private SeekBar mPlayMusicSeekBar;
    public static boolean isPlay=false;
    public static boolean isLike=false;
    public static String[] playMode=new String[]{"list_mode","circulate_mode","singlecycle_mode","randomplay_mode"};//播放模式
    public static String currentPlayMode=playMode[3];//当前的音乐播放模式
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       View view=inflater.inflate(R.layout.localmusic,container,false);
        mAddLikeMusicButton = (ImageView)view.findViewById(R.id.addLikeMusicButton);
        mSingleCycleMusicButton = (ImageView)view.findViewById(R.id.singleCycleMusicButton);
        mPlayModeButton = (ImageView)view.findViewById(R.id.playModeButton);
        mPrevMusicButton = (ImageButton)view.findViewById(R.id.prevMusicButton);
        mPlayMusicButton = (ImageButton)view.findViewById(R.id.playMusicButton);
        mNextMusicButton = (ImageButton)view.findViewById(R.id.nextMusicButton);
        mPlayMusicStartTimeTextView = (TextView)view.findViewById(R.id.playMusicStartTimeTextView);
        mPlayMusicStopTimeTextView = (TextView)view.findViewById(R.id.playMusicStopTimeTextView);
        mPlayMusicSeekBar = (SeekBar)view.findViewById(R.id.playMusicSeekBar);
        mPlayMusicButton.setOnClickListener(this);//设置点击播放按钮事件
        mAddLikeMusicButton.setOnClickListener(this);//设置添加喜欢音乐的按钮单击事件
        mPlayModeButton.setOnClickListener(this);
        mSingleCycleMusicButton.setOnClickListener(this);
       return view;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.playMusicButton:
                if(isPlay){
                mPlayMusicButton.setBackgroundResource(R.drawable.pause_image);
                isPlay=false;
            }else{
                mPlayMusicButton.setBackgroundResource(R.drawable.play_music);
                isPlay=true;
            }
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
                OnlyOneToast.makeText(view.getContext(),"单曲循环");
                break;
            default:break;
        }
    }
}
