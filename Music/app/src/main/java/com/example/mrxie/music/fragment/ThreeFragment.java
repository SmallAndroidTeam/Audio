package com.example.mrxie.music.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.mrxie.music.Internet.Injection;
import com.example.mrxie.music.R;
import com.example.mrxie.music.SongListInformation.App;

import com.example.mrxie.music.Internet.Injection;
import com.example.mrxie.music.Internet.PlayBean;
import com.example.mrxie.music.Internet.SongRankingBean;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ThreeFragment extends Fragment {
    private ListView rankingsonglist;
    private MyAdapter myAdapter;
    private String rankingimageUrl;
    private String rankingnamestr;
    private MyHandler myHandle;
    public AdapterView.OnItemClickListener listener;
    private SongRankingBean songRankingBean;
    private List<String> songidlist = new ArrayList<>();
    private Context context;
    private String url;
    private List<SongRankingBean.SongListBean> rankingsongbeanlist = new ArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_three, null);
        context=App.sContext;
        myAdapter = new MyAdapter(context);
        myHandle=new MyHandler(context);
        rankingsonglist=(ListView) view.findViewById(R.id.ranking_song_list);
        rankingsonglist.setAdapter(myAdapter);
        songRankingBean = new SongRankingBean();

        // rankingimage = (ImageView) findViewById(R.id.rankingimage);
        //rankingname = (TextView) findViewById(R.id.rankingname);
        net();
        getRankingInfo(1);
        return view;
    }
    public static String getUserAgent(Context context){
        WebView webView = new WebView(context);
        webView.layout(0, 0, 0, 0);
        WebSettings settings = webView.getSettings();
        String userAgent = settings.getUserAgentString();
        Log.d("User-Agent","User-Agent: "+ userAgent);
        return  userAgent;

    }

    private void getRankingInfo(int num){
        String ua= getUserAgent(context);
        try {
            Call<SongRankingBean> call = Injection.provideSongAPI()
                    .getSongRanking("json", "", "webapp_music", "baidu.ting.billboard.billList", num, 50, 0);
            call.enqueue(new Callback<SongRankingBean>() {
                @Override
                public void onResponse(Call<SongRankingBean> call, Response<SongRankingBean> response) {
                    SongRankingBean bean = response.body();
                    setRankingsongbeanlist(bean);
                    Message message= new Message();
                    message.what= SET_RANKINGBEAN;
                    myHandle.sendMessage(message);
                }

                @Override
                public void onFailure(Call<SongRankingBean> call, Throwable t) {

                }
            });
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    private void setRankingsongbeanlist(SongRankingBean bean){
        songRankingBean = bean;
    }
    public void net(){
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());
    }
    private void getRankingSongInfo(){
        rankingimageUrl = songRankingBean.getBillboard().getPic_s210();
        rankingnamestr = songRankingBean.getBillboard().getName();
        int i;
        for(i=0; i < songRankingBean.getSong_list().size()&&i< 50;i++){
            rankingsongbeanlist.add(songRankingBean.getSong_list().get(i));
        }
        int j;
        songidlist.clear();
        for(j= 0;j<rankingsongbeanlist.size();j++){
            songidlist.add(rankingsongbeanlist.get(j).getSong_id());
        }
    }
    private void refreshListView(){
        myAdapter.notifyDataSetChanged();
//              listener.resetLickListener();
//              rankingsonglist.setOnItemClickListener(listener);
//              myposition=-1;
    }
    public class MyAdapter extends BaseAdapter {
        LayoutInflater inflater;
        private Context context;
        public MyAdapter(Context c){
            this.inflater = LayoutInflater.from(c);
            context = c;
        }
        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public int getCount() {
            return rankingsongbeanlist.size();
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if(view == null){
                view = inflater.inflate(R.layout.song_ranking_list_item,null);
                viewHolder= new ViewHolder();
                viewHolder.song_Image = (ImageView) view.findViewById(R.id.song_image);
                viewHolder.song_name = (TextView) view.findViewById(R.id.ranking_song_name);
                viewHolder.siner_name = (TextView) view.findViewById(R.id.ranking_singer_name);
                view.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) view.getTag();
            }
            Picasso.with(context).load(rankingsongbeanlist.get(i).getPic_small()).into(viewHolder.song_Image);
            viewHolder.song_name.setText(rankingsongbeanlist.get(i).getTitle());
            viewHolder.siner_name.setText(rankingsongbeanlist.get(i).getArtist_name());
            return view;
        }
    }
    class ViewHolder{
        ImageView song_Image;
        TextView song_name;
        TextView siner_name;

    }
    private static final int SET_RANKINGBEAN = 123;
    public class MyHandler extends Handler {
        private Context context;
        public MyHandler(Context context ){ this.context= context;}

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case SET_RANKINGBEAN:
                    getRankingSongInfo();
//                    Picasso.with(context).load(songRankingBean.getBillboard().getPic_s210()).into(rankingimage);
//                    rankingname.setText(songRankingBean.getBillboard().getName()+"Top 50");
                    refreshListView();
                    break;
            }
        }
    }

}