package com.of.music.fragment.fragmentList;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.of.music.R;
import com.of.music.Application.App;
import com.of.music.info.MusicInfo;
import com.of.music.songListInformation.Music;
import com.of.music.songListInformation.MusicUtils;
import com.of.music.ui.IConstants;
import com.of.music.util.comparator.MusicComparator;
import com.of.music.util.onlineUtil.PreferencesUtility;
import com.of.music.util.onlineUtil.SortOrder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class MusicListFragment extends Fragment {
    private static final String TAG = "MusicListFragment";
    private Adapter mAdapter;
    private RecyclerView mRecylcerview;
    private LinearLayoutManager layoutManager;
    private boolean isFirstLoad = true;
    private boolean isAZSort = true;
    private PreferencesUtility mPreferenceUtility;
    private Context mContext = App.sContext;
    private HashMap<String, Integer> positionMap = new HashMap<>();
   private ArrayList<Music> musicArrayList=new ArrayList<>();
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser){
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser) {
        //
        }
    }


    @Override
    public void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mPreferenceUtility = PreferencesUtility.getInstance(mContext);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music_list, null);

        isFirstLoad = true;
        isAZSort = mPreferenceUtility.getSongSortOrder().equals(SortOrder.SongSortOrder.SONG_A_Z);

        mRecylcerview = (RecyclerView) view.findViewById(R.id.music_list_recycleview);
        layoutManager = new LinearLayoutManager(mContext);
        mRecylcerview.setLayoutManager(layoutManager);
        mAdapter = new Adapter(null);
        mRecylcerview.setAdapter(mAdapter);
        mRecylcerview.setHasFixedSize(true);
        mRecylcerview.addItemDecoration(new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL));

        reloadAdapter();
        return view;
    }

    public void reloadAdapter(){
        if (mAdapter == null){
            return ;
        }

        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(final Void... unused){
                isAZSort = mPreferenceUtility.getSongSortOrder().equals(SortOrder.SongSortOrder.SONG_A_Z);
                ArrayList<MusicInfo> songList = (ArrayList) MusicUtils.queryMusic(mContext, IConstants.START_FROM_LOCAL);
                //
                if (isAZSort){
                    Collections.sort(songList,new MusicComparator());
                    for (int i = 0; i < songList.size(); i++){
                        if (positionMap.get(songList.get(i).sort) == null){
                            positionMap.put(songList.get(i).sort, i);
                        }
                    }
                }
                mAdapter.updateDataSet(songList);
                
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid){
                mAdapter.notifyDataSetChanged();
                if (isAZSort){
                    mRecylcerview.addOnScrollListener(scrollChangeListener);
                }else{
                    mRecylcerview.removeOnScrollListener(scrollChangeListener);
                }
                if (isFirstLoad)
                {
                    isFirstLoad = false;
                }
            }
        }.execute();
    }

    public class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        private ArrayList<MusicInfo>  mList;
        PlayMusic playMusic;
        Handler handler;
        public Adapter(ArrayList<MusicInfo> list){
            mList = list;
        }

        public void updateDataSet(ArrayList<MusicInfo> list){
            this.mList = list;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
            
            return new ListItemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_music_list_item,viewGroup,false));
        }

        //
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position){
            MusicInfo model = null;
           // Log.i("222", "onBindViewHolder: "+mList.size());
                model = mList.get(position);
            if (holder instanceof ListItemViewHolder)
            {
                ((ListItemViewHolder)holder).mainTitle.setText(model.musicName.toString());
                ((ListItemViewHolder)holder).title.setText(model.artist.toString());
            }
        }

        @Override
        public int getItemCount(){
            return (null != mList? mList.size():0);
        }//重点

        public class ListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            //ViewHolder
            ImageView playState;
            ImageView moreOverflow;
            TextView mainTitle,title;

            ListItemViewHolder(View view){
                super(view);
                this.playState = (ImageView) view.findViewById(R.id.play_state);
                this.mainTitle = (TextView) view.findViewById(R.id.viewpager_list_toptext);
                this.title = (TextView) view.findViewById(R.id.viewpager_list_bottom_text);
                this.moreOverflow = (ImageView) view.findViewById(R.id.viewpager_list_button);

                moreOverflow.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        //-------------------------------------------------------------------------------
                        // ��� moreOverflow �ĵ���¼�
                    }
                });

                view.setOnClickListener(this);
            }

            @Override
            public void onClick(View v){
                if (playMusic != null)
                    handler.removeCallbacks(playMusic);

                if (getAdapterPosition() > -1){
                    playMusic = new PlayMusic(getAdapterPosition() -1);
                    handler.postDelayed(playMusic, 70);
                }
            }
        }

        class PlayMusic implements  Runnable{
            int position;
            public PlayMusic(int position){
                this.position = position;
            }

            @Override
            public void run(){
                long[] list = new long[mList.size()];
                HashMap<Long,MusicInfo> infos = new HashMap<>();
                for (int i=0;i<mList.size();i++){
                    MusicInfo info = mList.get(i);
                    list[i] = info.songId;
                    info.islocal = true;
                    info.albumData = MusicUtils.getAlbumArtUri(info.albumId)+"";
                    infos.put(list[i],mList.get(i));
                }
                if (position > -1){
                    //------------------------------------------------------------------------------------
                    // playall
                    //
                }
            }
        }
    }



    private RecyclerView.OnScrollListener scrollChangeListener = new RecyclerView.OnScrollListener(){
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState){
            super.onScrollStateChanged(recyclerView,newState);
        }
    };
}