package com.of.music.fragment.fragmentList;

import android.content.DialogInterface;
import android.content.Intent;
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
import com.of.music.adapter.OnMoreClickListener;
import com.of.music.adapter.PlaylistAdapter;
import com.of.music.fragment.LocalMusicFragment;
import com.of.music.fragment.fragmentNet.BaseFragment;
import com.of.music.model.Imusic;
import com.of.music.services.AudioPlayer;
import com.of.music.services.MusicService;
import com.of.music.services.OnPlayerEventListener;
import com.of.music.songListInformation.Music;
import com.of.music.util.onlineUtil.FileUtils;

import java.util.ArrayList;
import java.util.List;

public class RecentlyListFragment extends BaseFragment implements AdapterView.OnItemClickListener,OnMoreClickListener {
   
   private ListView lvPlaylist;

    private PlaylistAdapter adapter;
    private List<Imusic> imusicArrayList;
   @Override
   public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.fragment_recently_list, null);
       lvPlaylist=view.findViewById(R.id.lv_playlist);
       AudioPlayer.get().init(App.sContext);
        imusicArrayList= AudioPlayer.get().getMusicList();
       adapter = new PlaylistAdapter(AudioPlayer.get().getMusicList());
       adapter.setIsPlaylist(true);
        adapter.setOnMoreClickListener(this);
        lvPlaylist.setAdapter(adapter);
        lvPlaylist.setOnItemClickListener(this);

       return view;
    }

   @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
       Log.i("recently","从Recentlylist播放");
        ArrayList<Music> musics=new ArrayList<>();
        for(int i=0;i<imusicArrayList.size();i++){
           
           Music music=new Music(imusicArrayList.get(i).getTitle(),imusicArrayList.get(i).getPath()
                  ,imusicArrayList.get(i).getAlbum(),imusicArrayList.get(i).getArtist()
                    , FileUtils.getLrcDir()+FileUtils.getLrcFileName(imusicArrayList .get(i).getArtist(), imusicArrayList.get(i).getTitle()));
         musics.add(music);
        }
        LocalMusicFragment.sMusicList=musics;
        MusicService.playingMusicIndex =position;
       Intent intent = new Intent(getActivity(), MusicService.class);
       intent.setAction(MusicService.TOGGLEPAUSE_ACTION);
       getActivity().startService(intent);
    }

   @Override
   public void onMoreClick(final int position) {
        String[] items = new String[]{"移除"};
       Imusic music = AudioPlayer.get().getMusicList().get(position);
       AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
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
}
