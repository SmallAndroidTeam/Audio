package com.of.music.fragment.fragmentList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.of.music.Application.App;
import com.of.music.R;
import com.of.music.adapter.Bind;
import com.of.music.adapter.FavouriteListAdapt;
import com.of.music.adapter.OnMoreClickListener;
import com.of.music.adapter.PlaylistAdapter;
import com.of.music.fragment.LocalMusicFragment;
import com.of.music.fragment.fragmentNet.BaseFragment;
import com.of.music.info.FavouriteMusicListInfo;
import com.of.music.info.RecentlyMusicListInfo;
import com.of.music.model.Imusic;
import com.of.music.services.AudioPlayer;
import com.of.music.services.MusicService;
import com.of.music.services.OnPlayerEventListener;
import com.of.music.songListInformation.Music;
import com.of.music.util.onlineUtil.FileUtils;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;
import org.litepal.exceptions.DataSupportException;

import java.util.ArrayList;
import java.util.List;

public class RecentlyListFragment extends BaseFragment implements AdapterView.OnItemClickListener,OnMoreClickListener {
   
   private ListView lvPlaylist;
    ArrayList<Music> musics;
    private PlaylistAdapter adapter;
    private List<RecentlyMusicListInfo> imusicArrayList=new ArrayList<>();
   
   @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
       LitePal.getDatabase();
       View view = inflater.inflate(R.layout.fragment_recently_list, null);
       lvPlaylist=view.findViewById(R.id.lv_playlist);
       imusicArrayList=LitePal.select("name","artist","image","uri","Lrc_uri","playTime").order("playTime desc").find(RecentlyMusicListInfo.class);
       Log.i("recently","从Recentlylist播放"+imusicArrayList.size());
       musics=new ArrayList<>();
       for(int i=0;i<imusicArrayList.size();i++){
           Music music=new Music(imusicArrayList.get(i).getName(),imusicArrayList.get(i).getUri()
                   ,imusicArrayList.get(i).getImage(),imusicArrayList.get(i).getArtist(),imusicArrayList.get(i).getLrc_uri());
           musics.add(music);
       }
        adapter = new PlaylistAdapter(getActivity(),imusicArrayList);
        adapter.setIsPlaylist(true);
        adapter.setOnMoreClickListener(this);
        lvPlaylist.setAdapter(adapter);
        lvPlaylist.setOnItemClickListener(this);

       return view;
    }

   @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
       Log.i("recently","从Recentlylist播放"+imusicArrayList.get(position).getName());
        LocalMusicFragment.sMusicList=musics;
        MusicService.playingMusicIndex =position;
       new MusicService().initMusic();
       Intent intent = new Intent(getActivity(), MusicService.class);
       intent.setAction(MusicService.TOGGLEPAUSE_ACTION);
       getActivity().startService(intent);
    }

   @Override
   public void onMoreClick(final int position) {
        String[] items = new String[]{"移除"};
        Music music=musics.get(position);
        String name=music.getTitle();
        String uri=imusicArrayList.get(position).getUri();
      final AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle(music.getTitle());
       dialog.setItems(items, new DialogInterface.OnClickListener() {
           @Override
            public void onClick(DialogInterface dialog, int which) {
                LitePal.deleteAll(RecentlyMusicListInfo.class,"uri=?",imusicArrayList.get(position).getUri());
                Log.i("delete","         "+LitePal.findAll(RecentlyMusicListInfo.class).size());
                imusicArrayList.remove(position);
               adapter.notifyDataSetChanged();
             
          }
        });
      
       dialog.show();
   }
   
    public   void  AlterAdapter(){
        Log.i("Music", "onReceive: 广播接受成功");
        imusicArrayList.clear();
        Log.i("Music", "清理后，收藏列表（favouriteMusicListInfos）的歌曲数目：  "+imusicArrayList.size());

        imusicArrayList = LitePal.select("name","artist","image","uri","Lrc_uri","playTime").order("playTime desc").find(RecentlyMusicListInfo.class);
        Log.i("Music", "查找后，收藏列表（favouriteMusicListInfos）的歌曲数目"+imusicArrayList.size());
        PlaylistAdapter favouriteListAdapt = new PlaylistAdapter(getActivity(),imusicArrayList);
        lvPlaylist.setAdapter(favouriteListAdapt);
    }
}
