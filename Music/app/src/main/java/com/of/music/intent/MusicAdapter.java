package com.of.music.intent;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.of.music.R;

import java.util.List;

/**
 * 音乐对应的内容和样式适配器
 */
public class MusicAdapter extends BaseAdapter {
    /**
     * 上下文环境
     */
    private Context context;
    /**
     * 音乐数据
     */
//    private List<MusicVo> musics;
    private List<MusicNeteaseVo> musics;

    public MusicAdapter(Context context, List<MusicNeteaseVo> musics) {
        this.context = context;
        this.musics = musics;
    }

    @Override
    public int getCount() {
        return null == musics ? 0 : musics.size();
    }

    @Override
    public Object getItem(int position) {
        return null == musics ? null : musics.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MusicViewHolder musicViewHolder;
        if (null == convertView) {
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_music_item, null);//渲染视图
            musicViewHolder = new MusicViewHolder();//实例化试图句柄
            musicViewHolder.albumPicIv = convertView.findViewById(R.id.album_pic_iv);
            musicViewHolder.musicNameTv = convertView.findViewById(R.id.music_name_tv);
            musicViewHolder.authorNameTv = convertView.findViewById(R.id.author_name_tv);
            musicViewHolder.albumNameTv = convertView.findViewById(R.id.album_name_tv);
            convertView.setTag(musicViewHolder);//设置试图句柄，便于复用
        } else
            musicViewHolder = (MusicViewHolder) convertView.getTag();
        MusicNeteaseVo item = musics.get(position);
        //显示信息
        Log.i("kkk--->", "getView: "+item.pic);
        ImageLoaderUtil.loadPicByUrl(musicViewHolder.albumPicIv, item.pic);
        musicViewHolder.musicNameTv.setText(item.title);
        musicViewHolder.authorNameTv.setText(item.author);
        musicViewHolder.albumNameTv.setText(item.title);
        return convertView;
    }

    /**
     * 试图句柄
     */
    private static class MusicViewHolder {
        /**
         * 专辑封面
         */
        public ImageView albumPicIv;
        /**
         * 歌名
         */
        public TextView musicNameTv;
        /**
         * 歌手名
         */
        public TextView authorNameTv;
        /**
         * 专辑名
         */
        public TextView albumNameTv;
    }
}
