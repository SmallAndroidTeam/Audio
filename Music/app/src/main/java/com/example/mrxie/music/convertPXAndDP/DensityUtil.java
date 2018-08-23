package com.example.mrxie.music.convertPXAndDP;

import android.content.Context;

/**
 * 转化dp和px
 * Created by MR.XIE on 2018/8/18.
 */
public class DensityUtil {
    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context,float deValue){
        final float scale=context.getResources().getDisplayMetrics().density;
        return (int)(deValue*scale+0.5f);
    }
    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context,float deValue) {
    final  float scale=context.getResources().getDisplayMetrics().density;
    return (int)(deValue/scale+0.5f);

    }
    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 sp
     */
    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }
}
