package com.example.mrxie.music.activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.mrxie.music.services.MusicService;


public class EqualizerActivity extends AppCompatActivity {
    private static final String TAG = "AudioFxDemo";
    public static LinearLayout mLinearLayout;
    private TextView mStatusTextView;
    private AudioManager audioManager;
    private MusicService musicService;



    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        applypermission();


        // 音量控制
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mStatusTextView = new TextView(this);
        mLinearLayout = new LinearLayout(this);
        musicService = new MusicService();
        audioManager= (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        mLinearLayout.setOrientation(LinearLayout.VERTICAL);
        mLinearLayout.addView(mStatusTextView);
        //动态添加按钮到布局
        setContentView(mLinearLayout);

        Log.d(TAG,
                "MediaPlayer audio session ID: "
                        + musicService.mediaPlayer.getAudioSessionId());
        // 设置频谱显示
        musicService.setupVisualizerFxAndUI();
        // 设置示波器显示
        musicService.setupEqualizerFxAndUI();

        // Make sure the visualizer is enabled only when you actually want to
        // receive data, and
        // when it makes sense to receive data.
        // 确保只有在您真正想要接收数据时才启用可视化工具
        //  什么时候接收数据是有意义的。
        musicService.mVisualizer.setEnabled(true);

        // When the stream ends, we don't need to collect any more data. We
        // don't do this in
        // setupVisualizerFxAndUI because we likely want to have more,
        // non-Visualizer related code
        // in this callback.
        // 当歌曲流结束时，我们不需要再收集任何数据。
        // 我们不在setupVisualizerFxAndUI中执行此操作，
        // 因为我们可能希望在此回调中包含更多非Visualizer相关代码。
//        musicService.mediaPlayer
//                .setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                    public void onCompletion(MediaPlayer mediaPlayer) {
//                        musicService.mVisualizer.setEnabled(false);
//                        mStatusTextView.setText("播放结束");
//                    }
//                });

        //musicService.mediaPlayer.start();//musicService.mediaPlayer.start();
        mStatusTextView.setText("正在播放中");


    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//
//        if (isFinishing() && musicService.mediaPlayer != null) {
//            musicService.mVisualizer.release();
//            musicService.mEqualizer.release();
//            musicService.mediaPlayer.release();
//            musicService.mediaPlayer = null;
//        }
//    }


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


}
