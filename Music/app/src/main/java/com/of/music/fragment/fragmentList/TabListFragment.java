package com.of.music.fragment.fragmentList;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.of.music.R;
import com.of.music.Application.App;
import com.of.music.songListInformation.MusicUtils;
import com.of.music.ui.IConstants;
import com.of.music.util.onlineUtil.CommonUtils;

import java.util.ArrayList;
import java.util.List;

//所有列表的主界面
public class TabListFragment extends Fragment implements  View.OnClickListener{

    private TextView tvRecently, tvFavority, tvSong, tvArtist, tvAlbum, tvDownload, tvUsb;
    private ViewPager vpTablist;
    private TabListFragmentAdapter mFragmentAdapter;
    private RecentlyListFragment recentlyListFragment;
    private FavoriteListFragment favoriteListFragment;
    private MusicListFragment musicListFragment;
    private ArtistsListFragment artistsListFragment;
    private AlbumListFragment albumListFragment;
    private DownloadListFragment downloadListFragment;
    private USBListFragment usbListFragment;
    private List<Fragment> mFragmentList = new ArrayList<Fragment>();

    Context mContext ;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_tablistfragment, null);

        mContext = App.sContext;

        tvRecently = (TextView) view.findViewById(R.id.tv_recently);
        tvFavority = (TextView) view.findViewById(R.id.tv_favority);
        tvSong = (TextView) view.findViewById(R.id.tv_song);
        tvArtist = (TextView) view.findViewById(R.id.tv_artist);
        tvAlbum = (TextView) view.findViewById(R.id.tv_album);
        tvDownload = (TextView) view.findViewById(R.id.tv_download);
        tvUsb = (TextView) view.findViewById(R.id.tv_usb);

        tvRecently.setOnClickListener(this);
        tvFavority.setOnClickListener(this);
        tvSong.setOnClickListener(this);
        tvArtist.setOnClickListener(this);
        tvAlbum.setOnClickListener(this);
        tvDownload.setOnClickListener(this);
        tvUsb.setOnClickListener(this);

        recentlyListFragment = new RecentlyListFragment();
        favoriteListFragment = new FavoriteListFragment();
        musicListFragment = new MusicListFragment();
        artistsListFragment = new ArtistsListFragment();
        albumListFragment = new AlbumListFragment();
        downloadListFragment = new DownloadListFragment();
        usbListFragment = new USBListFragment();
        FragmentAlter.setRecentlyFragment(recentlyListFragment);
        FragmentAlter.setDownloadFragmenet(downloadListFragment);
        mFragmentList.add(recentlyListFragment);
        mFragmentList.add(favoriteListFragment);
        mFragmentList.add(musicListFragment);
        mFragmentList.add(artistsListFragment);
        mFragmentList.add(albumListFragment);
        mFragmentList.add(downloadListFragment);
        mFragmentList.add(usbListFragment);

        mFragmentAdapter = new TabListFragmentAdapter(getFragmentManager(), mFragmentList);

        vpTablist = (ViewPager) view.findViewById(R.id.vp_tablist);
        vpTablist.setOffscreenPageLimit(7);//设置viewpager的缓存为4帧
        vpTablist.setAdapter(mFragmentAdapter);
        mFragmentAdapter.notifyDataSetChanged();
        vpTablist.setCurrentItem(2);//默认显示单曲列表
        tvSong.setTextColor(getResources().getColor(R.color.red));
        // ViewPager的监听事件
        vpTablist.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
            seleteTab(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {
      
            }
        });

        return view;
    }

    @Override
    public void onClick(View v)
    {
        resetTvColor();
        switch (v.getId()) {
        case R.id.tv_recently:
            seleteTab(0);
            break;
        case R.id.tv_favority:
            seleteTab(1);
            break;
        case R.id.tv_song:
            seleteTab(2);
            break;
        case R.id.tv_artist:
            seleteTab(3);
            break;
        case R.id.tv_album:
            seleteTab(4);
            break;
        case R.id.tv_download:
            seleteTab(5);
            break;
        case R.id.tv_usb:
          seleteTab(6);
            break;
        default:
                break;
         }
    }

    public void seleteTab(int index){
        resetTvColor();
        switch (index){
            case 0:
                vpTablist.setCurrentItem(0, true);
                tvRecently.setTextColor(getResources().getColor(R.color.red));
                break;
            case 1:
                vpTablist.setCurrentItem(1, true);
                tvFavority.setTextColor(getResources().getColor(R.color.red));
                break;
            case 2:
                vpTablist.setCurrentItem(2, true);
                tvSong.setTextColor(getResources().getColor(R.color.red));
                break;
            case 3:
                vpTablist.setCurrentItem(3, true);
                tvArtist.setTextColor(getResources().getColor(R.color.red));
                break;
            case 4:
                vpTablist.setCurrentItem(4, true);
                tvAlbum.setTextColor(getResources().getColor(R.color.red));
                break;
            case 5:
                vpTablist.setCurrentItem(5, true);
                tvDownload.setTextColor(getResources().getColor(R.color.red));
                break;
            case  6:
                vpTablist.setCurrentItem(6, true);
                tvUsb.setTextColor(getResources().getColor(R.color.red));
                break;
                
        }
    
    }
    private void resetTvColor(){
        tvRecently.setTextColor(getResources().getColor(R.color.text_color));
        tvFavority.setTextColor(getResources().getColor(R.color.text_color));
        tvSong.setTextColor(getResources().getColor(R.color.text_color));
        tvArtist.setTextColor(getResources().getColor(R.color.text_color));
        tvAlbum.setTextColor(getResources().getColor(R.color.text_color));
        tvDownload.setTextColor(getResources().getColor(R.color.text_color));
        tvUsb.setTextColor(getResources().getColor(R.color.text_color));
    }

    //设置音乐overflow条目
    private void setMusicInfo() {

        if (CommonUtils.isLollipop() && ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            loadCount(false);
        } else {
            loadCount(true);
        }
    }

    private void loadCount(boolean has) {
        int recentMusicCount = 0,favoriteMusicCount = 0, localMusicCount = 0, artistsCount = 0,albumCount = 0, downLoadCount = 0 ,usbCount = 0;
        if(has){
            try{
                //最近播放的数量
                //recentMusicCount = TopTracksLoader.getCount(MainApplication.context, TopTracksLoader.QueryType.RecentSongs);
                // 收藏的数量
                // favoriteMusicCount =
                //本地单曲的数量
                localMusicCount = MusicUtils.queryMusic(mContext, IConstants.START_FROM_LOCAL).size();
                //歌手（艺术家）的数量
                artistsCount = MusicUtils.queryArtist(mContext).size();
                //专辑的数量
                albumCount = MusicUtils.queryAlbums(mContext).size();
                //下载歌曲的数量
                //downLoadCount = DownFileStore.getInstance(mContext).getDownLoadedListAll().size();
                // usb 设备中歌曲的数量
                // usbCount =
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public class TabListFragmentAdapter extends FragmentPagerAdapter{
        List<Fragment> fragmentList = new ArrayList<Fragment>(0);
        public TabListFragmentAdapter(FragmentManager fm, List<Fragment> fragmentList) {
            super(fm);
            this.fragmentList = fragmentList;
        }

        @Override
        public Fragment getItem(int position){
            return fragmentList.get(position);
        }

        @Override
        public int getCount(){
            return fragmentList.size();
        }
    }
}
