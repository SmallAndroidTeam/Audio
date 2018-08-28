package com.example.mrxie.music.broadcastReceiver;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.mrxie.music.R;
import com.example.mrxie.music.Toast.OnlyOneToast;
import com.example.mrxie.music.activity.LoadingActivity;
import com.example.mrxie.music.activity.MainActivity;
import com.github.mjdev.libaums.UsbMassStorageDevice;

import java.util.HashMap;
import java.util.Iterator;

/**
 * 静态广播，如果有悬浮窗的权限则一插入U盘且没启动App就会有一个弹出提示是否打开AUDIO播放U盘内的歌曲
 */
public class usbBroadCastReceiver extends BroadcastReceiver {
    public final  static String  USB_DEVICE_ATTACHED="android.hardware.usb.action.USB_DEVICE_ATTACHED";
    public final  static String USB_DEVICE_DETACHED="android.hardware.usb.action.USB_DEVICE_DETACHED";
    private final  static String TAG="Music";
    private UsbManager usbManager;
    private static AlertDialog alertDialog;
    private static CheckBox checkBox;//弹出框中的复选框
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        String action=intent.getAction();
        if(action.equals(USB_DEVICE_ATTACHED)){
            showUsbList(context);
        }else if(action.equals(USB_DEVICE_DETACHED)){//拔出设备
            if(alertDialog!=null&&alertDialog.isShowing()){
                alertDialog.cancel();
            }
        }
    }
    //获取U盘信息
    public void showUsbList(Context context){
        if(Build.VERSION.SDK_INT>=23){
            if(!Settings.canDrawOverlays(context)){//如果没有悬浮窗权限则没有弹窗弹出
                return;
            }
        }
        if(LoadingActivity.AppIsStart){//如果APP已经启动了，则没必要弹出框了
            return;
        }
        //读取U盘设备列表
        UsbMassStorageDevice[] storageDevices = UsbMassStorageDevice.getMassStorageDevices(context);
           if(storageDevices.length>0){
               showAlertDialog(context);
           }
    }
    private void showAlertDialog(final Context context){
        if(alertDialog==null){
            AlertDialog.Builder dialogBuiler=new AlertDialog.Builder(context).setCancelable(true);
            dialogBuiler.setTitle("Audio提示您");
            dialogBuiler.setMessage(R.string.alert_dialog_message);
            View view= LayoutInflater.from(context).inflate(R.layout.alert_dialog,null);
            checkBox=(CheckBox)view.findViewById(R.id.checkbox);
            final TextView okTextView=(TextView)view.findViewById(R.id.OK);
            final TextView cancelTextView=(TextView)view.findViewById(R.id.CANCEL);
            okTextView.setText("立即进入");
            cancelTextView.setText("取消");
            okTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                        Intent intent=new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_LAUNCHER);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        intent.setComponent(new ComponentName(context.getPackageName(),"com.example.mrxie.music.activity.MainActivity"));
                        context.startActivity(intent);

                    alertDialog.cancel();
                }
            });
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                  if(checkBox.isChecked()){
                   okTextView.setTextColor(context.getResources().getColor(R.color.dialog_unavailable));
                      okTextView.setClickable(false);
                  }else{
                      okTextView.setTextColor(context.getResources().getColor(R.color.dialog_available));
                      okTextView.setClickable(true);
                  }
                }
            });
            cancelTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
               alertDialog.cancel();
                }
            });
            dialogBuiler.setView(view);
            alertDialog=dialogBuiler.create();
            alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            alertDialog.show();
        }else{
          if(!checkBox.isChecked()){
              alertDialog.show();
          }
        }

    }
}
