package com.of.music.activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.util.Log;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.of.music.R;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.of.music.Toast.OnlyOneToast;
import com.of.music.adapter.ContentAdapter;
import com.of.music.adapter.ContentModel;
import com.of.music.convertPXAndDP.DensityUtil;
import com.of.music.fragment.LocalMusicFragment;
import com.of.music.fragment.TimingFragment;
import com.of.music.fragment.fragmentList.TabListFragment;
import com.of.music.fragment.fragmentNet.OnlineMusicFragment;
import com.of.music.fragment.fragmentSearch.SearchFragment;
import com.of.music.fragment.fragmentSettings.SettingFragment;
import com.of.music.fragment.fragmentSettings.SourcequalityFragment;
import com.of.music.services.MusicService;
import com.of.music.ui.LrcView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static java.lang.System.exit;

public class MainActivity extends FragmentActivity implements View.OnClickListener{
    private String TAG="Music";
    private ImageButton msongListButton;
    private ImageButton monlineMusicButton;
    private ImageButton msettingButton;
    private static ImageButton mLocalMusicButton;
    private Fragment mlocalMusicFragment,msongListFragment,monlineMusicFragment,msettingFragment,searchMusicFragment;
    private TextView musicTitle;
    private ImageButton searchMusicButton;
    private DrawerLayout drawerLayout;
    private ListView mLvLeftMenu;
    private long time=0;
    private LinearLayout tabLinearLayout;
    private LinearLayout TitleBarLinearLayout;
    private static UsbBroadcastReceiver usbBroadcastReceiver;//检测USB的广播
    public final  static String  USB_DEVICE_ATTACHED="android.hardware.usb.action.USB_DEVICE_ATTACHED";
    public final  static String USB_DEVICE_DETACHED="android.hardware.usb.action.USB_DEVICE_DETACHED";
    public final static String ACTION_USB_PERMISSION="com.android.example.USB_PERMISSION";
    public static final String ACTION_VOLUME_STATE_CHANGED = "android.os.storage.action.VOLUME_STATE_CHANGED";
    public static final String EXTRA_VOLUME_ID = "android.os.storage.extra.VOLUME_ID";
    public static final String EXTRA_VOLUME_STATE = "android.os.storage.extra.VOLUME_STATE";
    private   UsbManager usbManager;
    private  static AlertDialog alertDialog;
    private static boolean isLoadComplete=false;//判断MainActivity是否加载完成了一次
    public static  UsbMassStorageDevice[] storageDevices;//当前U盘列表
    private ContentAdapter adapter;
    private List<ContentModel> list;
    private static int Selection_Theme=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);
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
        usbBroadcastReceiver = new UsbBroadcastReceiver();
        usbManager = (UsbManager) this.getSystemService(Context.USB_SERVICE);
        //动态注册事件
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(USB_DEVICE_ATTACHED);
        intentFilter.addAction(USB_DEVICE_DETACHED);
        intentFilter.addAction(ACTION_USB_PERMISSION);
        registerReceiver(usbBroadcastReceiver,intentFilter);
    }

    public static ImageButton getmLocalMusicButton() {
        return mLocalMusicButton;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus){
            initImageIconPositionAndSize();//根据屏幕的宽高来初始化控件的位置和大小
            if(!isLoadComplete){
                usbBroadcastReceiver.showUsbList(this);
                isLoadComplete=true;
            }
        }
    }

    //加载完
    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * 检测usb的广播
     */
    class UsbBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO: This method is called when the BroadcastReceiver is receiving
            String action=intent.getAction();
            Log.i(TAG, "onReceive: /////"+action);
            if(action.equals(USB_DEVICE_ATTACHED)){
                OnlyOneToast.makeText(context,"设备插入");
                showUsbList(context);
            }else if(action.equals(USB_DEVICE_DETACHED)) {
                OnlyOneToast.makeText(context, "设备拔出");
                if (alertDialog != null && alertDialog.isShowing()) {
                    alertDialog.cancel();
                }
            }
            else if(action.equals(ACTION_USB_PERMISSION)){//获取usb的权限的广播
                synchronized (this){
                    UsbDevice usbDevice=intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if(usbDevice!=null){
                        if(intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED,false)){
                            //OnlyOneToast.makeText(context,usbDevice.getDeviceName()+":获取权限成功");
                            Intent intent1=new Intent();
                            intent1.setAction(Intent.ACTION_MAIN);
                            intent1.addCategory(Intent.CATEGORY_LAUNCHER);
                            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                            intent1.setComponent(new ComponentName(getPackageName(),"com.of.music.activity.MainActivity"));
                            startActivity(intent1);
                            readDevice(usbDevice);
                            Log.i(TAG, "onReceive:获取权限成功 ");
                        }else{
                            //OnlyOneToast.makeText(context,usbDevice.getDeviceName()+":获取权限失败");
                            Log.i(TAG, "onReceive:获取权限失败");
                        }
                    }
                }
            }
        }


        //        //读取设备列表
//        public void readDeviceList(Context context){
//            storageDevices = UsbMassStorageDevice.getMassStorageDevices(MainActivity.this);
//            Log.i(TAG, "readDeviceList: "+storageDevices.length);
//            String[] result=null;
//            if(storageDevices.length==0){
//                return;
//            }
//
//             for(UsbMassStorageDevice device:storageDevices){
//                //读取该设备是否有权限
//                if(usbManager.hasPermission(device.getUsbDevice())){
//                    //设备可以还没有挂载上，延迟一秒
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                     readDevice(device);
//                }else{
//                   getPermission(context,device.getUsbDevice());//没有权限进行申请
//                }
//            }
//            for (UsbMassStorageDevice s:storageDevices) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                    s.getPartitions().stream().close();
//                }
//            }
//        }
//
//        private void readDevice(UsbMassStorageDevice device){
//                if(device==null)
//                {
//                    return;
//                }
//            try {
//                device.init();//初始化
//              //获取分区
//                List<Partition> partitions=device.getPartitions();
//                if(partitions.size()==0){
//                   // OnlyOneToast.makeText(MainActivity.this,"错误:读取分区失败");
//                    Log.i(TAG, "readDevice:错误:读取分区失败 ");
//                    return;
//                }
//                //仅使用第一分区
//                com.github.mjdev.libaums.fs.FileSystem fileSystem=partitions.get(0).getFileSystem();
//                UsbFile root=fileSystem.getRootDirectory();//设置当前文件对象为根目录
//                readFile(root);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        private void readFile(UsbFile root) {
//            ArrayList<UsbFile> usbFiles=new ArrayList<>();
//            try{
//                for(UsbFile file: root.listFiles()){
//                    usbFiles.add(file);
//                }
//                Log.i(TAG, "readFile: "+usbFiles.size());
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//        }
//        private UsbMassStorageDevice getUsbMass(UsbDevice usbDevice){
//            for(UsbMassStorageDevice device:storageDevices){
//                if(device.getUsbDevice().equals(usbDevice)){
//                    return device;
//                }
//            }
//            return null;
//        }
        //获取U盘信息
        public void showUsbList(Context context){
            HashMap<String,UsbDevice> deviceHashMap= usbManager.getDeviceList();//获取设备列表
            if(deviceHashMap==null||deviceHashMap.size()==0){
                return;
            }
            Iterator<UsbDevice> deviceIterator=deviceHashMap.values().iterator();
            StringBuilder stringBuilder=new StringBuilder();
            while (deviceIterator.hasNext()){
                UsbDevice usbDevice=deviceIterator.next();
                stringBuilder.append("DeviceName="+usbDevice.getDeviceName()+"\n");
                stringBuilder.append("DeviceId="+usbDevice.getDeviceId()+"\n");
                stringBuilder.append("VendorId="+usbDevice.getVendorId()+"\n");
                stringBuilder.append("ProductId="+usbDevice.getProductId()+"\n");
                stringBuilder.append("DeviceClass="+usbDevice.getDeviceClass()+"\n");
                int deviceClass=usbDevice.getDeviceClass();
                if(deviceClass==0){
                    UsbInterface usbInterface=usbDevice.getInterface(0);
                    int InterfaceClass=usbInterface.getInterfaceClass();
                    stringBuilder.append("device Class 为0-------------\n");
                    stringBuilder.append("Interface.describeContents()="+usbInterface.describeContents()+"\n");
                    stringBuilder.append("Interface.getEndpointCount()="+usbInterface.getEndpointCount()+"\n");
                    stringBuilder.append("Interface.getId()="+usbInterface.getId()+"\n");
                    stringBuilder.append("Interface.getInterfaceClass()="+usbInterface.getInterfaceClass()+"\n");

                    if(usbInterface.getInterfaceClass()==8){
                        stringBuilder.append("此设备是U盘\n");
                        //OnlyOneToast.makeText(context,"此设备是U盘,");
                        getPermission(context,usbDevice);//获取权限
                    }else if(usbInterface.getInterfaceClass()==255){
                        stringBuilder.append("此设备是手机\n");
                        // OnlyOneToast.makeText(context,"此设备是手机");
                    }else if(usbInterface.getInterfaceClass()==3){
                        stringBuilder.append("此设备是鼠标或者键盘\n");
                        // OnlyOneToast.makeText(context,"此设备是鼠标或者键盘");
                    }else{
                        stringBuilder.append("其他设备\n");
                        // OnlyOneToast.makeText(context,"此设备是其他设备");
                    }

                }
            }
            Log.i(TAG, "showUsbList: "+stringBuilder);
        }

        //读取U盘信息
        public void readDevice(final UsbDevice usbdevice) {
            synchronized (this){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        UsbInterface usbInterface=usbdevice.getInterface(0);
                        UsbEndpoint inEndpoint=usbInterface.getEndpoint(0);//输人端
                        UsbEndpoint outEndpoint=usbInterface.getEndpoint(1);//输出端
                        UsbDeviceConnection connection=usbManager.openDevice(usbdevice);
                        connection.claimInterface(usbInterface,true);//建立连接与接口之间的关系

                        String sendStringMsg="0x88";
                        byte[] sendBytes=sendStringMsg.getBytes();
                        int out=connection.bulkTransfer(outEndpoint,sendBytes,sendBytes.length,3000);
                        Log.i(TAG, "发送:"+out+"#"+sendStringMsg+"#"+sendBytes);
                        byte[] receiveMsgBytes=new byte[32];
                        int in=connection.bulkTransfer(inEndpoint,receiveMsgBytes,receiveMsgBytes.length,3000);
                        String receiveMsgString=receiveMsgBytes.toString();
                        Log.i(TAG, "应答:"+in+"#"+receiveMsgString+"#"+receiveMsgBytes);

                    }
                }).start();
            }
        }
        public  void  getPermission(final Context context, final UsbDevice usbDevice)//获取权限
        {
            if(usbManager.hasPermission(usbDevice)){//如果有读取权限就不执行
                Log.i(TAG, "getPermission: 有读取权限");
                readDevice(usbDevice);
                return;
            }
            Log.i(TAG, "getPermission: 没有读取权限");
            synchronized (this){
                if(alertDialog==null){
                    AlertDialog.Builder dialoguilder=new AlertDialog.Builder(context).setTitle("U盘读取权限不可用").setMessage("由于Audio需要读取U盘信息;\n否则，无法加载U盘里面的歌曲")
                            .setCancelable(false).setPositiveButton("立即开启", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    PendingIntent permissionIntent=PendingIntent.getBroadcast(context,1,new Intent(ACTION_USB_PERMISSION),0);
                                    usbManager.requestPermission(usbDevice,permissionIntent);
                                }
                            }).setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    OnlyOneToast.makeText(context,"获取权限失败");
                                    if(alertDialog!=null&&alertDialog.isShowing()){
                                        alertDialog.cancel();
                                    }
                                }
                            });
                    alertDialog=dialoguilder.create();
                }
                if(Build.VERSION.SDK_INT>=23){
                    if(!Settings.canDrawOverlays(context)){
                        alertDialog.show();
                    }else{
                        // alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);//设置系统级别的弹出框
                        alertDialog.show();
                    }
                }else{
                    //alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);//设置系统级别的弹出框
                    alertDialog.show();
                }
            }
        }
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
        }else if(keyCode == KeyEvent.KEYCODE_HOME){

                        return true;
        }else{
            return super.onKeyDown(keyCode, event);
        }


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    //根据屏幕的宽高来初始化控件的位置和大小
    private  void  initImageIconPositionAndSize(){
        WindowManager windowManager=getWindowManager();
        long screenHeigt=windowManager.getDefaultDisplay().getHeight();//屏幕的高度
        Log.i("recen", "initImageIconPositionAndSize: "+screenHeigt);
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
        LocalMusicFragment.musicTitle=musicTitle;
        MusicService.musicTitle=musicTitle;
        LocalMusicFragment.activity=MainActivity.this;

    }
    private void setUpDrawer() {
        LayoutInflater inflater = LayoutInflater.from(this);
        mLvLeftMenu.addHeaderView(inflater.inflate(R.layout.nav_header_main, mLvLeftMenu, false));
        initData();
        adapter = new ContentAdapter(this, list);
        mLvLeftMenu.setAdapter(adapter);

        mLvLeftMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {//设置左侧菜单栏点击事件
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1:
                        Selection_Theme++;
                        if(Selection_Theme%2==1) {
                            drawerLayout.setBackgroundResource(R.drawable.black);
                            list.get(0).setText("日间模式");
                        }else{
                            drawerLayout.setBackgroundResource(R.drawable.playindex_background_image);
                            list.get(0).setText("夜间模式");
                        }
                        adapter = new ContentAdapter(getBaseContext(), list);
                        mLvLeftMenu.setAdapter(adapter);
                        drawerLayout.closeDrawers();
                        break;
                    case 2:

                        break;
                    case 3:
                        TimingFragment fragment3 = new TimingFragment();
                        fragment3.show(getSupportFragmentManager(), "timing");
                        drawerLayout.closeDrawers();

                        break;
                    case 4:
                        SourcequalityFragment fragment4 = new SourcequalityFragment();
                        fragment4.show(getSupportFragmentManager(), "quality");
                        drawerLayout.closeDrawers();
                        break;
                    case 5:
                        if(MusicService.mediaPlayer.isPlaying()==false){
                            Toast.makeText(MainActivity.this,"当前没有音乐播放",Toast.LENGTH_LONG).show();
                        }else{
                            Intent intent = new Intent(MainActivity.this,EqualizerActivity.class);
                            startActivity(intent);
                            drawerLayout.closeDrawers();
                        }
                        break;
                    case 6:
                        exit(0);
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
        list.add(new ContentModel(R.mipmap.topmenu_icn_equalizer, "均衡器", 5));
        list.add(new ContentModel(R.mipmap.topmenu_icn_exit, "退出", 6));

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
            case 0://选中本地音乐
                mLocalMusicButton.setBackgroundResource(R.drawable.localmusic_selected);//图标改变
                if(mlocalMusicFragment==null){//初始化本地音乐页面localMusicFragment()
                    mlocalMusicFragment=new LocalMusicFragment();
                    fragmentTransaction.add(R.id.IndexContent,mlocalMusicFragment);
                }else{
                    fragmentTransaction.show(mlocalMusicFragment);
                }
                break;
            case 1://选中歌曲列表的
                msongListButton.setBackgroundResource(R.drawable.songlist_selected);//图标改变
                if(msongListFragment==null){//初始化歌曲页面songListFragment()
                    //msongListFragment=new SongListFragment();
                    msongListFragment = new TabListFragment();
                    fragmentTransaction.add(R.id.IndexContent,msongListFragment);
                }else{
                    fragmentTransaction.show(msongListFragment);
                }
                break;
            case 2://选中在线音乐
                monlineMusicButton.setBackgroundResource(R.drawable.onlinemusic_selected);//图标改变

                if(monlineMusicFragment==null){//初始化在线音乐页面onlineMusicFragment()
                    monlineMusicFragment=new OnlineMusicFragment();
                    fragmentTransaction.add(R.id.IndexContent,monlineMusicFragment);
                }else{
                    fragmentTransaction.show(monlineMusicFragment);
                }
                break;
            case 3://选中设置按钮
                msettingButton.setBackgroundResource(R.drawable.setting_selected);//图标改变
                if(msettingFragment==null){//初始化设置页面settingFragment()
                    msettingFragment=new SettingFragment();
                    fragmentTransaction.add(R.id.IndexContent,msettingFragment);
                }else{
                    fragmentTransaction.show(msettingFragment);
                }
                break;
            case 4:
                searchMusicButton.setBackgroundResource(R.drawable.search_selected);
                if(searchMusicFragment==null){
                    searchMusicFragment=new SearchFragment();
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
        unregisterReceiver(usbBroadcastReceiver);
        super.onDestroy();
    }


}
