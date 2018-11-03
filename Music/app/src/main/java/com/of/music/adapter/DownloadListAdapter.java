package com.of.music.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
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

public class DownloadListAdapter extends BaseAdapter{
    private List<DownloadMusicInfo> downloadInfoList=new ArrayList<>();
    private OnMoreClickListener listener;
    private boolean isPlaylist;
    public Context context;
    public DownloadListAdapter(Context context,List<DownloadMusicInfo> musicList) {
        super();
        this.context=context;
        this.downloadInfoList.clear();
        this.downloadInfoList.addAll(musicList);
    }
    
    public List<DownloadMusicInfo> getDownloadInfoList() {
        return downloadInfoList;
    }
    
    public void setDownloadInfoList(List<DownloadMusicInfo> downloadInfoList) {
        this.downloadInfoList.clear();
        this.downloadInfoList.addAll(downloadInfoList);
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
            convertView=convertView.inflate(App.sContext, R.layout.fragement_download_music_item, null);
            holder = new ViewHolder();
            holder.tvArtist=convertView.findViewById(R.id.tv_artist1);
            holder.ivCover=convertView.findViewById(R.id.iv_cover1);
            holder.ivMore=convertView.findViewById(R.id.iv_more1);
            holder.tvTitle=convertView.findViewById(R.id.tv_title1);
            holder.vDivider=convertView.findViewById(R.id.v_divider1);
            holder.vPlaying=convertView.findViewById(R.id.v_playing1);
            convertView.setTag(holder);
        } else {
            holder =(ViewHolder)convertView.getTag();
        }
        holder.vPlaying.setVisibility((isPlaylist && position == AudioPlayer.get().getPlayPosition()) ? View.VISIBLE : View.INVISIBLE);
        DownloadMusicInfo musicName=downloadInfoList.get(position);
        String albumAddress= musicName.getCoverPath();
        Bitmap   cover= MusicIconLoader.getInstance().load(albumAddress);
        holder.ivCover.setImageBitmap(cover);
        holder.tvTitle.setText(musicName.getTitle());
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
