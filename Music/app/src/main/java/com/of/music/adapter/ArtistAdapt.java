package com.of.music.adapter;

import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.of.music.R;
import com.of.music.Application.App;
import com.of.music.fragment.SingerListFragment;
import com.of.music.songListInformation.MusicIconLoader;
import com.of.music.songListInformation.MusicUtils;

public class ArtistAdapt extends BaseAdapter{

    private int mPlayingPosition;


    public void setPlayingPosition(int position) {
        mPlayingPosition = position;
    }

    public int getPlayingPosition() {
        return mPlayingPosition;
    }

    @Override
    public int getCount() {
        return SingerListFragment.musicList.size();
    }

    @Override
    public Object getItem(int position) {
        return SingerListFragment.musicList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder ;

        if(convertView == null) {
            convertView = View.inflate(App.sContext, R.layout.music_list_item, null);
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.tv_music_list_title);
            holder.artist = (TextView) convertView.findViewById(R.id.tv_music_list_artist);
            holder.icon = (ImageView) convertView.findViewById(R.id.music_list_icon);
            holder.mark = convertView.findViewById(R.id.music_list_selected);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }

        if(mPlayingPosition == position) {
            holder.mark.setVisibility(View.VISIBLE);
        }else {
            holder.mark.setVisibility(View.INVISIBLE);
        }
                Bitmap icon = MusicIconLoader.getInstance().load(SingerListFragment.musicList.get(position).getImage());
                holder.icon.setImageBitmap(icon);
            holder.title.setText(SingerListFragment.musicList.get(position).getArtist());
            int k = 0;
            for (int i = 0; i < MusicUtils.sMusicList.size(); i++) {
                if (SingerListFragment.musicList.get(position).getArtist().equals(MusicUtils.sMusicList.get(i).getArtist())) {
                    k++;
                }
            }
            holder.artist.setText("歌曲数：" + k);
        return convertView;
    }

    static class ViewHolder {
        ImageView icon;
        TextView title;
        TextView artist;
        View mark;
    }
}
