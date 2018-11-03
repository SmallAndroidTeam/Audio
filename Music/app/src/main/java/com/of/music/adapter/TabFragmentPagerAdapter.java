package com.of.music.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Create By rongxinglan IN 2018/8/23
 */
public class TabFragmentPagerAdapter extends FragmentPagerAdapter {
    private FragmentManager mFragmentManager;
    private List<Fragment> mList=new ArrayList<>();
    public TabFragmentPagerAdapter(FragmentManager fm, List<Fragment> list){
     super(fm);
     this.mList.clear();
     this.mList.addAll(list);
    }

    @Override
    public Fragment getItem(int page0) {
        return mList.get(page0);
    }

    @Override
    public int getCount() {
        return mList.size();
    }
}
