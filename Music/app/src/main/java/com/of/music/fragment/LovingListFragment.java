package com.of.music.fragment;

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
import com.of.music.adapter.SongNameAdapt;
import com.of.music.db.MusicOperator;
import com.of.music.info.MusicName;
import com.of.music.services.MusicService;
import com.of.music.songListInformation.Music;

import java.util.ArrayList;
import java.util.List;


public class LovingListFragment extends Fragment {
    private MusicOperator lxrOperator;
    private String TAG = "Music";
      ListView lv;
    private ArrayList<Music> musicList = new ArrayList<Music>();
    SwipeRefreshLayout mSwipeRefreshLayout;
    public  static List<MusicName> stringList = new ArrayList<MusicName>();
    private SimpleAdapter simpleAdapter;
    private static final int SET_RANKINGBEAN = 123;
     SongNameAdapt songNameAdapt;
    MyBoadCast broadcastReceiver;
    private static int oldMusicIndex1=-1;//上次点击歌单的下标，开始设置为-1，则没有点击过歌单
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_one, container, false);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl);
        lv = (ListView) view.findViewById(R.id.lv1);
        lxrOperator = new MusicOperator(getActivity());

        stringList = lxrOperator.queryMany();
        songNameAdapt = new SongNameAdapt(getActivity(), stringList);

        lv.setAdapter(songNameAdapt);
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
//        stringList = lxrOperator.queryAlllxr();
//        Log.i(TAG, "the number of list  " + stringList.size());
//        List<Map<String, Object>> ListMap = new ArrayList<>();
//        for (int i = 0; i < stringList.size(); i++) {
//            Map map = new HashMap();
//            map.put("MusicName", stringList.get(i).getName());
//            ListMap.add(map);
//        }
//
//        simpleAdapter = new SimpleAdapter(getActivity(), ListMap, R.layout.item, new String[]{"MusicName"}, new int[]{R.id.MusicName});
//
//        lv.setAdapter(simpleAdapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                musicList.clear();
                Log.i(TAG, "aaaaaaaaa "+stringList.size());

                for(int j=0;j<stringList.size();j++)
                {    Music music =new Music();
                    music.setImage(stringList.get(j).getImage());
                    music.setTitle(stringList.get(j).getName());
                    music.setArtist(stringList.get(j).getArtist());
                    music.setUri(stringList.get(j).getUri());
                    music.setLrcpath(stringList.get(j).getLrc_uri());
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
                Log.i("Music", "onItemClick: "+oldMusicIndex1+"//"+i);


            }
        });




        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getActivity(), "long click:" + stringList.get(i).toString(), Toast.LENGTH_SHORT).show();
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

//    public Handler handler = new Handler() {
//        int i = 0;
//
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case SET_RANKINGBEAN:
//
//                    if(songNameAdapt!=null){
//                     //   songNameAdapt.notifyDataSetChanged();
//                        Log.i(TAG, "aaaaaaaaa ");}
//                    else{
//                        Log.i(TAG, "bbbbbbb: ");
//                    }
//
//                    Log.i(TAG, "click!!!!!: " + (++i));
//                    break;
//                default:
//                    break;
//            }
//        }
//
//    };

public class MyBoadCast extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("Music", "onReceive: 广播接受成功");
        stringList.clear();
 Log.i("Music", "歌曲数目"+stringList.size());
        stringList = lxrOperator.queryMany();
        Log.i("Music", "歌曲数目"+stringList.size());
       SongNameAdapt songNameAdapt1 = new SongNameAdapt(getActivity(), stringList);
        lv.setAdapter(songNameAdapt1);
    }
}

}