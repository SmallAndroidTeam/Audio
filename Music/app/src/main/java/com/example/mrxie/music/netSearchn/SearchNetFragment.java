package com.example.mrxie.music.netSearchn;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.mrxie.music.R;
import com.example.mrxie.music.songListInformation.Music;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class SearchNetFragment extends Fragment implements View.OnClickListener {

   // private ListView listView;
    private LinearLayout load_layout;
    private LinearLayout ll_search_btn_container;//查询按钮的容器
    private LinearLayout ll_search_container;//查询按钮的容器
    private ImageButton ib_search_btn;
    private EditText et_search_content;
    private SearchResultAdapter searchResultAdapter;
    private ArrayList<SearchResult> mSearchResult = new ArrayList<>();
    public   static String s;
    private int mPage = 0;//搜索音乐的页码
   private ListView listView_net_music;
    private static int oldMusicIndex1=-1;
    private  ArrayList<Music> musicList = new ArrayList<Music>();
   private   boolean hasMoreData  = false;//歌曲播放中的暂停状态

   // private int position = 0;//当前播放的位置,提供给PlayActivity

    //onAttach(),当fragment被绑定到activity时被调用(Activity会被传入.).
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //mainActivity = (MainActivity) context;
       // mainActivity = (MainActivity) getActivity();
    }

    public static SearchNetFragment newInstance() {
        SearchNetFragment net = new SearchNetFragment();
        return net;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //UI组件初始化
        View view = inflater.inflate(R.layout.search_net_music,null);
        //item
        listView_net_music = (ListView) view.findViewById(R.id.listView_net_music);
        //findViewById
        load_layout = (LinearLayout) view.findViewById(R.id.load_layout);
        ll_search_btn_container = (LinearLayout) view.findViewById(R.id.ll_search_btn_container);
        ll_search_container = (LinearLayout) view.findViewById(R.id.ll_search_container);
        ib_search_btn = (ImageButton) view.findViewById(R.id.ib_search_btn);
        et_search_content = (EditText) view.findViewById(R.id.et_search_content);
        listView_net_music.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                musicList.clear();
//                for(int j=0;j<mSearchResult.size();j++)
//                {    Music music =new Music();
//                    music.setImage(mSearchResult.get(j).getAlbum());
//                    music.setTitle(mSearchResult.get(j).getMusicName());
//                    music.setArtist(mSearchResult.get(j).getArtist());
//                    music.setUri("https://music.taihe.com"+mSearchResult.get(j).getUrl());
//
//                    musicList.add(music);
//                }

//                if(!(localMusicFragment.sMusicList).equals(musicList))//点击之后变化歌单，如果当前歌单和此歌单不一致，则把当前的歌词设置为此歌单
//                {
//                    localMusicFragment.sMusicList=musicList;
//                }
//                Log.i("music", "aaaaaaaaa "+localMusicFragment.sMusicList.get(i).getTitle());
//                Log.i("music", "aaaaaaaaa "+localMusicFragment.sMusicList.get(i).getUri());
//                //设置当前播放的音乐下标
//                if(oldMusicIndex1==i){//如果点击的相同的歌曲,就会进入播放界面
//                    new  MainActivity().getmLocalMusicButton().callOnClick();
//                }else {
//                    MusicService.playingMusicIndex = i;
//                    new MusicService().initHttpMusic();//初始化当前播放的歌曲
////                    //发送服务给MusicSerice播放歌曲
                   Intent intent = new Intent(Intent.ACTION_VIEW);
                   intent.setData(Uri.parse("https://music.taihe.com"+mSearchResult.get(i).getUrl()));
                startActivity(intent);
//                    oldMusicIndex1 = i;
//                }

            }
        });
        et_search_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                mAdapter.getFilter().filter(s);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
//        //Item点击事件监听
//        listView.setOnItemClickListener(this);
        //按钮点击事件监听
        ll_search_btn_container.setOnClickListener(this);
        ib_search_btn.setOnClickListener(this);

        loadNetData();//加载网络音乐
        return view;
    }

    private void loadNetData() {
        load_layout.setVisibility(View.VISIBLE);
        //加载网络音乐的异步任务
        new LoadNetDataTask().execute(Constants.MIGU_CHINA);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ll_search_btn_container:
                ll_search_btn_container.setVisibility(View.GONE);
                ll_search_container.setVisibility(View.VISIBLE);
                break;
            case R.id.ib_search_btn:
                //搜索事件
                ll_search_btn_container.setVisibility(View.VISIBLE);
                ll_search_container.setVisibility(View.GONE);
                searchMusic();
                break;
        }
    }


    //搜索音乐
    private void searchMusic() {
        //隐藏键盘
         MobileUtils.hideInputMethod(et_search_content);
        ll_search_btn_container.setVisibility(View.VISIBLE);
        ll_search_container.setVisibility(View.GONE);
        //获取输入的文字
        String key = et_search_content.getText().toString();
        if (TextUtils.isEmpty(key)){//如果为空,则,Toast提示
            Toast.makeText(getActivity(),"请输入歌手或歌词",Toast.LENGTH_SHORT).show();
            return;
        }
        load_layout.setVisibility(View.VISIBLE);//加载layout效果.显示
        //填充item 使用SearchMusicUtils搜索音乐工具类,并,使用观察者设计模式,自己回调,自己监听
        SearchMusic.getInstance().setListener(new SearchMusic.OnSearchResultListener(){
            @Override
            public void onSearchResult(ArrayList<SearchResult> results) {
                ArrayList<SearchResult> sr = searchResultAdapter.getSearchResults();
                sr.clear();
                sr.addAll(results);
                searchResultAdapter.notifyDataSetChanged();//更新网络音乐列表
                load_layout.setVisibility(View.GONE);
            }
        }).search(key,mPage);
    }


    //加载网络音乐的异步任务
    //Android1.5提供了 工具类 android.os.AsyncTask，它使创建异步任务变得更加简单，不再需要编写任务线程和Handler实例即可完成相同的任务。
    class LoadNetDataTask extends AsyncTask<String,Integer,Integer> {
        //onPreExecute方法用于在执行后台任务前做一些UI操作
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            load_layout.setVisibility(View.VISIBLE);//加载layout.显示
            listView_net_music.setVisibility(View.GONE);//item.隐藏
            mSearchResult.clear();//搜索结果.清理
        }

        //doInBackground方法内部执行后台任务,不可在此方法内修改UI
        @Override
        protected Integer doInBackground(String... params) {
            String url = params[0];
            try {


                //使用Jsoup组件请求网络,并解析音乐数据
                Document doc = Jsoup.connect(url).userAgent(Constants.USER_AGENT).timeout(6000).get();
                System.out.println("start**********doc**********doc**********doc**********");
                System.out.println(doc);
                System.out.println(" end **********doc**********doc**********doc**********");


                Elements songTitles = doc.select("span.song-title");
                System.out.println(songTitles);
                Elements artists = doc.select("span.author_list");
                System.out.println(artists);

                for (int i=0;i<songTitles.size();i++){
                SearchResult searchResult = new SearchResult();

                    //a链接,存在urls集合中;即,歌曲url集合;
                    //a链接,第一个a连接,href属性的值;即,最终的url;
                    Elements urls = songTitles.get(i).getElementsByTag("a");
                    //System.out.println("@urls : " + urls);
                    searchResult.setUrl(urls.get(0).attr("href"));//设置最终的url
                    searchResult.setMusicName(urls.get(0).text());//设置最终的歌名

                    //a链接,存在urls集合中;即,歌曲url集合;
                    Elements artistElements = artists.get(i).getElementsByTag("a");
                    //System.out.println("@artistElements : " + artistElements);
                    searchResult.setArtist(artistElements.get(0).text());//设置最终的歌手

                    searchResult.setAlbum("华语榜");//设置最终的专辑

                    System.out.println("@mSearchResult : " + searchResult);
                    mSearchResult.add(searchResult);//把最终的所有信息,添加到集合
                    Log.i("Music", "歌曲的数量是：");
                }
                System.out.println("@mSearchResult : " + mSearchResult);
                //System.out.println("@songTitles.size() : " + mSearchResult.size());
            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            }
            return 1;
        }

        //onPostExecute方法用于在执行完后台任务后更新UI,显示结果
        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (result==1){
                searchResultAdapter = new SearchResultAdapter(getActivity(),mSearchResult);
                //System.out.println(mSearchResult);
                listView_net_music.setAdapter(searchResultAdapter);
               listView_net_music.addFooterView(LayoutInflater.from(getActivity()).inflate(R.layout.search_result_item,null));
            }
            load_layout.setVisibility(View.GONE);
            listView_net_music.setVisibility(View.VISIBLE);

        }
    }

}
