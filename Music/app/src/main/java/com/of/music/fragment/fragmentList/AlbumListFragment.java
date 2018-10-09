package com.of.music.fragment.fragmentList;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.of.music.R;
import com.facebook.drawee.view.SimpleDraweeView;
import com.of.music.Application.App;
import com.of.music.info.AlbumInfo;
import com.of.music.songListInformation.MusicUtils;
import com.of.music.util.PreferencesUtility;
import com.of.music.util.SortOrder;
import com.of.music.util.comparator.AlbumComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class AlbumListFragment extends Fragment {

    private LinearLayoutManager layoutManager;
    private List<AlbumInfo> mAlbumList = new ArrayList<>();
    private AlbumAdapter mAdapter;
    private RecyclerView mRecylcerview;
    private PreferencesUtility mPreferences;
    private boolean isAZSort = true;
    private HashMap<String,Integer> positionMap = new HashMap<>();
    private Context mContext = App.sContext;
    private RecyclerView.ItemDecoration itemDecoration;

    @Override
    public void onCreate(final  Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mPreferences = PreferencesUtility.getInstance(mContext);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album_list, container,false);
        isAZSort = mPreferences.getAlbumSortOrder().equals(SortOrder.AlbumSortOrder.ALBUM_A_Z);
        mRecylcerview = (RecyclerView) view.findViewById(R.id.album_list_recycleview);
        layoutManager = new LinearLayoutManager(mContext);
        mRecylcerview.setLayoutManager(layoutManager);
        mRecylcerview.setHasFixedSize(true);

        mAdapter = new AlbumAdapter(null);
        mRecylcerview.setAdapter(mAdapter);
        setItemDecoration();

        reloadAdapter();
        return view;
    }

    //set item decoration
    private void setItemDecoration(){
        itemDecoration = new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL);
        mRecylcerview.addItemDecoration(itemDecoration);
    }

    public void reloadAdapter(){
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(final Void... unused){
                isAZSort = mPreferences.getAlbumSortOrder().equals(SortOrder.AlbumSortOrder.ALBUM_A_Z);
                List<AlbumInfo> albumList = MusicUtils.queryAlbums(mContext);
                if (isAZSort){
                    Collections.sort(albumList, new AlbumComparator());
                    for (int i = 0;i < albumList.size(); i++){
                        if (positionMap.get(albumList.get(i).album_sort) == null)
                            positionMap.put(albumList.get(i).album_sort, i);
                    }
                }
                mAdapter.updateDataSet(albumList);
                return null;
            }

            @Override
            protected  void onPostExecute(Void aVoid){
                if (isAZSort) {
                    mRecylcerview.addOnScrollListener(scrollListener);
                } else {
                    mRecylcerview.removeOnScrollListener(scrollListener);
                }
                mAdapter.notifyDataSetChanged();
            }
        }.execute();
    }

    private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }
    };

    public class AlbumAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        final static int FIRST_ITEM = 0;
        final static int ITEM = 1;
        private List<AlbumInfo> mList;

        public AlbumAdapter(List<AlbumInfo> list) {
            this.mList = list;
        }

        //update adapter data
        public void updateDataSet(List<AlbumInfo> list) {
            this.mList = list;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            return new ListItemViewHolder(LayoutInflater.from(viewGroup.getContext()).
                    inflate(R.layout.fragment_album_list_item, viewGroup, false));
        }

        @Override
        public int getItemViewType(int position) {
            return position == FIRST_ITEM ? FIRST_ITEM : ITEM;

        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            AlbumInfo model = mList.get(position);
            ((ListItemViewHolder) holder).title.setText(model.album_name.toString());
            ((ListItemViewHolder) holder).title2.setText(model.number_of_songs + "首" + model.album_artist);
            ((ListItemViewHolder) holder).draweeView.setImageURI(Uri.parse(model.album_art + ""));//要加“” 弹出println needs a message
/*
            //根据播放中歌曲的专辑名判断当前专辑条目是否有播放的歌曲
            if (MusicPlayer.getArtistName() != null && MusicPlayer.getAlbumName().equals(model.album_name)) {
                ((ListItemViewHolder) holder).moreOverflow.setImageResource(R.drawable.song_play_icon);
                ((ListItemViewHolder) holder).moreOverflow.setImageTintList(R.color.theme_color_primary);
            } else {
                ((ListItemViewHolder) holder).moreOverflow.setImageResource(R.drawable.list_icn_more);
            }
*/
        }

        @Override
        public int getItemCount() {
            return mList == null ? 0 : mList.size();
        }

        //ViewHolder
        public class ListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            ImageView moreOverflow;
            SimpleDraweeView draweeView;
            TextView title, title2;

            ListItemViewHolder(View view) {
                super(view);
                this.title = (TextView) view.findViewById(R.id.viewpager_list_toptext);
                this.title2 = (TextView) view.findViewById(R.id.viewpager_list_bottom_text);
                this.draweeView = (SimpleDraweeView) view.findViewById(R.id.viewpager_list_img);
                draweeView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //....
                    }
                });
                this.moreOverflow = (ImageView) view.findViewById(R.id.viewpager_list_button);
                moreOverflow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
/*
                        MoreFragment morefragment = MoreFragment.newInstance(mList.get(getAdapterPosition()).album_id + "", IConstants.ALBUMOVERFLOW);
                        morefragment.show(getFragmentManager(), "album");
*/
                    }
                });
                view.setOnClickListener(this);

            }

            @Override
            public void onClick(View v) {
/*
                FragmentTransaction transaction = ((AppCompatActivity) mContext).getSupportFragmentManager().beginTransaction();
                AlbumDetailFragment fragment = AlbumDetailFragment.newInstance(mList.get(getAdapterPosition()).album_id, false, null);
                transaction.hide(((AppCompatActivity) mContext).getSupportFragmentManager().findFragmentById(R.id.tab_container));
                transaction.add(R.id.tab_container, fragment);
                transaction.addToBackStack(null).commit();
*/
            }

        }
    }
}
