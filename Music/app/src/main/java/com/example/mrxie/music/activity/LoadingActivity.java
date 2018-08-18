package com.example.mrxie.music.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.example.mrxie.music.R;
import com.example.mrxie.music.Toast.OnlyOneToast;


public class LoadingActivity extends Activity {
    private AlertDialog alertDialog;//获取权限的对话框
    private String TAG="Music";
    private String[] permissions={Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};
    //延迟
    private static final long SPLASH_DELAY_MILLIS = 1600;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //无标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
      //全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_first_index);
        getPermission();//动态获取权限
    }

    @Override
    protected void onResume() {
//        //设置横屏
//        if(getRequestedOrientation()!= ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        }

        super.onResume();
    }

    //进入主界面
    private void gotoMainActivity(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent=new Intent(LoadingActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
                //切换活动时有动画效果
                LoadingActivity.this.overridePendingTransition(R.anim.in_form_right,R.anim.out_to_left);
            }
        },SPLASH_DELAY_MILLIS);

    }
    private void getPermission(){

        //动态获取权限
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){//当手机系统大于23时，才有必要判断权限是否获取
            // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
            if(ContextCompat.checkSelfPermission(LoadingActivity.this,permissions[1])!= PackageManager.PERMISSION_GRANTED)
            {
                // 如果没有授予该权限，就去提示用户请求
                showDialogTipUserRequestPermission();
                //  ActivityCompat.requestPermissions(MainActivity.this,new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            }else{
                gotoMainActivity();
            }
        }else{
            gotoMainActivity();
        }
    }
    //提示用户请求权限弹出框
    private void showDialogTipUserRequestPermission() {
        new AlertDialog.Builder(LoadingActivity.this).setTitle("读取权限不可用").setMessage("由于Audio需要获取本地音乐信息;\n否则，您将无法正常使用")
                .setPositiveButton("立即开启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(LoadingActivity.this,permissions,1);
                    }
                }).setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        }).setCancelable(false).show();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                    if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                        OnlyOneToast.makeText(this,"权限获取成功");
                        gotoMainActivity();
                    }else{
                        // 判断用户是否 点击了不再提醒。(检测该权限是否还可以申请)
                        boolean b=shouldShowRequestPermissionRationale(permissions[1]);
                        if(!b){
                            // 用户还是想用我的 APP 的
                            // 提示用户去应用设置界面手动开启权限
                            showDialogTipUserGoToAppSettting();
                        }else{
                            finish();
                        }

                    }
                }

                break;
            default:
                break;
        }
    }

    private void showDialogTipUserGoToAppSettting() {
        alertDialog=new AlertDialog.Builder(LoadingActivity.this).setTitle("读取权限不可用").setMessage("请在-应用设置-权限-中，允许Audio使用读取权限来获取数据")
                .setPositiveButton("立即开启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // 跳转到应用设置界面
                        goToAppSetting();
                    }
                }).setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                }).setCancelable(false).show();
    }
    // 跳转到当前应用的设置界面
    private void goToAppSetting() {
        Intent intent=new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri=Uri.fromParts("package",getPackageName(),null);
        intent.setData(uri);
        startActivityForResult(intent,23);//返回上个活动是请求码为23
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==23){
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                // 检查该权限是否已经获取
                if(ContextCompat.checkSelfPermission(LoadingActivity.this,permissions[1])!= PackageManager.PERMISSION_GRANTED){
                    showDialogTipUserGoToAppSettting();
                }else{
                    if(alertDialog!=null&&alertDialog.isShowing()){
                        alertDialog.dismiss();
                    }
                    OnlyOneToast.makeText(this,"权限获取成功");
                    gotoMainActivity();
                }
            }
        }
    }

}
