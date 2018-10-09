package com.of.music.fragment.fragmentNet;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.of.music.R;
import com.of.music.Application.App;
import com.of.music.fragment.LocalMusicFragment;
import com.of.music.intent.MusicAdapter;
import com.of.music.intent.MusicNeteaseVo;
import com.of.music.intent.NetworkUtil;
import com.of.music.intent.RequestHelper;
import com.of.music.services.MusicService;
import com.of.music.songListInformation.Music;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NewSongFragment extends Fragment implements View.OnClickListener
        , AdapterView.OnItemClickListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {
    /**
     * 请求音乐数据标志码
     */
    private static final int REQUEST_MUSIC_DATA = 0x000;
    /**
     * 添加数据标志码
     */
    private static final int ADD_MUSIC_DATA = 0x001;
    /**
     * 更新界面
     */
    private static final int UPDATE_UI = 0x002;
    /**
     * 播放音乐
     */
    private static final int PLAY_MUSIC = 0x003;
    /**
     * 音乐数据
     */
//    private List<MusicVo> musics;
    private List<MusicNeteaseVo> musics;
    /**
     * 列表的Item内容和样式适配器
     */
    private MusicAdapter adapter;
    /**
     * 音乐播放工具
     */
    private MediaPlayer mediaPlayer;
    /**
     * 专辑封面
     */
    private ImageView albumIv;
    /**
     * 歌名
     */
    private TextView musicNameTv;
    /**
     * 歌手名
     */
    private TextView authorTv;
    /**
     * 播放/暂停按钮
     */
    private ImageView playerIv;
    /**
     * 歌曲列表
     */
    private ListView musicLv;
    /**
     * 播放标志，为true则为播放，否则为暂停
     */
    private boolean isPlaying;
    /**
     * 网络请求工具
     */
    private RequestHelper requestHelper;
    /**
     * 消息句柄（可以直接实力化内部类）
     */
    private Handler handler;
    /**
     * 当前播放歌曲的位置索引
     */
    private int nowPlayingIndex;
    /**
     * 消息处理回调
     */
    private Handler.Callback callback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case REQUEST_MUSIC_DATA://启动线程获取音乐列表
                    startThreadRequestMusicData();
                    break;
                case ADD_MUSIC_DATA://添加所有音乐
                    getMusicDataFromJsonStr((String) msg.obj);//对于类型强转的，要注意判空，在发消息时已经做了
                    break;
                case UPDATE_UI://更新列表
                    adapter.notifyDataSetChanged();
                    break;
                case PLAY_MUSIC://播放音乐
                    getMusicPathFromJsonStr((String) msg.obj);
            }
            return false;
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_three, null);

        albumIv = view.findViewById(R.id.album_pic_iv);
        musicNameTv = view.findViewById(R.id.music_name_tv);
        authorTv = view.findViewById(R.id.author_name_tv);
      
        musicLv = view.findViewById(R.id.musics_lv);
        initData();
        initAdapter();
        setListener();
        return view;
    }

    /**
     * 初始化
     */
    private void initData() {
        requestHelper = new RequestHelper();
        musics = new ArrayList<>();
        handler = new Handler(callback);
        mediaPlayer = new MediaPlayer();
    }

    /**
     * 初始化适配器
     */
    private void initAdapter() {
        adapter = new MusicAdapter(App.sContext, musics);
        musicLv.setAdapter(adapter);
    }

    /**
     * 设置监听
     */
    private void setListener() {
       
        musicLv.setOnItemClickListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
    }

    public void onStart() {
        super.onStart();
        if (NetworkUtil.isNetworkConnected(App.sContext)) {
            toast("正在请求数据，请稍等...");
            handler.sendEmptyMessage(REQUEST_MUSIC_DATA);
        } else
            toast("未连接网络或连接的网络不可用！");
    }

    private void toast(String toast) {
        Toast.makeText(getActivity(), toast, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mediaPlayer.start();//开始播放
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        int nextPlayIndex = (++nowPlayingIndex) % musics.size();//获取下一首歌的位置索引，最后一首歌的下一首是第一首
//        showSelectedMusicInfo(musics.get(nextPlayIndex));//显示音乐信息
        playSelectedMusic(musics.get(nextPlayIndex).url);//播放音乐
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        if (position < 0 || position + 1 > musics.size())
//            return;
        //	public Music(String title, String uri, String image, String artist, String lrcpath)
        final ArrayList<Music> arrayList = new ArrayList<>();
        for (MusicNeteaseVo musicNeteaseVo : musics) {
            Log.i("m", "onItemClick: " + musicNeteaseVo.lrc);
            Log.i("musiciamge", musicNeteaseVo.pic);
            Music music = new Music(musicNeteaseVo.title, musicNeteaseVo.url, musicNeteaseVo.pic, musicNeteaseVo.author, musicNeteaseVo.lrc);
            arrayList.add(music);
        }
        LocalMusicFragment.sMusicList = arrayList;
        MusicService.playingMusicIndex = position;
        new MusicService().initMusic();
        Intent intent = new Intent(getActivity(), MusicService.class);
        intent.setAction(MusicService.TOGGLEPAUSE_ACTION);
        getActivity().startService(intent);
        adapter.notifyDataSetChanged();
    }
//        showSelectedMusicInfo(musics.get(position));
//        playSelectedMusic(musics.get(position).url);
////        startThreadRequestMusicPath(musics.get(position).song_id);
//        nowPlayingIndex = position;
    


//    private void showSelectedMusicInfo(MusicNeteaseVo music) {
//        ImageLoaderUtil.loadPicByUrl(albumIv, music.pic);//加载图片
//        musicNameTv.setText(music.title);//显示歌名
//        authorTv.setText(music.author);//显示歌手
//    }

    /**
     * 播放选中的音乐
     *
     * @param musicPath 音乐文件路径
     */
    private void playSelectedMusic(String musicPath) {
        if (!mediaPlayer.isPlaying())
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(musicPath);
                mediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    @Override
    public void onClick(View v) {
        if (isPlaying) {
            toast("即将暂停播放");
            mediaPlayer.pause();
        } else {
            toast("即将开始播放");
            mediaPlayer.start();
        }
        isPlaying = !isPlaying;//更新播放状态
    }

    private void startThreadRequestMusicData() {
        new Thread() {
            @Override
            public void run() {
                super.run();
//                String musicData = requestHelper.getMusics("https://tingapi.ting.baidu.com/v1/restserver/ting",2,66,0);//百度的
//                String musicData = requestHelper.getMusics("https://s.music.163.com/search/get/","2","66","0","周杰伦");//网易云音乐的
                String musicData = requestHelper.getNeteaseMusics("https://api.hibai.cn/api/index/index");//网易云音乐用户的
                if (!TextUtils.isEmpty(musicData))//有数据
                    sendMusicDataMessage(musicData);//发送已获取到音乐数据消息
            }
        }.start();
    }

    /**
     * 发送获取到音乐数据消息
     *
     * @param musicData 返回的音乐数据json格式字符串
     */
    private void sendMusicDataMessage(String musicData) {
        if (null != handler) {//有消息句柄
            Message msg = handler.obtainMessage();//从消息队列里获取一个消息实例，注意避免自己创建一个消息实例，这样会造成不必要的资源浪费，每天一杯奶就够了，不要贪多哈
            msg.what = ADD_MUSIC_DATA;//设置消息区分标志
            msg.obj = musicData;//设置消息内容
            handler.sendMessage(msg);//立即发送
        }
    }

    /**
     * 启动线程获取指定音乐的详细信息（注释参考获取音乐列表方法）
     *
     * @param songId 音乐id
     */
    private void startThreadRequestMusicPath(final String songId) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                String musicPath = requestHelper.getMusicPath("https://tingapi.ting.baidu.com/v1/restserver/ting", songId);
                if (!TextUtils.isEmpty(musicPath))
                    sendMusicPathMessage(musicPath);

            }
        }.start();
    }

    /**
     * 发送获取到音乐详细信息消息（注释参考发送获取到音乐数据消息方法）
     */
    private void sendMusicPathMessage(String musicPath) {
        if (null != handler) {
            Message msg = handler.obtainMessage();
            msg.what = PLAY_MUSIC;
            msg.obj = musicPath;
            handler.sendMessage(msg);
        }
    }

    /**
     * 发送更新界面消息
     */
    private void sendUpdateUIMessage() {
        if (null != handler)
            handler.sendEmptyMessage(UPDATE_UI);//不需要消息内容的
    }

    /**
     * 将json字符串转为音乐描述数据模型（实体）
     *
     * @param jsonStr 返回的json字符串
     */
    private void getMusicDataFromJsonStr(String jsonStr) {
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);//根据json字符串实例化json对象
//            JSONArray array=jsonObject.optJSONArray("song_list");
            //注意，从json数据（类似于map）中获取内容时，如果不确定对应的key（键）是否有的，请使用opt前缀方法操作，如果确定有的话，使用get也可以，opt还可以设置默认值
            JSONArray array = jsonObject.optJSONArray("Body");//获取json对象中的json数组
//            MusicVo item;
            MusicNeteaseVo item;//声明音乐
            JSONObject json;//声明json对象
            for (int i = 0, size = array.length(); i < size; i++) {//依次获取json数组中的每个json对象
//                item=new MusicVo();
//                json= (JSONObject) array.get(i);
//                item.album_title=json.optString("album_title");
//                item.author=json.optString("author");
//                item.lrclink=json.optString("lrclink");
//                item.pic_small=json.optString("pic_small");
//                item.song_id=json.optString("song_id");
//                item.title=json.optString("title");
                //以下是数据封装
                item = new MusicNeteaseVo();
                json = (JSONObject) array.get(i);
                item.title = json.optString("title");
                item.author = json.optString("author");
                item.lrc = json.optString("lrc");
                item.pic = json.optString("pic");
                item.url = json.optString("url");
                musics.add(item);//添加到音乐列表中
            }
            item = null;
            sendUpdateUIMessage();//发送更新界面消息
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取音乐文件路径（注释参考将json字符串转为音乐描述数据模型）
     *
     * @param jsonStr 返回的json字符串
     */
    private void getMusicPathFromJsonStr(String jsonStr) {
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            JSONObject json = jsonObject.optJSONObject("bitrate");
            String musicUrl = json.optString("file_link");
//            int index=musicUrl.indexOf("?xcode");
//            if (index>-1){
//                musicUrl=musicUrl.substring(0,index);
////                musicUrl=musicUrl.replaceFirst("http","https");
//            }
            playSelectedMusic(musicUrl);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}