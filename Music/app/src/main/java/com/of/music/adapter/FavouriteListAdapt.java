package com.of.music.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.of.music.R;
import com.of.music.info.FavouriteMusicListInfo;
import com.of.music.songListInformation.MusicIconLoader;

import java.util.List;

public class FavouriteListAdapt extends BaseAdapter {
    private Context context;
    private List<FavouriteMusicListInfo> list;

    public FavouriteListAdapt(Context context, List<FavouriteMusicListInfo> list) {
        super();
        this.context = context;
        this.list = list;
    }
    //这个是返回Adapter 连接/绑定 的数据集合的长度，也是对应容器View（如ListView）的项的个数。
    @Override
    public int getCount() {
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (list != null) {
            return list.get(position);
        }
        return null;
    }
    @Override
    public long getItemId(int position) {
        return list.get(position).getId();
    }
    //这个是返回parent中每个项（如ListView中的每一行）的View。
    // View convertView参数就是滚出屏幕的Item的View
    //ViewGroup parent参数是加载xml视图时使用。inflate(R.layout.adapter__item, parent, false);确定他父控件，减少宽高的测算
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FavouriteListAdapt.ViewHold hold;
        if (convertView == null) {
            hold = new FavouriteListAdapt.ViewHold();
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.music_list_item, null);
            convertView.setTag(hold);
        } else {
            hold = (FavouriteListAdapt.ViewHold) convertView.getTag();
        }
        hold.icon = (ImageView) convertView.findViewById(R.id.music_list_icon);
        Bitmap icon = MusicIconLoader.getInstance().load(list.get(position).getImage());
        hold.icon.setImageBitmap(icon);
        hold.textView = (TextView) convertView.findViewById(R.id.tv_music_list_title);
        hold.textView.setText(list.get(position).getName());
        hold.textView1 = (TextView) convertView.findViewById(R.id.tv_music_list_artist);
        hold.textView1.setText(list.get(position).getArtist());

        return convertView;
    }

    class ViewHold {
        public ImageView icon;
        public TextView textView;
        public TextView textView1;
    }

}
