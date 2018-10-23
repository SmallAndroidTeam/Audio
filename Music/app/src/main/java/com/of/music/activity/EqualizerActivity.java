package com.of.music.activity;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.of.music.Application.App;
import com.of.music.R;
import com.of.music.adapter.ModeAdapter;
import com.of.music.defineViewd.AutoLocateHorizontalView;
import com.of.music.defineViewd.VerticalSeekBar;
import com.of.music.services.MusicService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class EqualizerActivity extends AppCompatActivity {
    private int progress_pre=0;
    private RelativeLayout relativeLayout;
    private MusicService musicService;
    private static final String TAG = "AudioFxDemo";
    private Button button;
    private AudioManager audioManager;
    private android.media.audiofx.Equalizer mEqualizer;
    final String []bands={"band1","band2","band3","band4","band5"};
    private AutoLocateHorizontalView mContent;
    List<String> modeList;
    String[] modes = new String[]{"摇滚", "流行", "爵士", "打击", "古典", "默认","自定义"};
    String[] mbands = new String[]{"band1","band2","band3","band4","band5"};
    private ModeAdapter modeAdapter;
    private VerticalSeekBar verticalSeekBar1 =null;
    private VerticalSeekBar verticalSeekBar2 =null;
    private VerticalSeekBar verticalSeekBar3 =null;
    private VerticalSeekBar verticalSeekBar4 =null;
    private VerticalSeekBar verticalSeekBar5 =null;
    private VerticalSeekBar []verticalSeekBar = {verticalSeekBar1,verticalSeekBar2,verticalSeekBar3,verticalSeekBar4,verticalSeekBar5};
    private Switch mSwitch;
    private Bundle bundle = new Bundle();
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private SharedPreferences preferences;
    private int flag;
    private int []equalizerData = {0,0,0,0,0};//不初始化会空指针异常


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applypermission();
        sharedPreferences=getSharedPreferences("verticalseekbar",MODE_PRIVATE);
        editor=sharedPreferences.edit();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        audioManager= (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        setContentView(R.layout.activity_equalizer);
        musicService = new MusicService();
        setupEqualizer();
    }

    //权限申请
    public void applypermission(){
        if(Build.VERSION.SDK_INT>=23){
            //检查是否已经给了权限
            int checkpermission= ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.RECORD_AUDIO);
            if(checkpermission!= PackageManager.PERMISSION_GRANTED){//没有给权限
                Log.e("permission","动态申请");
                //参数分别是当前活动，权限字符串数组，requestcode
                ActivityCompat.requestPermissions(EqualizerActivity.this,new String[]{Manifest.permission.RECORD_AUDIO}, 1);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //grantResults数组与权限字符串数组对应，里面存放权限申请结果
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Toast.makeText(EqualizerActivity.this,"已授权",Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(EqualizerActivity.this,"拒绝授权",Toast.LENGTH_SHORT).show();
        }
    }



    public void setupEqualizer(){
        mEqualizer = new Equalizer(0,musicService.mediaPlayer.getAudioSessionId());// 以MediaPlayer的AudioSessionId创建Equalizer,相当于设置Equalizer负责控制该MediaPlayer
        mEqualizer.setEnabled(true);
        // 获取均衡控制器支持最小值和最大值
        final short minEQLevel = mEqualizer.getBandLevelRange()[0];
        final short maxEQLevel = mEqualizer.getBandLevelRange()[1];
        final short brands = mEqualizer.getNumberOfBands();
        relativeLayout = (RelativeLayout)findViewById(R.id.activityRoot);
        button = (Button)findViewById(R.id.button);
        button.setVisibility(View.INVISIBLE);
        verticalSeekBar[0]= (VerticalSeekBar)findViewById(R.id.vertical_Seekbar1);
        verticalSeekBar[1]= (VerticalSeekBar)findViewById(R.id.vertical_Seekbar2);
        verticalSeekBar[2]= (VerticalSeekBar)findViewById(R.id.vertical_Seekbar3);
        verticalSeekBar[3]= (VerticalSeekBar)findViewById(R.id.vertical_Seekbar4);
        verticalSeekBar[4]= (VerticalSeekBar)findViewById(R.id.vertical_Seekbar5);
        mSwitch = (Switch) findViewById(R.id.mSwitch);
        //滑动按钮
        mContent = (AutoLocateHorizontalView) findViewById(R.id.recyleview);
        modeList = new ArrayList<>();
        for (String mode : modes) {
            modeList.add(mode);
        }
        modeAdapter = new ModeAdapter(this, modeList);
        //将AutoLocateHorizontalView设置为HORIZONTAL（水平的）
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mContent.setLayoutManager(linearLayoutManager);
        mContent.setInitPos(sharedPreferences.getInt("position",0));
        mContent.setAdapter(modeAdapter);
        for(short i=0;i<brands;i++){
            final short brand = i;
            verticalSeekBar[i].setMax(maxEQLevel-minEQLevel);
            if(sharedPreferences==null){
                verticalSeekBar[i].setProgress(mEqualizer.getBandLevel(brand)-minEQLevel);
            }else {
                progress_pre=sharedPreferences.getInt(bands[i],0);
                verticalSeekBar[i].setProgress(progress_pre-minEQLevel);
            }
        }

        // 从SharedPreferences获取Switch的状态数据:
        preferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        if (preferences != null) {
            boolean switchOnOrOf = preferences.getBoolean("flag", false);
            mSwitch.setChecked(switchOnOrOf);
            if (switchOnOrOf==true){
                flag=1;
                mContent.setBackgroundColor(Color.parseColor("#efe7e7"));
                relativeLayout.setBackgroundColor(Color.WHITE);
            }else {
                flag=0;
                mContent.setBackgroundColor(Color.GRAY);
                relativeLayout.setBackgroundColor(Color.GRAY);
            }
        }


        //开关点击事件
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    SharedPreferences preferences = getSharedPreferences("user", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor1 = preferences.edit();
                    editor1.putBoolean("flag", true);
                    editor1.commit();
                    relativeLayout.setBackgroundColor(Color.WHITE);
                    for(short i=0;i<brands;i++){
                        final short brand = i;
                        verticalSeekBar[i].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                                mEqualizer.setBandLevel(brand,(short)(i+minEQLevel));
                                editor.putInt(bands[brand],(int) (mEqualizer.getBandLevel(brand)));
                                editor.apply();
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {

                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {
                                Toast.makeText(App.sContext, "垂直seekBar", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }

                    //已选中的位置 改变的点击事件
                    mContent.setOnSelectedPositionChangedListener(new AutoLocateHorizontalView.OnSelectedPositionChangedListener() {
                        @Override
                        public void selectedPositionChanged(int pos) {
                            switch (pos){
                                case 0:
                                    button.setVisibility(View.INVISIBLE);
                                    editor.clear();
                                    editor.putInt("band1",1500);
                                    editor.putInt("band2",-300);
                                    editor.putInt("band3",500);
                                    editor.putInt("band4",-300);
                                    editor.putInt("band5",1500);
                                    editor.putInt("position",0);
                                    editor.apply();
                                    mEqualizer.setBandLevel((short)0,(short) (3000+minEQLevel));
                                    mEqualizer.setBandLevel((short)1,(short) (1200+minEQLevel));
                                    mEqualizer.setBandLevel((short)2,(short) (2000+minEQLevel));
                                    mEqualizer.setBandLevel((short)3,(short) (1200+minEQLevel));
                                    mEqualizer.setBandLevel((short)4,(short) (3000+minEQLevel));
                                    //verticalSeekBar[0].setProgress(3000);
                                    verticalSeekBar[0].setProgress((sharedPreferences.getInt("band1",0))-minEQLevel);
                                    //verticalSeekBar[1].setProgress(1200);
                                    verticalSeekBar[1].setProgress((sharedPreferences.getInt("band2",0))-minEQLevel);
                                    //verticalSeekBar[2].setProgress(2000);
                                    verticalSeekBar[2].setProgress((sharedPreferences.getInt("band3",0))-minEQLevel);
                                    //verticalSeekBar[3].setProgress(1200);
                                    verticalSeekBar[3].setProgress((sharedPreferences.getInt("band4",0))-minEQLevel);
                                    //verticalSeekBar[4].setProgress(3000);
                                    verticalSeekBar[4].setProgress((sharedPreferences.getInt("band5",0))-minEQLevel);
                                    break;
                                case 1:
                                    button.setVisibility(View.INVISIBLE);
                                    editor.clear();
                                    editor.putInt("band1",1200);
                                    editor.putInt("band2",-600);
                                    editor.putInt("band3",1200);
                                    editor.putInt("band4",600);
                                    editor.putInt("band5",600);
                                    editor.putInt("position",1);
                                    editor.apply();
                                    mEqualizer.setBandLevel((short)0,(short) (2700+minEQLevel));
                                    mEqualizer.setBandLevel((short)1,(short) (900+minEQLevel));
                                    mEqualizer.setBandLevel((short)2,(short) (2700+minEQLevel));
                                    mEqualizer.setBandLevel((short)3,(short) (2100+minEQLevel));
                                    mEqualizer.setBandLevel((short)4,(short) (2100+minEQLevel));
                                    verticalSeekBar[0].setProgress(2700);
                                    verticalSeekBar[1].setProgress(900);
                                    verticalSeekBar[2].setProgress(2700);
                                    verticalSeekBar[3].setProgress(2100);
                                    verticalSeekBar[4].setProgress(2100);
                                    break;
                                case 2:
                                    button.setVisibility(View.INVISIBLE);
                                    editor.clear();
                                    editor.putInt("band1",600);
                                    editor.putInt("band2",0);
                                    editor.putInt("band3",1200);
                                    editor.putInt("band4",0);
                                    editor.putInt("band5",600);
                                    editor.putInt("position",2);
                                    editor.apply();
                                    mEqualizer.setBandLevel((short)0,(short) (2100+minEQLevel));
                                    mEqualizer.setBandLevel((short)1,(short) (1500+minEQLevel));
                                    mEqualizer.setBandLevel((short)2,(short) (2700+minEQLevel));
                                    mEqualizer.setBandLevel((short)3,(short) (1500+minEQLevel));
                                    mEqualizer.setBandLevel((short)4,(short) (2100+minEQLevel));
                                    verticalSeekBar[0].setProgress(2100);
                                    verticalSeekBar[1].setProgress(1500);
                                    verticalSeekBar[2].setProgress(2700);
                                    verticalSeekBar[3].setProgress(1500);
                                    verticalSeekBar[4].setProgress(2100);
                                    break;
                                case 3:
                                    button.setVisibility(View.INVISIBLE);
                                    editor.clear();
                                    editor.putInt("band1",-1200);
                                    editor.putInt("band2",600);
                                    editor.putInt("band3",600);
                                    editor.putInt("band4",-600);
                                    editor.putInt("band5",-1200);
                                    editor.putInt("position",3);
                                    editor.apply();
                                    mEqualizer.setBandLevel((short)0,(short) (300+minEQLevel));
                                    mEqualizer.setBandLevel((short)1,(short) (2100+minEQLevel));
                                    mEqualizer.setBandLevel((short)2,(short) (2100+minEQLevel));
                                    mEqualizer.setBandLevel((short)3,(short) (900+minEQLevel));
                                    mEqualizer.setBandLevel((short)4,(short) (300+minEQLevel));
                                    verticalSeekBar[0].setProgress(300);
                                    verticalSeekBar[1].setProgress(2100);
                                    verticalSeekBar[2].setProgress(2100);
                                    verticalSeekBar[3].setProgress(900);
                                    verticalSeekBar[4].setProgress(300);
                                    break;
                                case 4:
                                    button.setVisibility(View.INVISIBLE);
                                    editor.clear();
                                    editor.putInt("band1",600);
                                    editor.putInt("band2",600);
                                    editor.putInt("band3",0);
                                    editor.putInt("band4",900);
                                    editor.putInt("band5",1200);
                                    editor.putInt("position",4);
                                    editor.apply();
                                    mEqualizer.setBandLevel((short)0,(short) (2100+minEQLevel));
                                    mEqualizer.setBandLevel((short)1,(short) (2100+minEQLevel));
                                    mEqualizer.setBandLevel((short)2,(short) (1500+minEQLevel));
                                    mEqualizer.setBandLevel((short)3,(short) (2400+minEQLevel));
                                    mEqualizer.setBandLevel((short)4,(short) (2700+minEQLevel));
                                    verticalSeekBar[0].setProgress(2100);
                                    verticalSeekBar[1].setProgress(2100);
                                    verticalSeekBar[2].setProgress(1500);
                                    verticalSeekBar[3].setProgress(2400);
                                    verticalSeekBar[4].setProgress(2700);
                                    break;
                                case 5:
                                    button.setVisibility(View.INVISIBLE);
                                    editor.clear();
                                    editor.putInt("band1",-600);
                                    editor.putInt("band2",-300);
                                    editor.putInt("band3",300);
                                    editor.putInt("band4",600);
                                    editor.putInt("band5",900);
                                    editor.putInt("position",5);
                                    editor.apply();
                                    mEqualizer.setBandLevel((short)0,(short) (900+minEQLevel));
                                    mEqualizer.setBandLevel((short)1,(short) (1200+minEQLevel));
                                    mEqualizer.setBandLevel((short)2,(short) (1800+minEQLevel));
                                    mEqualizer.setBandLevel((short)3,(short) (2100+minEQLevel));
                                    mEqualizer.setBandLevel((short)4,(short) (2400+minEQLevel));
                                    verticalSeekBar[0].setProgress(900);
                                    verticalSeekBar[1].setProgress(1200);
                                    verticalSeekBar[2].setProgress(1800);
                                    verticalSeekBar[3].setProgress(2100);
                                    verticalSeekBar[4].setProgress(2400);
                                    break;
                                case 6:
                                    button.setVisibility(View.VISIBLE);
                                    for (short j=0;j<5;++j){
                                        final short band = j;
                                        verticalSeekBar[j].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                            @Override
                                            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                                                mEqualizer.setBandLevel(band,(short) (i + minEQLevel));
                                                equalizerData[band] = i-1500;
                                                //editor.putInt(mbands[band],mEqualizer.getBandLevel(band));
                                            }

                                            @Override
                                            public void onStartTrackingTouch(SeekBar seekBar) {

                                            }

                                            @Override
                                            public void onStopTrackingTouch(SeekBar seekBar) {

                                            }
                                        });
                                    }
                                    button.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            editor.clear();
                                            editor.putInt("band1",equalizerData[0]);
                                            editor.putInt("band2",equalizerData[1]);
                                            editor.putInt("band3",equalizerData[2]);
                                            editor.putInt("band4",equalizerData[3]);
                                            editor.putInt("band5",equalizerData[4]);
                                            button.setVisibility(View.INVISIBLE);
                                            editor.putInt("position",6);
                                            editor.apply();
                                        }
                                    });

                                    Log.i(TAG, "onProgressChanged: band5:progress + minEQLevel:     "+sharedPreferences.getInt("band5",0));
                                    Toast.makeText(App.sContext, "自定义", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }
                    });
                    mContent.setBackgroundColor(Color.parseColor("#efe7e7"));
                    Toast.makeText(App.sContext, "开", Toast.LENGTH_SHORT).show();
                } else {
                    SharedPreferences preferences = getSharedPreferences("user", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor1 = preferences.edit();
                    editor1.putBoolean("flag", false);
                    editor1.commit();
                    relativeLayout.setBackgroundColor(Color.GRAY);
                    for(short i=0;i<brands;i++){
                        final short brand = i;
                        verticalSeekBar[i].setMax(maxEQLevel-minEQLevel);


                        if(sharedPreferences==null){
                            verticalSeekBar[i].setProgress(mEqualizer.getBandLevel(brand)-minEQLevel);
                        }else {
                            progress_pre=sharedPreferences.getInt(bands[i],0);
                            verticalSeekBar[i].setProgress(progress_pre-minEQLevel);
                        }
                        verticalSeekBar[i].setOnSeekBarChangeListener(null);

                    }

                    mContent.setOnSelectedPositionChangedListener(null);

                    mContent.setBackgroundColor(Color.GRAY);
                    Toast.makeText(App.sContext, "关", Toast.LENGTH_SHORT).show();
                }
            }
        });

//已选中的位置 改变的点击事件
        mContent.setOnSelectedPositionChangedListener(new AutoLocateHorizontalView.OnSelectedPositionChangedListener() {
            @Override
            public void selectedPositionChanged(int pos) {
                if (flag==1){
                    switch (pos){
                        case 0:
                            button.setVisibility(View.INVISIBLE);
                            editor.clear();
                            editor.putInt("band1",1500);
                            editor.putInt("band2",-300);
                            editor.putInt("band3",500);
                            editor.putInt("band4",-300);
                            editor.putInt("band5",1500);
                            editor.putInt("position",0);
                            editor.apply();
                            mEqualizer.setBandLevel((short)0,(short) (3000+minEQLevel));
                            mEqualizer.setBandLevel((short)1,(short) (1200+minEQLevel));
                            mEqualizer.setBandLevel((short)2,(short) (2000+minEQLevel));
                            mEqualizer.setBandLevel((short)3,(short) (1200+minEQLevel));
                            mEqualizer.setBandLevel((short)4,(short) (3000+minEQLevel));
                            verticalSeekBar[0].setProgress(3000);
                            verticalSeekBar[1].setProgress(1200);
                            verticalSeekBar[2].setProgress(2000);
                            verticalSeekBar[3].setProgress(1200);
                            verticalSeekBar[4].setProgress(3000);
                            break;
                        case 1:
                            button.setVisibility(View.INVISIBLE);
                            editor.clear();
                            editor.putInt("band1",1200);
                            editor.putInt("band2",-600);
                            editor.putInt("band3",1200);
                            editor.putInt("band4",600);
                            editor.putInt("band5",600);
                            editor.putInt("position",1);
                            editor.apply();
                            mEqualizer.setBandLevel((short)0,(short) (2700+minEQLevel));
                            mEqualizer.setBandLevel((short)1,(short) (900+minEQLevel));
                            mEqualizer.setBandLevel((short)2,(short) (2700+minEQLevel));
                            mEqualizer.setBandLevel((short)3,(short) (2100+minEQLevel));
                            mEqualizer.setBandLevel((short)4,(short) (2100+minEQLevel));
                            verticalSeekBar[0].setProgress(2700);
                            verticalSeekBar[1].setProgress(900);
                            verticalSeekBar[2].setProgress(2700);
                            verticalSeekBar[3].setProgress(2100);
                            verticalSeekBar[4].setProgress(2100);
                            break;
                        case 2:
                            button.setVisibility(View.INVISIBLE);
                            editor.clear();
                            editor.putInt("band1",600);
                            editor.putInt("band2",0);
                            editor.putInt("band3",1200);
                            editor.putInt("band4",0);
                            editor.putInt("band5",600);
                            editor.putInt("position",2);
                            editor.apply();
                            mEqualizer.setBandLevel((short)0,(short) (2100+minEQLevel));
                            mEqualizer.setBandLevel((short)1,(short) (1500+minEQLevel));
                            mEqualizer.setBandLevel((short)2,(short) (2700+minEQLevel));
                            mEqualizer.setBandLevel((short)3,(short) (1500+minEQLevel));
                            mEqualizer.setBandLevel((short)4,(short) (2100+minEQLevel));
                            verticalSeekBar[0].setProgress(2100);
                            verticalSeekBar[1].setProgress(1500);
                            verticalSeekBar[2].setProgress(2700);
                            verticalSeekBar[3].setProgress(1500);
                            verticalSeekBar[4].setProgress(2100);
                            break;
                        case 3:
                            button.setVisibility(View.INVISIBLE);
                            editor.clear();
                            editor.putInt("band1",-1200);
                            editor.putInt("band2",600);
                            editor.putInt("band3",600);
                            editor.putInt("band4",-600);
                            editor.putInt("band5",-1200);
                            editor.putInt("position",3);
                            editor.apply();
                            mEqualizer.setBandLevel((short)0,(short) (300+minEQLevel));
                            mEqualizer.setBandLevel((short)1,(short) (2100+minEQLevel));
                            mEqualizer.setBandLevel((short)2,(short) (2100+minEQLevel));
                            mEqualizer.setBandLevel((short)3,(short) (900+minEQLevel));
                            mEqualizer.setBandLevel((short)4,(short) (300+minEQLevel));
                            verticalSeekBar[0].setProgress(300);
                            verticalSeekBar[1].setProgress(2100);
                            verticalSeekBar[2].setProgress(2100);
                            verticalSeekBar[3].setProgress(900);
                            verticalSeekBar[4].setProgress(300);
                            break;
                        case 4:
                            button.setVisibility(View.INVISIBLE);
                            editor.clear();
                            editor.putInt("band1",600);
                            editor.putInt("band2",600);
                            editor.putInt("band3",0);
                            editor.putInt("band4",900);
                            editor.putInt("band5",1200);
                            editor.putInt("position",4);
                            editor.apply();
                            mEqualizer.setBandLevel((short)0,(short) (2100+minEQLevel));
                            mEqualizer.setBandLevel((short)1,(short) (2100+minEQLevel));
                            mEqualizer.setBandLevel((short)2,(short) (1500+minEQLevel));
                            mEqualizer.setBandLevel((short)3,(short) (2400+minEQLevel));
                            mEqualizer.setBandLevel((short)4,(short) (2700+minEQLevel));
                            verticalSeekBar[0].setProgress(2100);
                            verticalSeekBar[1].setProgress(2100);
                            verticalSeekBar[2].setProgress(1500);
                            verticalSeekBar[3].setProgress(2400);
                            verticalSeekBar[4].setProgress(2700);
                            break;
                        case 5:
                            button.setVisibility(View.INVISIBLE);
                            editor.clear();
                            editor.putInt("band1",-600);
                            editor.putInt("band2",-300);
                            editor.putInt("band3",300);
                            editor.putInt("band4",600);
                            editor.putInt("band5",900);
                            editor.putInt("position",5);
                            editor.apply();
                            mEqualizer.setBandLevel((short)0,(short) (900+minEQLevel));
                            mEqualizer.setBandLevel((short)1,(short) (1200+minEQLevel));
                            mEqualizer.setBandLevel((short)2,(short) (1800+minEQLevel));
                            mEqualizer.setBandLevel((short)3,(short) (2100+minEQLevel));
                            mEqualizer.setBandLevel((short)4,(short) (2400+minEQLevel));
                            verticalSeekBar[0].setProgress(900);
                            verticalSeekBar[1].setProgress(1200);
                            verticalSeekBar[2].setProgress(1800);
                            verticalSeekBar[3].setProgress(2100);
                            verticalSeekBar[4].setProgress(2400);
                            break;
                        case 6:
                            button.setVisibility(View.VISIBLE);
                            for (short j=0;j<5;++j){
                                final short band = j;
                                verticalSeekBar[j].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                    @Override
                                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                                        mEqualizer.setBandLevel(band,(short) (i + minEQLevel));
                                        equalizerData[band] = i-1500;
                                        //editor.putInt(mbands[band],mEqualizer.getBandLevel(band));
                                    }

                                    @Override
                                    public void onStartTrackingTouch(SeekBar seekBar) {

                                    }

                                    @Override
                                    public void onStopTrackingTouch(SeekBar seekBar) {

                                    }
                                });
                            }
                            button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    editor.clear();
                                    editor.putInt("band1",equalizerData[0]);
                                    editor.putInt("band2",equalizerData[1]);
                                    editor.putInt("band3",equalizerData[2]);
                                    editor.putInt("band4",equalizerData[3]);
                                    editor.putInt("band5",equalizerData[4]);
                                    button.setVisibility(View.INVISIBLE);
                                    editor.putInt("position",6);
                                    editor.apply();
                                }
                            });

                            Log.i(TAG, "onProgressChanged: band5:progress + minEQLevel:     "+sharedPreferences.getInt("band5",0));
                            Toast.makeText(App.sContext, "自定义", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }

            }
        });


    }

}
