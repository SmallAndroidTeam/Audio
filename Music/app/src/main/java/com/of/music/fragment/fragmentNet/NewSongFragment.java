package com.of.music.fragment.fragmentNet;

import android.content.Intent;
import android.os.Bundle;
import android.renderscript.RenderScript;
import android.support.annotation.Nullable;

import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.of.music.Application.AppCache;
import com.of.music.R;
import com.of.music.activity.ControlPanel;
import com.of.music.activity.OnlineMusicActivity;
import com.of.music.adapter.Bind;
import com.of.music.adapter.SheetAdapter;
import com.of.music.model.Extras;
import com.of.music.model.Keys;
import com.of.music.model.SheetInfo;
import com.of.music.services.AudioPlayer;

import java.util.List;


public class NewSongFragment extends BaseFragment implements AdapterView.OnItemClickListener {
    @Bind(R.id.lv_sheet)
    private ListView lvPlaylist;
    
    private List<SheetInfo> mSongLists;
    
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sheet_list, container, false);
    }
    
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        mSongLists = AppCache.get().getSheetList();
        if (mSongLists.isEmpty()) {
            String[] titles = getResources().getStringArray(R.array.online_music_list_title);
            String[] types = getResources().getStringArray(R.array.online_music_list_type);
            for (int i = 0; i < titles.length; i++) {
                SheetInfo info = new SheetInfo();
                info.setTitle(titles[i]);
                info.setType(types[i]);
                mSongLists.add(info);
            }
        }
        SheetAdapter adapter = new SheetAdapter(mSongLists);
        lvPlaylist.setAdapter(adapter);
    }
    
    @Override
    protected void setListener() {
        lvPlaylist.setOnItemClickListener(this);
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SheetInfo sheetInfo = mSongLists.get(position);
        OnlineMusicActivity.setmListInfo(sheetInfo);
        Intent intent = new Intent(getContext(), OnlineMusicActivity.class);
        //intent.putExtra(Extras.MUSIC_LIST_TYPE, sheetInfo);
        startActivity(intent);
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        int position = lvPlaylist.getFirstVisiblePosition();
        int offset = (lvPlaylist.getChildAt(0) == null) ? 0 : lvPlaylist.getChildAt(0).getTop();
        outState.putInt(Keys.PLAYLIST_POSITION, position);
        outState.putInt(Keys.PLAYLIST_OFFSET, offset);
    }
    
    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        lvPlaylist.post(new Runnable() {
            @Override
            public void run() {
                int position = savedInstanceState.getInt(Keys.PLAYLIST_POSITION);
                int offset = savedInstanceState.getInt(Keys.PLAYLIST_OFFSET);
                lvPlaylist.setSelectionFromTop(position, offset);
            }
        });
    }
    }
  