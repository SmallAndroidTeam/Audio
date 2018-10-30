package com.of.music.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.of.music.R;
import com.of.music.model.DownloadInfo;
import com.of.music.model.DownloadMusicInfo;
import com.of.music.model.Imusic;
import com.of.music.services.AudioPlayer;
import com.of.music.songListInformation.MusicIconLoader;
import com.of.music.util.onlineUtil.CoverLoader;
import com.of.music.util.onlineUtil.FileUtils;

import java.io.File;
import java.util.List;

public class DownloadListAdapter extends BaseAdapter{
    private List<DownloadMusicInfo> downloadInfoList;
    private OnMoreClickListener listener;
    private boolean isPlaylist;
    
    public DownloadListAdapter(List<DownloadMusicInfo> musicList) {
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
        DownloadListAdapter.ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_music, parent, false);
            holder = new DownloadListAdapter.ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (DownloadListAdapter.ViewHolder) convertView.getTag();
        }
        holder.vPlaying.setVisibility((isPlaylist && position == AudioPlayer.get().getPlayPosition()) ? View.VISIBLE : View.INVISIBLE);
        DownloadMusicInfo music = downloadInfoList.get(position);
        String albumAddress= music.getCoverPath();
       
        Bitmap   cover= MusicIconLoader.getInstance().load(albumAddress);
       
        holder.ivCover.setImageBitmap(cover);
        holder.tvTitle.setText(music.getTitle());
      
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
        @Bind(R.id.v_playing)
        private View vPlaying;
        @Bind(R.id.iv_cover)
        private ImageView ivCover;
        @Bind(R.id.tv_title)
        private TextView tvTitle;
        @Bind(R.id.tv_artist)
        private TextView tvArtist;
        @Bind(R.id.iv_more)
        private ImageView ivMore;
        @Bind(R.id.v_divider)
        private View vDivider;
        
        public ViewHolder(View view) {
            ViewBinder.bind(this, view);
        }
    }
}
