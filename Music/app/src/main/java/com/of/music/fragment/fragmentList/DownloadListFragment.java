package com.of.music.fragment.fragmentList;

import android.Manifest;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.of.music.Application.App;
import com.of.music.Application.AppCache;
import com.of.music.R;
import com.of.music.activity.ArtistInfoActivity;
import com.of.music.activity.MusicInfoActivity;
import com.of.music.activity.OnlineMusicActivity;
import com.of.music.adapter.Bind;
import com.of.music.adapter.OnMoreClickListener;
import com.of.music.adapter.PlaylistAdapter;
import com.of.music.adapter.RxBusTags;
import com.of.music.downloadExecute.DownloadOnlineMusic;
import com.of.music.fragment.LocalMusicFragment;
import com.of.music.fragment.fragmentNet.BaseFragment;
import com.of.music.model.Imusic;
import com.of.music.model.Keys;
import com.of.music.model.RequestCode;
import com.of.music.services.AudioPlayer;
import com.of.music.services.MusicService;
import com.of.music.songListInformation.Music;
import com.of.music.songListInformation.MusicUtils;
import com.of.music.util.onlineUtil.FileUtils;
import com.of.music.util.onlineUtil.PermissionReq;
import com.of.music.util.onlineUtil.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.wcy.music.loader.MusicLoaderCallback;

public class DownloadListFragment extends BaseFragment implements AdapterView.OnItemClickListener, OnMoreClickListener {
    @Bind(R.id.lv_local_music)
    private ListView lvLocalMusic;
    @Bind(R.id.v_searching)
    private TextView vSearching;
    
    private Loader<Cursor> loader;
    private PlaylistAdapter adapter;
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_folder_list, container, false);
    }
    
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
      
        adapter = new PlaylistAdapter(AppCache.get().getLocalMusicList());
        
        adapter.setOnMoreClickListener(this);
        lvLocalMusic.setAdapter(adapter);
        loadMusic();
    }
    
    private void loadMusic() {
        lvLocalMusic.setVisibility(View.GONE);
        vSearching.setVisibility(View.VISIBLE);
        PermissionReq.with(this)
                .permissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .result(new PermissionReq.Result() {
                    @Override
                    public void onGranted() {
                        initLoader();
                    }
                    
                    @Override
                    public void onDenied() {
                        ToastUtils.show(R.string.no_permission_storage);
                        lvLocalMusic.setVisibility(View.VISIBLE);
                        vSearching.setVisibility(View.GONE);
                    }
                })
                .request();
    }
    
    private void initLoader() {
        loader = getActivity().getLoaderManager().initLoader(0, null, new MusicLoaderCallback(getContext(),
                new ValueCallback<List<Imusic>>() {
                    @Override
                    public void onReceiveValue(List<Imusic> value) {
                        AppCache.get().getLocalMusicList().clear();
                        AppCache.get().getLocalMusicList().addAll(value);
                        lvLocalMusic.setVisibility(View.VISIBLE);
                        vSearching.setVisibility(View.GONE);
                        adapter.notifyDataSetChanged();
                    }
                }));
    }
    
    @Subscribe(tags = {@Tag(RxBusTags.SCAN_MUSIC)})
    public void scanMusic(Object object) {
        if (loader != null) {
            loader.forceLoad();
        }
    }
    
    @Override
    protected void setListener() {
        lvLocalMusic.setOnItemClickListener(this);
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ArrayList<Imusic> musicArrayList=new ArrayList<>();
       final ArrayList<Music> musics=new ArrayList<>();
        musicArrayList=AppCache.get().getLocalMusicList();
        Log.i("musicsize",musicArrayList.get(0).getCoverPath()+"  "+musicArrayList.get(0).getTitle()
                +"  "+musicArrayList.get(0).getAlbum()+"  "+musicArrayList.get(0).getArtist()+"   "
                +musicArrayList.get(0).getPath()+"  "+musicArrayList.get(0).getFileName()+"    "
                +MusicUtils.sMusicList.get(0).getLrcpath()+"    "+MusicUtils.sMusicList.get(0).getImage());
        for(int i=0;i<musicArrayList.size();i++){
            
            Music imusic=new Music(musicArrayList.get(i).getTitle(),musicArrayList.get(i).getPath()
                    ,musicArrayList.get(i).getCoverPath() ,musicArrayList.get(i).getArtist()
                    ,MusicUtils.sMusicList.get(i).getLrcpath());
            musics.add(imusic);
        }
        LocalMusicFragment.sMusicList = musics;
        MusicService.playingMusicIndex =position;
        Intent intent = new Intent(getActivity(), MusicService.class);
        intent.setAction(MusicService.TOGGLEPAUSE_ACTION);
        getActivity().startService(intent);
    }
    
    @Override
    public void onMoreClick(final int position) {
        final Imusic music=AppCache.get().getLocalMusicList().get(position);
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle(music.getTitle());
        dialog.setItems(R.array.local_music_dialog, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:// 分享
                        shareMusic(music);
                        break;
//                    case 1:// 设为铃声
//                        requestSetRingtone(music);
//                        break;
                    case 1:// 删除
                        deleteMusic(music);
                        break;
                }
            
        } });
        dialog.show();
    }
    private void shareMusic(Imusic music) {
        File file = new File(music.getPath());
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("audio/*");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        startActivity(Intent.createChooser(intent, getString(R.string.share)));
    }
    
    private void requestSetRingtone(final Imusic music) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(getContext())) {
            ToastUtils.show(R.string.no_permission_setting);
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + getContext().getPackageName()));
            startActivityForResult(intent, RequestCode.REQUEST_WRITE_SETTINGS);
        } else {
            setRingtone(music);
        }
    }
    /**
     * 设置铃声
     */
    private void setRingtone(Imusic music) {
        Uri uri = MediaStore.Audio.Media.getContentUriForPath(music.getPath());
        // 查询音乐文件在媒体库是否存在
        Cursor cursor = App.sContext.getContentResolver()
                .query(uri, null, MediaStore.MediaColumns.DATA + "=?", new String[] { music.getPath() }, null);
        if (cursor == null) {
            return;
        }
        if (cursor.moveToFirst() && cursor.getCount() > 0) {
            String _id = cursor.getString(0);
            ContentValues values = new ContentValues();
            values.put(MediaStore.Audio.Media.IS_MUSIC, true);
            values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
            values.put(MediaStore.Audio.Media.IS_ALARM, false);
            values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
            values.put(MediaStore.Audio.Media.IS_PODCAST, false);
            
            getContext().getContentResolver()
                    .update(uri, values, MediaStore.MediaColumns.DATA + "=?", new String[] { music.getPath() });
            Uri newUri = ContentUris.withAppendedId(uri, Long.valueOf(_id));
            RingtoneManager.setActualDefaultRingtoneUri(getContext(), RingtoneManager.TYPE_RINGTONE, newUri);
            ToastUtils.show(R.string.setting_ringtone_success);
        }
        cursor.close();
    }
    
    private void deleteMusic(final Imusic music) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        String title = music.getTitle();
        String msg = getString(R.string.delete_music, title);
        dialog.setMessage(msg);
        dialog.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                File file = new File(music.getPath());
                if (file.delete()) {
                    // 刷新媒体库
                    Intent intent =
                            new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://".concat(music.getPath())));
                    getContext().sendBroadcast(intent);
                }
            }
        });
        dialog.setNegativeButton(R.string.cancel, null);
        dialog.show();
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCode.REQUEST_WRITE_SETTINGS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.System.canWrite(getContext())) {
                ToastUtils.show(R.string.grant_permission_setting);
            }
        }
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        int position = lvLocalMusic.getFirstVisiblePosition();
        int offset = (lvLocalMusic.getChildAt(0) == null) ? 0 : lvLocalMusic.getChildAt(0).getTop();
        outState.putInt(Keys.LOCAL_MUSIC_POSITION, position);
        outState.putInt(Keys.LOCAL_MUSIC_OFFSET, offset);
    }
    
    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        lvLocalMusic.post(new Runnable() {
            @Override
            public void run() {
                int position = savedInstanceState.getInt(Keys.PLAYLIST_POSITION);
                int offset = savedInstanceState.getInt(Keys.PLAYLIST_OFFSET);
                lvLocalMusic.setSelectionFromTop(position, offset);
            }
        });
    }
}