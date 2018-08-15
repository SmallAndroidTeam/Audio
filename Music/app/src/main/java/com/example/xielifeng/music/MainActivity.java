package com.example.xielifeng.music;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.xielifeng.music.fragment.localMusicFragment;
import com.example.xielifeng.music.fragment.onlineMusicFragment;
import com.example.xielifeng.music.fragment.settingFragment;
import com.example.xielifeng.music.fragment.songListFragment;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private String TAG="Music";
    private ImageButton msongListButton;
    private ImageButton monlineMusicButton;
    private ImageButton msettingButton;
    private ImageButton mLocalMusicButton;
    private Fragment mlocalMusicFragment,msongListFragment,monlineMusicFragment,msettingFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //去掉标题
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.hide();
        }

        initViews();
        initEvents();
        selectTab(0);//设置默认的主页
    }


    private void initViews() {
        mLocalMusicButton = (ImageButton) this.findViewById(R.id.music);
        msongListButton = (ImageButton) this.findViewById(R.id.SongList);
        monlineMusicButton =(ImageButton)this.findViewById(R.id.onlineMusic);
        msettingButton = (ImageButton) this.findViewById(R.id.set);

    }
    private void initEvents() {
        mLocalMusicButton.setOnClickListener(this);
        msongListButton.setOnClickListener(this);
        monlineMusicButton.setOnClickListener(this);
        msettingButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
      switch (view.getId()){
          case R.id.music:
              selectTab(0);
              break;
          case R.id.SongList:
              selectTab(1);
              break;
          case R.id.onlineMusic:
              selectTab(2);
              break;
          case R.id.set:
              selectTab(3);
              break;
          default:
              break;
      }
    }
    private void selectTab(int i){
        FragmentManager fragmentManager=getSupportFragmentManager();
        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        hideFragments(fragmentTransaction);
        switch (i){
            case 0:
                mLocalMusicButton.setBackgroundResource(R.drawable.localmusic_selected);
                 if(mlocalMusicFragment==null){
                     mlocalMusicFragment=new localMusicFragment();
                     fragmentTransaction.add(R.id.IndexContent,mlocalMusicFragment);
                 }else{
                     fragmentTransaction.show(mlocalMusicFragment);
                 }
                break;
            case 1:
                msongListButton.setBackgroundResource(R.drawable.songlist_selected);
                if(msongListFragment==null){
                    msongListFragment=new songListFragment();
                    fragmentTransaction.add(R.id.IndexContent,msongListFragment);
                }else{
                    fragmentTransaction.show(msongListFragment);
                }
                break;
            case 2:
                monlineMusicButton.setBackgroundResource(R.drawable.onlinemusic_selected);
                if(monlineMusicFragment==null){
                    monlineMusicFragment=new onlineMusicFragment();
                    fragmentTransaction.add(R.id.IndexContent,monlineMusicFragment);
                }else{
                    fragmentTransaction.show(monlineMusicFragment);
                }
                break;
            case 3:
                msettingButton.setBackgroundResource(R.drawable.setting_selected);
                if(msettingFragment==null){
                    msettingFragment=new settingFragment();
                    fragmentTransaction.add(R.id.IndexContent,msettingFragment);
                }else{
                    fragmentTransaction.show(msettingFragment);
                }
                break;
            default:
                break;
        }
        fragmentTransaction.commit();
    }
    private void hideFragments(FragmentTransaction fragmentTransaction){
      if(mlocalMusicFragment!=null){
          fragmentTransaction.hide(mlocalMusicFragment);
      }
        if(msongListFragment!=null){
            fragmentTransaction.hide(msongListFragment);
        }
        if(monlineMusicFragment!=null){
            fragmentTransaction.hide(monlineMusicFragment);
        }
        if(msettingFragment!=null){
            fragmentTransaction.hide(msettingFragment);
        }

        mLocalMusicButton.setBackgroundResource(R.drawable.localmusic);
        msongListButton.setBackgroundResource(R.drawable.songlist);
        monlineMusicButton.setBackgroundResource(R.drawable.onlinemusic);
        msettingButton.setBackgroundResource(R.drawable.set);
    }
}
