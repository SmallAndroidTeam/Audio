package com.example.mrxie.music.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.mrxie.music.R;
import com.example.mrxie.music.fragment.LocalMusicFragment;
import com.example.mrxie.music.services.MusicService;
import com.example.mrxie.music.songListInformation.Music;
import com.example.mrxie.music.songListInformation.MusicUtils;
import com.example.mrxie.music.activity.MainActivity;
import com.example.mrxie.music.adapter.ArtistAdapt;

import java.util.ArrayList;
import java.util.List;

public class SingerListFragment extends Fragment {


    private ArtistAdapt    artistAdapter = new ArtistAdapt();
    ListView lv;

    SwipeRefreshLayout mSwipeRefreshLayout;
     public static  List<Music> musicList = new ArrayList<Music>();
    private static int oldMusicIndex=-1;//上次点击歌单的下标，开始设置为-1，则没有点击过歌单

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.songlist, container, false);
        mSwipeRefreshLayout=(SwipeRefreshLayout)view.findViewById(R.id.srl1);
        lv=(ListView)view.findViewById(R.id.lv2)  ;
        for(int k=0;k<MusicUtils.sMusicList.size();k++)
        {
            musicList.add(MusicUtils.sMusicList.get(k));
        }
        for(int i=0; i<musicList.size()-1; i++){
            String temp = musicList.get(i).getArtist();
            for(int j=i+1; j<musicList.size(); j++){
                if(temp.equals(musicList.get(j).getArtist())){
                    musicList.remove(j);
                    j-- ;
                }
            }
        }
        initData();
        return view;
    }

    private void initData() {

        lv.setAdapter(artistAdapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(!(LocalMusicFragment.sMusicList).equals(MusicUtils.sMusicList))//点击之后变化歌单，如果当前歌单和此歌单不一致，则把当前的歌词设置为此歌单
                {
                    LocalMusicFragment.sMusicList=MusicUtils.sMusicList;
                }

                //设置当前播放的音乐下标
                if(oldMusicIndex==i){//如果点击的相同的歌曲,就会进入播放界面
                    MainActivity.getmLocalMusicButton().callOnClick();
                }else{
                    MusicService.playingMusicIndex=i;
                    new MusicService().initMusic();//初始化当前播放的歌曲
                    //发送服务给MusicSerice播放歌曲
                    Intent intent=new Intent(view.getContext(),MusicService.class);
                    intent.setAction(MusicService.TOGGLEPAUSE_ACTION);
                    view.getContext().startService(intent);
                    oldMusicIndex=i;
                }
                Log.i("Music", "onItemClick: "+oldMusicIndex+"//"+i);

                // OnlyOneToast.makeText(view.getContext(), String.valueOf(i));
            }
        });
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
           //     Toast.makeText(getActivity(), "long click:" + stringList.get(i).toString(), Toast.LENGTH_SHORT).show();
                return true;
            }
        });


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
}