package com.example.mrxie.music.fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import  com.example.mrxie.music.R;
import com.example.mrxie.music.Service.MusicService;
import com.example.mrxie.music.adapter.MusicListAdapter;

import java.util.ArrayList;
import java.util.List;


public class songListFragment extends Fragment {


    private MusicListAdapter mMusicListAdapter = new MusicListAdapter();
    ListView lv;

    SwipeRefreshLayout mSwipeRefreshLayout;

    private List<String> stringList;
    private ArrayAdapter lvAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.songlist, container, false);
        mSwipeRefreshLayout=(SwipeRefreshLayout)view.findViewById(R.id.srl1);
        lv=(ListView)view.findViewById(R.id.lv2)  ;
        initData();
        return view;
    }

    private void initData() {

        lv.setAdapter(mMusicListAdapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//            onplay(i);

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
}