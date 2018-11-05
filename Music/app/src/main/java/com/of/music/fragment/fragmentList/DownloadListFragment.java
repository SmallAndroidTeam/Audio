package com.of.music.fragment.fragmentList;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.of.music.Application.App;
import com.of.music.Application.AppCache;
import com.of.music.R;
import com.of.music.activity.ArtistInfoActivity;
import com.of.music.activity.MusicInfoActivity;
import com.of.music.activity.OnlineMusicActivity;
import com.of.music.adapter.Bind;
import com.of.music.adapter.DownloadListAdapter;
import com.of.music.adapter.OnMoreClickListener;
import com.of.music.adapter.PlaylistAdapter;
import com.of.music.adapter.RxBusTags;
import com.of.music.db.DownloadMusicOperater;
import com.of.music.downloadExecute.DownloadMusic;
import com.of.music.downloadExecute.DownloadOnlineMusic;
import com.of.music.fragment.LocalMusicFragment;
import com.of.music.fragment.fragmentNet.BaseFragment;
import com.of.music.info.MusicName;
import com.of.music.model.DownloadMusicInfo;
import com.of.music.model.Imusic;
import com.of.music.model.Keys;
import com.of.music.model.RequestCode;
import com.of.music.services.AudioPlayer;
import com.of.music.services.MusicService;
import com.of.music.songListInformation.LocalMusicUtils;
import com.of.music.songListInformation.Music;
import com.of.music.songListInformation.MusicUtils;
import com.of.music.util.onlineUtil.FileUtils;
import com.of.music.util.onlineUtil.PermissionReq;
import com.of.music.util.onlineUtil.ToastUtils;

import org.litepal.LitePal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import me.wcy.music.loader.MusicLoaderCallback;

public class DownloadListFragment extends BaseFragment implements AdapterView.OnItemClickListener, OnMoreClickListener {
    @Bind(R.id.lv_local_music)
    private ListView lvLocalMusic;
    @Bind(R.id.v_searching)
    private TextView vSearching;
    ArrayList<Music> downloadlist;
    private Loader<Cursor> loader;
    private DownloadListAdapter adapter;
    private List<DownloadMusicInfo> imusics=new ArrayList<>();
    public  static DownloadMusicOperater downloadMusicOperater;
   
    @Bind(R.id.downloadsrl)
    private SwipeRefreshLayout mSwipeRefreshLayout;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_folder_list, container, false);
        
    }
    
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        downloadMusicOperater=new DownloadMusicOperater(getActivity());
        imusics.clear();
        imusics.addAll(downloadMusicOperater.queryMany());
        adapter = new DownloadListAdapter(getActivity(),imusics);
        downloadlist=new ArrayList<>();
        for(int i=0;i<imusics.size();i++){
            Music music=new Music(imusics.get(i).getTitle(),imusics.get(i).getMusicPath()
                    ,imusics.get(i).getCoverPath(),imusics.get(i).getArtist(),imusics.get(i).getLrcPath());
            downloadlist.add(music);
        }
        
        lvLocalMusic.setAdapter(adapter);
        adapter.setOnMoreClickListener(this);
        //初始化下拉控件颜色
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... voids) {
                        SystemClock.sleep(2000);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {

                        Toast.makeText(getActivity(), "下拉刷新成功", Toast.LENGTH_SHORT).show();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }.execute();
            }
        });
   }
    
    @Override
    protected void setListener() {
        lvLocalMusic.setOnItemClickListener(this);
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.i("altertime",imusics.get(position).getTime()+"//"+"a");
        LocalMusicFragment.sMusicList=downloadlist;
        MusicService.playingMusicIndex=position;
        new MusicService().initMusic();
        Intent intent = new Intent(getActivity(), MusicService.class);
        intent.setAction(MusicService.TOGGLEPAUSE_ACTION);
        Objects.requireNonNull(getActivity()).startService(intent);
       
    }
    
    @Override
    public void onMoreClick(final int position) {
        final DownloadMusicInfo music=imusics.get(position);
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
    private void shareMusic(DownloadMusicInfo music) {
        File file = new File(music.getMusicPath());
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
    
    private void deleteMusic(final DownloadMusicInfo music) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        String title = music.getTitle();
        final String msg = getString(R.string.delete_music, title);
        dialog.setMessage(msg);
        dialog.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                File file = new File(music.getMusicPath());
                if (file.delete()) {
                    // 刷新媒体库
                    Intent intent =
                            new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://".concat(music.getMusicPath())));
                    getContext().sendBroadcast(intent);
                    LocalMusicFragment.sMusicList.remove(music);
                    downloadMusicOperater.delete(music.getTitle());
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
    public void DownloadAlter() {
            Log.i("Music", "onReceive: 广播接受成功");
            imusics.clear();
            Log.i("Music", "清理后，收藏列表（favouriteMusicListInfos）的歌曲数目：  "+imusics.size());
            imusics.addAll(downloadMusicOperater.queryMany());
            Log.i("Music", "查找后，收藏列表（favouriteMusicListInfos）的歌曲数目"+imusics.size());
          adapter.setDownloadInfoList(imusics);
         adapter.notifyDataSetChanged();
        }
    
    @Override
    public void onPause() {
        super.onPause();
        Log.i("audio111", "onPause: ");
    }
    
    @Override
    public void onResume() {
        super.onResume();
        DownloadAlter();
        Log.i("audio111", "onResume: ");
    }
}