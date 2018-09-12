package com.example.mrxie.music.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mrxie.music.R;
import com.example.mrxie.music.songListInformation.MusicIconLoader;
import com.example.mrxie.music.info.MusicName;

import java.util.List;

public class SongNameAdapt extends BaseAdapter {
    private Context context;
    private List<MusicName> list;

    public SongNameAdapt(Context context, List<MusicName> list) {
        super();
        this.context = context;
        this.list = list;
    }

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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SongNameAdapt.ViewHold hold;
        if (convertView == null) {
            hold = new SongNameAdapt.ViewHold();
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.music_list_item, null);
            convertView.setTag(hold);
        } else {
            hold = (SongNameAdapt.ViewHold) convertView.getTag();
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
