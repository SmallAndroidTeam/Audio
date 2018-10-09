package com.of.music.fragment.fragmentSearch;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import com.of.music.R;
import com.of.music.activity.MainActivity;
import com.of.music.adapter.TabFragmentPagerAdapter;
import com.of.music.adapter.UserAdapter;
import com.of.music.fragment.LocalMusicFragment;
import com.of.music.info.User;
import com.of.music.netSearchn.MobileUtils;
import com.of.music.services.MusicService;
import com.of.music.songListInformation.MusicUtils;

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

    public static class SearchMusicFragment extends Fragment {
     private EditText et_search;
     private ListView user_list;
     private static int oldMusicIndex=-1;
     private List<User> mDatas = new ArrayList<>();
     private UserAdapter mUserAdapter;
   @Nullable
   @Override
   public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
   View view=inflater.inflate(R.layout.activity,container,false);
    for(int i = 0; i< MusicUtils.sMusicList.size(); i++)
    {
   //         Bitmap icon = MusicIconLoader.getInstance().load(MusicUtils.sMusicList.get(position).getImage());
   //         holder.icon.setImageBitmap(icon);

     String name=MusicUtils.sMusicList.get(i).getTitle();
     String image=MusicUtils.sMusicList.get(i).getImage();
     User user = new User(image,name);
     mDatas.add(user);
    }
   // initView();
    et_search = (EditText)view.findViewById(R.id.et_search);
    user_list = (ListView)view.findViewById(R.id.user_list);

    initListView();
    intiEditView();
   return view;
   }
     private void intiEditView() {
      et_search.addTextChangedListener(new TextWatcher() {
       @Override
       public void beforeTextChanged(CharSequence s, int start, int count, int after) {

       }

       @Override
       public void onTextChanged(CharSequence s, int start, int before, int count) {
   //                mAdapter.getFilter().filter(s);

      //  MobileUtils.hideInputMethod(et_search);
         UserAdapter.key=et_search.getText().toString();
        mUserAdapter.getFilter().filter(s);
                       if(UserAdapter.key==null){
                        MobileUtils.hideInputMethod(et_search);
                       }
       }

       @Override
       public void afterTextChanged(Editable s) {

       }
      });
     }

     private void initListView() {
   //        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, data);
   //        user_list.setAdapter(mAdapter);

      mUserAdapter = new UserAdapter(getActivity(),mDatas);
      user_list.setAdapter(mUserAdapter);
      user_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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

     }

   }
}
