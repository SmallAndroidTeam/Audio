package com.of.music.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.of.music.Application.App;
import com.of.music.R;
import com.of.music.info.MusicName;
import com.of.music.info.RecentlyMusicListInfo;
import com.of.music.model.DownloadInfo;
import com.of.music.model.DownloadMusicInfo;
import com.of.music.model.Imusic;
import com.of.music.services.AudioPlayer;
import com.of.music.songListInformation.MusicIconLoader;
import com.of.music.util.onlineUtil.CoverLoader;
import com.of.music.util.onlineUtil.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PlaylistAdapter extends BaseAdapter{
    private List<RecentlyMusicListInfo> downloadInfoList;
    private OnMoreClickListener listener;
    private boolean isPlaylist;
    public Context context;
    public PlaylistAdapter(Context context,List<RecentlyMusicListInfo> musicList) {
        super();
        this.context=context;
        this.downloadInfoList = musicList;
    }
    
    public void setIsPlaylist(boolean isPlaylist) {
        this.isPlaylist = isPlaylist;
    }
    
    public void setOnMoreClickListener(OnMoreClickListener listener) {
        this.listener = listener;
    }
    
    @Override
    public int getCount() {
        return downloadInfoList.size();
    }
    
    @Override
    public Object getItem(int position) {
        return downloadInfoList.get(position);
    }
    
    @Override
    public long getItemId(int position) {
        return position;
    }
    
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView=convertView.inflate(App.sContext, R.layout.view_holder_music, null);
            holder = new ViewHolder();
            holder.tvArtist=convertView.findViewById(R.id.tv_artist);
            holder.ivCover=convertView.findViewById(R.id.iv_cover);
            holder.ivMore=convertView.findViewById(R.id.iv_more);
            holder.tvTitle=convertView.findViewById(R.id.tv_title);
            holder.vDivider=convertView.findViewById(R.id.v_divider);
            holder.vPlaying=convertView.findViewById(R.id.v_playing);
            convertView.setTag(holder);
        } else {
            holder =(ViewHolder)convertView.getTag();
        }
//        holder.vPlaying.setVisibility((isPlaylist && position == AudioPlayer.get().getPlayPosition()) ? View.VISIBLE : View.INVISIBLE);
        RecentlyMusicListInfo musicName=downloadInfoList.get(position);
        String albumAddress= musicName.getImage();
        Bitmap   cover= MusicIconLoader.getInstance().load(albumAddress);
        holder.ivCover.setImageBitmap(cover);
        holder.tvTitle.setText(musicName.getName());
        holder.tvArtist.setText(musicName.getArtist());
        holder.ivMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onMoreClick(position);
                }
            }
        });
        holder.vDivider.setVisibility(isShowDivider(position) ? View.VISIBLE : View.GONE);
        return convertView;
    }
    
    private boolean isShowDivider(int position) {
        return position != downloadInfoList.size() - 1;
    }
    
    private static class ViewHolder {
        private View vPlaying;
        private ImageView ivCover;
        private TextView tvTitle;
        private TextView tvArtist;
        private ImageView ivMore;
        private View vDivider;
        
    }
}
