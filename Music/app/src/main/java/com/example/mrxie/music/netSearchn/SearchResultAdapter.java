package com.example.mrxie.music.netSearchn;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.BaseAdapter;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//
//import com.ofilm.test.viewpager1.R;
//import com.ofilm.test.viewpager1.pojo.SearchResult;
//
//import java.util.ArrayList;
//
///**
// * 自定义的音乐列表适配器(网络)
// * 为了方便扩展，因为之前没有考虑到显示专辑封面
// * Created by iwanghang on 30/4/16.
// */
//public class SearchResultAdapter extends BaseAdapter{
//
//    private Context ctx; //上下文对象引用
//    private ArrayList<SearchResult> searchResults;//存放SearchResult引用的集合
//    private SearchResult searchResult;//SearchResult对象引用
//    //private int pos = -1;			//列表位置
//
//    /**
//     * 构造函数
//     * @param ctx    上下文
//     * @param searchResults  集合对象
//     */
//    public SearchResultAdapter(Context ctx, ArrayList<SearchResult> searchResults){
//        this.ctx = ctx;
//        this.searchResults = searchResults;
//        //System.out.println("MyMusicListAdapter.java #0 : ctx = " + ctx + ",mp3Infos = " + mp3Infos.size());
//    }
//
//    public ArrayList<SearchResult> searchResults() {
//        System.out.println("NetMusicListAdapter.java #1 : public ArrayList<SearchResult> searchResults() {");
//        return searchResults;
//    }
//
//    public void setSearchResults(ArrayList<SearchResult> searchResults) {
//        System.out.println("NetMusicListAdapter.java #2 : public void setMp3Infos(ArrayList<SearchResult> searchResults) {");
//        this.searchResults = searchResults;
//    }
//
//    public ArrayList<SearchResult> getSearchResults() {
//        return searchResults;
//    }
//
//    @Override
//    public int getCount() {
//        //System.out.println("NetMusicListAdapter.java #3 : public int getCount() {" + mp3Infos.size());
//        //return mp3Infos.size();
//        return searchResults.size();
//    }
//
//    @Override
//    public Object getItem(int position) {
//        System.out.println("NetMusicListAdapter.java #4 : public Object getItem(int position) {");
//        return searchResults.get(position);
//    }
//
//    @Override
//    public long getItemId(int position) {
//        //System.out.println("NetMusicListAdapter.java #5 : public long getItemId(int position) {");
//        return position;
//    }
//
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        //System.out.println("NetMusicListAdapter.java #6 : public View getView ");
//        ViewHolder vh;
//        if(convertView==null){
//            //vh = new ViewHolder();
//            convertView = LayoutInflater.from(ctx).inflate(R.layout.item_net_music_list,null);
//            vh = new ViewHolder();
//            vh.textView1_title = (TextView) convertView.findViewById(R.id.textView1_title);
//            vh.textView2_singer = (TextView) convertView.findViewById(R.id.textView2_singer);
//
//            convertView.setTag(vh);//表示给View添加一个格外的数据，
//        }else {
//            vh = (ViewHolder)convertView.getTag();//通过getTag的方法将数据取出来
//        }
//
//        SearchResult searchResult = searchResults.get(position);
//        vh.textView1_title.setText(searchResult.getMusicName());//显示标题
//        vh.textView2_singer.setText(searchResult.getArtist());//显示歌手
//        //vh.textView3_time.setText(MediaUtils.formatTime(mp3Info.getDuration()));//显示时长
//
//        //获取专辑封面图片
//        //Bitmap albumBitmapItem = MediaUtils.getArtwork(ctx,mp3Info.getId(),mp3Info.getAlbumId(),true,true);
//        //System.out.println("NetMusicListAdapter.java #8 : albumBitmapItem = " + albumBitmapItem.getConfig());
//
//        //改变播放界面专辑封面图片
//        //vh.imageView1_ablum.setImageBitmap(albumBitmapItem);
//        //vh.imageView1_ablum.setImageResource(R.mipmap.music);
//
//        return convertView;
//    }
//
//    /**
//     * 定义一个内部类
//     * 声明相应的控件引用
//     */
//    static class ViewHolder{
//        //所有控件对象引用
//        TextView textView1_title;//标题
//        TextView textView2_singer;//歌手
//        //TextView textView3_time;//时长
//        //ImageView imageView1_ablum;//专辑封面图片
//    }
//}

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.mrxie.music.R;

import java.util.ArrayList;

public class SearchResultAdapter extends BaseAdapter {
    private ArrayList<SearchResult> mSearchResult;
    private Context ctx; //上下文对象引用
//    public SearchResultAdapter(ArrayList<SearchResult> searchResult) {
//        mSearchResult = searchResult;
//    }
public SearchResultAdapter(Context ctx, ArrayList<SearchResult> mSearchResult){
    this.ctx = ctx;
    this.mSearchResult = mSearchResult;
    //System.out.println("MyMusicListAdapter.java #0 : ctx = " + ctx + ",mp3Infos = " + mp3Infos.size());
}
    @Override
    public int getCount() {
        return mSearchResult.size();
    }

    @Override
    public Object getItem(int position) {
        return mSearchResult.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    public ArrayList<SearchResult> getSearchResults() {
       return mSearchResult;
   }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;//通常出现在适配器里，为的是listview滚动的时候快速设置值，而不必每次都重新创建很多对象，从而提升性能。
        if(convertView == null) {
            convertView = LayoutInflater.from(ctx).inflate( R.layout.search_result_item, null);
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.tv_search_result_title);
            holder.artist = (TextView) convertView.findViewById(R.id.tv_search_result_artist);
            holder.album = (TextView) convertView.findViewById(R.id.tv_search_result_album);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();//表示给View添加一个格外的数据，以后可以用getTag()将这个数据取出来。
        }

        String artist = mSearchResult.get(position).getArtist();
        String album = mSearchResult.get(position).getAlbum();

        holder.title.setText(mSearchResult.get(position).getMusicName());

        if(!TextUtils.isEmpty(artist)) holder.artist.setText(artist);
        else holder.artist.setText("未知艺术家");

        if(!TextUtils.isEmpty(album)) holder.album.setText(album);
        else holder.album.setText("未知专辑");
        return convertView;//收敛视图
    }
    public ArrayList<SearchResult> getSearchResult() {
        return mSearchResult;
    }


    static class ViewHolder {
        public TextView title;
        public TextView artist;
        public TextView album;
    }
}
