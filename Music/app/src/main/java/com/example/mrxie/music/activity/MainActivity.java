package com.example.mrxie.music.activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.mrxie.music.R;
import com.example.mrxie.music.Service.MusicService;
import com.example.mrxie.music.Toast.OnlyOneToast;
import com.example.mrxie.music.fragment.localMusicFragment;
import com.example.mrxie.music.fragment.songListFragment;
import com.example.mrxie.music.fragment.searchMusicFragment;
import com.example.mrxie.music.fragment.settingFragment;
import com.example.mrxie.music.fragment.onlineMusicFragment;

public class MainActivity extends FragmentActivity implements View.OnClickListener {
    private String TAG="Music";
    private ImageButton msongListButton;
    private ImageButton monlineMusicButton;
    private ImageButton msettingButton;
    private ImageButton mLocalMusicButton;
    private Fragment mlocalMusicFragment,msongListFragment,monlineMusicFragment,msettingFragment,searchMusicFragment;
    private TextView musicTitle;
    private ImageButton searchMusicButton;
    private long time=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //无标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        initViews();
        initEvents();
        selectTab(0);//设置默认的主页
    }

     //加载完
    @Override
    protected void onResume() {
        super.onResume();
    }
    //重写了单点事件
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {//点击二次返回桌面
        if(keyCode==KeyEvent.KEYCODE_BACK)
        {
            if((System.currentTimeMillis()-time)>1000)
            {
                OnlyOneToast.makeText(MainActivity.this,"再按一次返回桌面");
                time=System.currentTimeMillis();
            }else{
                Intent intent=new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
            }
            return true;
        }else{
            return super.onKeyDown(keyCode, event);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus){
           // initImageIconPositionAndSize();//根据屏幕的宽高来初始化控件的位置和大小
        }
    }

    private void initViews() {
        mLocalMusicButton = (ImageButton) this.findViewById(R.id.music);
        msongListButton = (ImageButton) this.findViewById(R.id.SongList);
        monlineMusicButton =(ImageButton)this.findViewById(R.id.onlineMusic);
        msettingButton = (ImageButton) this.findViewById(R.id.set);
        searchMusicButton = (ImageButton)this.findViewById(R.id.search);
        musicTitle = (TextView)this.findViewById(R.id.musicTitle);
        localMusicFragment.musicTitle=musicTitle;
        MusicService.musicTitle=musicTitle;
        localMusicFragment.activity=MainActivity.this;

    }
    private void initEvents() {
        mLocalMusicButton.setOnClickListener(this);
        msongListButton.setOnClickListener(this);
        monlineMusicButton.setOnClickListener(this);
        msettingButton.setOnClickListener(this);
        searchMusicButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
      switch (view.getId()){
          case R.id.music:
             if(musicTitle.getVisibility()==View.INVISIBLE){//如果标题没显示就显示
                 musicTitle.setVisibility(View.VISIBLE);
             }
              selectTab(0);
              break;
          case R.id.SongList:
              if(musicTitle.getVisibility()==View.VISIBLE){//如果标题显示就没显示
                  musicTitle.setVisibility(View.INVISIBLE);
              }
              selectTab(1);
              break;
          case R.id.onlineMusic:
              if(musicTitle.getVisibility()==View.VISIBLE){
                  musicTitle.setVisibility(View.INVISIBLE);
              }
              selectTab(2);
              break;
          case R.id.set:
              if(musicTitle.getVisibility()==View.VISIBLE){
                  musicTitle.setVisibility(View.INVISIBLE);
              }
              selectTab(3);
              break;
          case R.id.search:
              if(musicTitle.getVisibility()==View.VISIBLE){
                  musicTitle.setVisibility(View.INVISIBLE);
              }
              selectTab(4);break;
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
            case 4:
                searchMusicButton.setBackgroundResource(R.drawable.search_selected);
                if(searchMusicFragment==null){
                    searchMusicFragment=new searchMusicFragment();
                    fragmentTransaction.add(R.id.IndexContent,searchMusicFragment);
                }
                else{
                    fragmentTransaction.show(searchMusicFragment);
                }
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
        if(searchMusicFragment!=null){
          fragmentTransaction.hide(searchMusicFragment);
        }
        mLocalMusicButton.setBackgroundResource(R.drawable.localmusic);
        msongListButton.setBackgroundResource(R.drawable.songlist);
        monlineMusicButton.setBackgroundResource(R.drawable.onlinemusic);
        msettingButton.setBackgroundResource(R.drawable.set);
        searchMusicButton.setBackgroundResource(R.drawable.search);
    }

    @Override
    protected void onDestroy() {
//        //当应用关闭之后关闭服务
//        Intent intent=new Intent(MainActivity.this,MusicService.class);
//        stopService(intent);
        super.onDestroy();
    }
}
