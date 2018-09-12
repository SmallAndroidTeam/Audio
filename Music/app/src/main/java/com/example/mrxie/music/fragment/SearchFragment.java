package com.example.mrxie.music.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mrxie.music.netSearchn.SearchNetFragment;
import com.example.mrxie.music.R;
import com.example.mrxie.music.adapter.TabFragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment implements View.OnClickListener {
    private TextView local_btn;
    private TextView net_btn;
    private ViewPager myViewPager;
    private List<Fragment> list;
    private TabFragmentPagerAdapter adapter;
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.activity_main1,container,false);
        local_btn = (TextView)view.findViewById(R.id.local_btn);
        net_btn = (TextView)view.findViewById(R.id.net_btn);
        myViewPager  =(ViewPager)view.findViewById(R.id.myViewPager);


        //设置按钮点击事件
        local_btn.setOnClickListener(this);
        net_btn.setOnClickListener(this);


        //把Fragment添加到list集合里面
        list = new ArrayList<>();
        list.add(new SearchMusicFragment());
        list.add(new SearchNetFragment());

        adapter = new TabFragmentPagerAdapter(getFragmentManager(),list);
        myViewPager.setAdapter(adapter);
        myViewPager.setCurrentItem(0);
        return view;
    }

    /*
     *点击事件
     */
    public void onClick(View view){
        switch (view.getId()){
            case R.id.local_btn:
                myViewPager.setCurrentItem(0);
                local_btn.setTextColor(Color.GREEN) ;
                net_btn.setTextColor(Color.BLACK) ;
                break;
            case R.id.net_btn:
                myViewPager.setCurrentItem(1);
                net_btn.setTextColor(Color.GREEN) ;
                local_btn.setTextColor(Color.BLACK);
                break;


        }
    }
}
