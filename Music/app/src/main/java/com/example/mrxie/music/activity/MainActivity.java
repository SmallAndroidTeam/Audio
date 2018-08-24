package com.example.mrxie.music.activity;
import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.util.Log;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.mrxie.music.R;
import com.example.mrxie.music.Service.MusicService;
import com.example.mrxie.music.Toast.OnlyOneToast;
import com.example.mrxie.music.convertPXAndDP.DensityUtil;
import com.example.mrxie.music.adapter.ContentAdapter;
import com.example.mrxie.music.adapter.ContentModel;

import com.example.mrxie.music.dialog.CardPickerDialog;
import com.example.mrxie.music.fragment.TimingFragment;
import com.example.mrxie.music.fragment.localMusicFragment;
import com.example.mrxie.music.fragment.onlineMusicFragment;
import com.example.mrxie.music.fragment.searchMusicFragment;
import com.example.mrxie.music.fragment.settingFragment;
import com.example.mrxie.music.fragment.songListFragment;
import com.example.mrxie.music.ui.LrcView;

import com.example.mrxie.music.ui.ThemeHelper;

import java.util.ArrayList;
import java.util.List;

import retrofit2.http.HEAD;

public class MainActivity extends FragmentActivity implements View.OnClickListener, CardPickerDialog.ClickListener {
    private String TAG="Music";
    private ImageButton msongListButton;
    private ImageButton monlineMusicButton;
    private ImageButton msettingButton;
    private ImageButton mLocalMusicButton;
    private Fragment mlocalMusicFragment,msongListFragment,monlineMusicFragment,msettingFragment,searchMusicFragment;
    private TextView musicTitle;
    private ImageButton searchMusicButton;
    private DrawerLayout drawerLayout;
    private ListView mLvLeftMenu;
    private long time=0;
    private LinearLayout tabLinearLayout;
    private LinearLayout TitleBarLinearLayout;
    private ContentAdapter adapter;
    private List<ContentModel> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //无标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        initViews();
        //根据屏幕的宽高来初始化控件的位置和大小
       // initImageIconPositionAndSize();
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
            initImageIconPositionAndSize();//根据屏幕的宽高来初始化控件的位置和大小
        }
    }
    //根据屏幕的宽高来初始化控件的位置和大小
    private  void  initImageIconPositionAndSize(){
        WindowManager windowManager=getWindowManager();
        long screenHeigt=windowManager.getDefaultDisplay().getHeight();//屏幕的高度
        long screenWidth=windowManager.getDefaultDisplay().getWidth();
        int marginLeft=(int)(1.0*screenHeigt/10);
        int TitleBarHeigt=(int)(2.0*screenHeigt/10);
        LinearLayout.LayoutParams musicTitleLayoutParams= (LinearLayout.LayoutParams) musicTitle.getLayoutParams();
        musicTitleLayoutParams.width=(int)(1.0*screenWidth*3/7)-marginLeft;
        musicTitleLayoutParams.leftMargin=marginLeft;
       // musicTitleLayoutParams.topMargin=marginLeft;
        musicTitle.setLayoutParams(musicTitleLayoutParams);

        LayoutParams TitleBarRelativeLayoutLayoutParams=TitleBarLinearLayout.getLayoutParams();
        TitleBarRelativeLayoutLayoutParams.height=TitleBarHeigt;

         LayoutParams tabLinearLayoutLayoutParams= (LayoutParams) tabLinearLayout.getLayoutParams();
        tabLinearLayoutLayoutParams.width=(int) (1.0*screenWidth*4/7);


        int IconWidth;
        if(6*marginLeft>=screenWidth*4.0/7){
            IconWidth=(int)((screenWidth*4.0/7-marginLeft)/6);
        }else{
            IconWidth=marginLeft;
        }
        int IconHeight=IconWidth;
         int IconRightMarign=(int)(screenWidth*4.0/7-1.0*screenHeigt/10-5.0*IconWidth)/7;
         if(IconRightMarign<0)
             IconRightMarign=0;
        Log.i(TAG, "initImageIconPositionAndSize: "+IconRightMarign);
        LinearLayout.LayoutParams searchMusicButtonLayoutParams=(LinearLayout.LayoutParams)searchMusicButton.getLayoutParams();
        searchMusicButtonLayoutParams.width=IconWidth;
        searchMusicButtonLayoutParams.height=IconHeight;
        searchMusicButtonLayoutParams.rightMargin=IconRightMarign;
        searchMusicButton.setLayoutParams(searchMusicButtonLayoutParams);

        LinearLayout.LayoutParams mLocalMusicButtonLayoutParams=(LinearLayout.LayoutParams)mLocalMusicButton.getLayoutParams();
        mLocalMusicButtonLayoutParams.width=IconWidth;
        mLocalMusicButtonLayoutParams.height=IconHeight;
        mLocalMusicButtonLayoutParams.rightMargin=IconRightMarign;
        mLocalMusicButton.setLayoutParams(mLocalMusicButtonLayoutParams);

        LinearLayout.LayoutParams msongListButtonLayoutParams=(LinearLayout.LayoutParams)msongListButton.getLayoutParams();
        msongListButtonLayoutParams.width=IconWidth;
        msongListButtonLayoutParams.height=IconHeight;
        msongListButtonLayoutParams.rightMargin=IconRightMarign;
        msongListButton.setLayoutParams(msongListButtonLayoutParams);

        LinearLayout.LayoutParams monlineMusicButtonLayoutParams=(LinearLayout.LayoutParams)monlineMusicButton.getLayoutParams();
        monlineMusicButtonLayoutParams.width=IconWidth;
        monlineMusicButtonLayoutParams.height=IconHeight;
        monlineMusicButtonLayoutParams.rightMargin=IconRightMarign;
        monlineMusicButton.setLayoutParams(monlineMusicButtonLayoutParams);

        LinearLayout.LayoutParams msettingButtonLayoutParams=(LinearLayout.LayoutParams)msettingButton.getLayoutParams();
        msettingButtonLayoutParams.width=IconWidth;
        msettingButtonLayoutParams.height=IconHeight;
        msettingButtonLayoutParams.rightMargin=marginLeft;
        msettingButton.setLayoutParams(msettingButtonLayoutParams);
        //设置标题字体大小
        musicTitle.setTextSize(DensityUtil.px2sp(this,marginLeft/2));
        LrcView.defaultTextSize=DensityUtil.px2sp(this,marginLeft);//设置默认的歌词大小
        LrcView.defaultDividerHeight=marginLeft/2;
        // Log.i(TAG, (musicTitle!=null)+"initImageIconPositionAndSize: "+"screentHeight:"+screenHeigt+"/screenWidth:"+screenWidth+"//"+(int)(1.0*screenWidth*3/7));

    }
    private void initViews() {
        mLocalMusicButton = (ImageButton) this.findViewById(R.id.music);
        msongListButton = (ImageButton) this.findViewById(R.id.SongList);
        monlineMusicButton =(ImageButton)this.findViewById(R.id.onlineMusic);
        msettingButton = (ImageButton) this.findViewById(R.id.set);
        searchMusicButton = (ImageButton)this.findViewById(R.id.search);
        musicTitle = (TextView)this.findViewById(R.id.musicTitle);
        tabLinearLayout = (LinearLayout)this.findViewById(R.id.tabLinearLayout);
        TitleBarLinearLayout=(LinearLayout)this.findViewById(R.id.TitleBar);
        drawerLayout = (DrawerLayout) findViewById(R.id.fd);
        mLvLeftMenu = (ListView) findViewById(R.id.id_lv_left_menu);

        setUpDrawer();
        localMusicFragment.musicTitle=musicTitle;
        MusicService.musicTitle=musicTitle;
        localMusicFragment.activity=MainActivity.this;

    }
    private void setUpDrawer() {
        LayoutInflater inflater = LayoutInflater.from(this);
        mLvLeftMenu.addHeaderView(inflater.inflate(R.layout.nav_header_main, mLvLeftMenu, false));
        initData();
        adapter = new ContentAdapter(this, list);
        mLvLeftMenu.setAdapter(adapter);
       mLvLeftMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1:
                        drawerLayout.closeDrawers();
                        break;
                    case 2:
                        CardPickerDialog dialog = new CardPickerDialog();
                        dialog.setClickListener(MainActivity.this);
                        dialog.show(getSupportFragmentManager(), "theme");
                        drawerLayout.closeDrawers();

                        break;
                    case 3:
                        TimingFragment fragment3 = new TimingFragment();
                        fragment3.show(getSupportFragmentManager(), "timing");
                        drawerLayout.closeDrawers();

                        break;
                    case 4:

                    case 5:
                        break;


                }
            }
        });
    }
    private void initData() {
        list = new ArrayList<ContentModel>();

        list.add(new ContentModel(R.mipmap.topmenu_icn_night, "夜间模式", 1));
        list.add(new ContentModel(R.mipmap.topmenu_icn_skin, "主题换肤", 2));
        list.add(new ContentModel(R.mipmap.topmenu_icn_time, "定时关闭音乐", 3));
        list.add(new ContentModel(R.mipmap.topmenu_icn_vip, "下载歌曲品质", 4));
        list.add(new ContentModel(R.mipmap.topmenu_icn_exit, "退出", 5));

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
              drawerLayout.openDrawer(Gravity.START);
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
        //当应用关闭之后
        super.onDestroy();
    }
    @Override
    public void onConfirm(int currentTheme) {
        if (ThemeHelper.getTheme(MainActivity.this) != currentTheme) {
            ThemeHelper.setTheme(MainActivity.this, currentTheme);


        }

    }

}
