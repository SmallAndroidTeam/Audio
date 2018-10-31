package com.of.music.fragment.fragmentList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.of.music.R;
import com.of.music.activity.MainActivity;
import com.of.music.adapter.FavouriteListAdapt;
import com.of.music.adapter.SongNameAdapt;
import com.of.music.db.MusicOperator;
import com.of.music.fragment.LocalMusicFragment;
import com.of.music.fragment.LovingListFragment;
import com.of.music.info.FavouriteMusicListInfo;
import com.of.music.info.MusicName;
import com.of.music.services.MusicService;
import com.of.music.songListInformation.Music;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

public class FavoriteListFragment extends Fragment {
    public static List<FavouriteMusicListInfo> favouriteMusicListInfos;
    private static String TAG = "FavoriteListFragment";
    ListView lv;
    private ArrayList<Music> musicList = new ArrayList<Music>();
    SwipeRefreshLayout mSwipeRefreshLayout;
    FavouriteListAdapt favouriteListAdapt;
    MyBoadCast broadcastReceiver;
    private static int oldMusicIndex1=-1;//上次点击歌单的下标，开始设置为-1，则没有点击过歌单
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {//container参数实际上是添加fragment时，包裹fragment的view
        LitePal.getDatabase();
        View view = inflater.inflate(R.layout.fragment_download_list, container, false);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl_favouritelist);
        lv = (ListView) view.findViewById(R.id.lv1_favouritelist);
        favouriteMusicListInfos = LitePal.findAll(FavouriteMusicListInfo.class);
        favouriteListAdapt = new FavouriteListAdapt(getActivity(),favouriteMusicListInfos);
        lv.setAdapter(favouriteListAdapt);
        initData();
        broadcastReceiver = new MyBoadCast();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.of.music.MY_BROADCAST");
        getActivity().registerReceiver(broadcastReceiver, filter);

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            getActivity().unregisterReceiver(broadcastReceiver);
        }catch (RuntimeException e){
            e.printStackTrace();
        }

    }

    //
    private void initData() {
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                musicList.clear();
                Log.i(TAG, "favouriteMusicListInfos的大小（size）:    "+favouriteMusicListInfos.size());

                for(int j=0;j<favouriteMusicListInfos.size();j++)
                {    Music music =new Music();
                    music.setImage(favouriteMusicListInfos.get(j).getImage());
                    music.setTitle(favouriteMusicListInfos.get(j).getName());
                    music.setArtist(favouriteMusicListInfos.get(j).getArtist());
                    music.setUri(favouriteMusicListInfos.get(j).getUri());
                    music.setLrcpath(favouriteMusicListInfos.get(j).getLrc_uri());
                    musicList.add(music);
                }

                if(!(LocalMusicFragment.sMusicList).equals(musicList))//点击之后变化歌单，如果当前歌单和此歌单不一致，则把当前的歌词设置为此歌单
                {
                    LocalMusicFragment.sMusicList=musicList;
                }

                //设置当前播放的音乐下标
                if(oldMusicIndex1==i){//如果点击的相同的歌曲,就会进入播放界面
                    new MainActivity().getmLocalMusicButton().callOnClick();
                }else{
                    MusicService.playingMusicIndex=i;
                    new MusicService().initMusic();//初始化当前播放的歌曲
                    //发送服务给MusicSerice播放歌曲
                    Intent intent=new Intent(view.getContext(),MusicService.class);
                    intent.setAction(MusicService.TOGGLEPAUSE_ACTION);
                    view.getContext().startService(intent);
                    oldMusicIndex1=i;
                }
                Log.i("Music", "当前音乐的下标oldMusicIndex1:   "+oldMusicIndex1+"//"+i);


            }
        });




        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getActivity(), "long click:  " + favouriteMusicListInfos.get(i).toString(), Toast.LENGTH_SHORT).show();
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


    public class MyBoadCast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("Music", "onReceive: 广播接受成功");
            favouriteMusicListInfos.clear();
            Log.i("Music", "清理后，收藏列表（favouriteMusicListInfos）的歌曲数目：  "+favouriteMusicListInfos.size());
            favouriteMusicListInfos = LitePal.findAll(FavouriteMusicListInfo.class);
            Log.i("Music", "查找后，收藏列表（favouriteMusicListInfos）的歌曲数目"+favouriteMusicListInfos.size());
            FavouriteListAdapt favouriteListAdapt = new FavouriteListAdapt(getActivity(),favouriteMusicListInfos);
            lv.setAdapter(favouriteListAdapt);
        }
    }
}
