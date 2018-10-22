package com.of.music.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.of.music.R;
import com.of.music.adapter.Bind;
import com.of.music.adapter.OnMoreClickListener;
import com.of.music.adapter.PlaylistAdapter;
import com.of.music.model.Imusic;
import com.of.music.services.AudioPlayer;
import com.of.music.services.OnPlayerEventListener;


/**
 * 播放列表
 */
public class PlaylistActivity extends BaseActivity implements AdapterView.OnItemClickListener, OnMoreClickListener, OnPlayerEventListener {
    @Bind(R.id.lv_playlist)
    private ListView lvPlaylist;

    private PlaylistAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
    }

    @Override
    protected void onServiceBound() {
        adapter = new PlaylistAdapter(AudioPlayer.get().getMusicList());
        adapter.setIsPlaylist(true);
        adapter.setOnMoreClickListener(this);
        lvPlaylist.setAdapter(adapter);
        lvPlaylist.setOnItemClickListener(this);
        AudioPlayer.get().addOnPlayEventListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AudioPlayer.get().play(position);
    }

    @Override
    public void onMoreClick(final int position) {
        String[] items = new String[]{"移除"};
        Imusic music = AudioPlayer.get().getMusicList().get(position);
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(music.getTitle());
        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AudioPlayer.get().delete(position);
                adapter.notifyDataSetChanged();
            }
        });
        dialog.show();
    }

    @Override
    public void onChange(Imusic music) {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onPlayerStart() {
    }

    @Override
    public void onPlayerPause() {
    }

    @Override
    public void onPublish(int progress) {
    }

    @Override
    public void onBufferingUpdate(int percent) {
    }

    @Override
    protected void onDestroy() {
        AudioPlayer.get().removeOnPlayEventListener(this);
        super.onDestroy();
    }
}
