package com.example.mrxie.music.Intent;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.mrxie.music.R;


/**
 * 图片加载工具
 */
public class ImageLoaderUtil {
    /**
     * 生成图片请求设置
     */
    private static RequestOptions createRequestOptions() {
        return new RequestOptions()
                .error(R.mipmap.ic_launcher)//加载失败后显示的图片
                .placeholder(R.mipmap.ic_launcher)//占位图
                .circleCrop()//圆形
                .timeout(2000);//设置加载超时时间
    }

    /**
     * 为指定图片控件加载指定图片
     *
     * @param imageView 图片控件
     * @param url       图片地址
     */
    public static void loadPicByUrl(ImageView imageView, String url) {
        if (null == imageView)
            return;
        Glide.with(imageView.getContext())
                .load(url)
                .apply(createRequestOptions())//使用加载请求设置
                .into(imageView);
    }
}
