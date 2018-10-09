package com.of.music.intent;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**网络工具*/
public class NetworkUtil {
    /**校验是否联网或者网络可用*/
    public static boolean isNetworkConnected(Context context){
        if (null==context)//没有上下文
            return false;
        //获取连接管理
        ConnectivityManager connectivityManager= (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();//获取在用的网络信息
        if (null!=networkInfo)//有在用的网络
            return networkInfo.isConnected();//判断是否连接
        return false;
    }
}
