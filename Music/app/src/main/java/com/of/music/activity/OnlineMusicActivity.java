package com.of.music.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.of.music.Application.App;
import com.of.music.R;
import com.of.music.adapter.Bind;
import com.of.music.adapter.OnMoreClickListener;
import com.of.music.adapter.OnlineMusicAdapter;
import com.of.music.downloadExecute.DownloadOnlineMusic;
import com.of.music.downloadExecute.PlayOnlineMusic;
import com.of.music.downloadExecute.ShareOnlineMusic;
import com.of.music.fragment.LocalMusicFragment;
import com.of.music.http.HttpCallback;
import com.of.music.http.HttpClient;
import com.of.music.info.RecentlyMusicListInfo;
import com.of.music.model.Imusic;
import com.of.music.model.LoadStateEnum;
import com.of.music.model.OnlineMusic;
import com.of.music.model.OnlineMusicList;
import com.of.music.model.SheetInfo;
import com.of.music.services.AudioPlayer;
import com.of.music.services.MusicService;
import com.of.music.songListInformation.Music;
import com.of.music.util.onlineUtil.FileUtils;
import com.of.music.util.onlineUtil.ImageUtils;
import com.of.music.util.onlineUtil.ScreenUtils;
import com.of.music.util.onlineUtil.ToastUtils;
import com.of.music.util.onlineUtil.ViewUtils;
import com.of.music.widget.AutoLoadListView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class OnlineMusicActivity extends BaseActivity implements OnItemClickListener
        , OnMoreClickListener, AutoLoadListView.OnLoadListener {
    private static final int MUSIC_LIST_SIZE = 20;

    @Bind(R.id.lv_online_music_list)
    private AutoLoadListView lvOnlineMusic;
    @Bind(R.id.ll_loading)
    private LinearLayout llLoading;
    @Bind(R.id.ll_load_fail)
    private LinearLayout llLoadFail;
    private View vHeader;
    private  static SheetInfo mListInfo;
    private OnlineMusicList mOnlineMusicList;
    public  static List<OnlineMusic> mMusicList = new ArrayList<>();
    private OnlineMusicAdapter mAdapter = new OnlineMusicAdapter(mMusicList);
    private int mOffset = 0;
     private int pos;
     private List<Imusic> imusicArrayList=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getRequestedOrientation()!= ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
            setContentView(R.layout.activity_online_music);
        setTitle(mListInfo.getTitle());
        Log.i("mListInfo","         "+mListInfo);
        initView();
        onLoad();
    }
    
    public static void setmListInfo(SheetInfo mListInfo) {
        OnlineMusicActivity.mListInfo = mListInfo;
    }
    
    @Override
    protected void onServiceBound() {
      //  mListInfo = (SheetInfo) getIntent().getSerializableExtra(Extras.MUSIC_LIST_TYPE);
//        setTitle(mListInfo.getTitle());
//        Log.i("mListInfo","         "+mListInfo);
//        initView();
//        onLoad();
    }

    private void initView() {
        vHeader = LayoutInflater.from(this).inflate(R.layout.activity_online_music_list_header, null);
        ScreenUtils.init(App.sContext);
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtils.dp2px(150));
        vHeader.setLayoutParams(params);
        lvOnlineMusic.addHeaderView(vHeader, null, false);
        lvOnlineMusic.setAdapter(mAdapter);
        lvOnlineMusic.setOnLoadListener(this);
        ViewUtils.changeViewState(lvOnlineMusic, llLoading, llLoadFail, LoadStateEnum.LOADING);

        lvOnlineMusic.setOnItemClickListener(this);
        mAdapter.setOnMoreClickListener(this);
    }

    private void getMusic(final int offset) {
        HttpClient.getSongListInfo(mListInfo.getType(), MUSIC_LIST_SIZE, offset, new HttpCallback<OnlineMusicList>() {
            @Override
            public void onSuccess(OnlineMusicList response) {
                lvOnlineMusic.onLoadComplete();
                mOnlineMusicList = response;
                if (offset == 0 && response == null) {
                    ViewUtils.changeViewState(lvOnlineMusic, llLoading, llLoadFail, LoadStateEnum.LOAD_FAIL);
                    return;
                } else if (offset == 0) {
                    initHeader();
                    ViewUtils.changeViewState(lvOnlineMusic, llLoading, llLoadFail, LoadStateEnum.LOAD_SUCCESS);
                }
                if (response == null || response.getSong_list() == null || response.getSong_list().size() == 0) {
                    lvOnlineMusic.setEnable(false);
                    return;
                }
                mOffset += MUSIC_LIST_SIZE;
                mMusicList.addAll(response.getSong_list());
                arrayList.clear();
                getMusicList.clear();
                for(int i=0;i<mMusicList.size();i++){
                    OnlineMusic onlineMusic=mMusicList.get(i);
                    play(onlineMusic,i);
                }
            
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if(getCount==0){
                            for(int i=0;i<getMusicList.size();i++){
                                arrayList.add(getMusicList.get(i));
                                Log.i("teste11111", "index: "+i+"//"+getMusicList.get(i).getTitle());
                            }
                            this.cancel();
                        }else{
                            Log.i("teste11112", "run: "+getCount);
                        }
                    }
                },0,100);
               
                Log.i("mMusicLisc",mMusicList+",,,,,,");
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFail(Exception e) {
                lvOnlineMusic.onLoadComplete();
                if (e instanceof RuntimeException) {
                    // 歌曲全部加载完成
                    lvOnlineMusic.setEnable(false);
                    return;
                }
                if (offset == 0) {
                    ViewUtils.changeViewState(lvOnlineMusic, llLoading, llLoadFail, LoadStateEnum.LOAD_FAIL);
                } else {
                    ToastUtils.show(R.string.load_fail);
                }
            }
        });
    }

    @Override
    public void onLoad() {
        getMusic(mOffset);
    }
//    private  ArrayList<Music> musicArrayList;
//    private Music musicItem;
//    private  boolean isExecuteComplete=false;

 
    @Override
    public void onItemClick(final AdapterView<?> parent, View view,final  int position, long id) {
//       OnlineMusic onlineMusic=(OnlineMusic) parent.getAdapter().getItem(position);
//
//        Log.i("ooooooo",onlineMusic.getLrclink());
//        play(onlineMusic);
//        arrayList=new ArrayList<>();
//        Music music=new Music(title,url,coverpath,artist,onlineMusic.getLrclink());
//        arrayList.add(music);
//        Log.i("ooooooo","      "+arrayList.size());
        final int index=position-1;
      new Timer().schedule(new TimerTask() {
          @Override
          public void run() {
              if(getCount==0){
                  Log.i("teste11111","      "+arrayList.size()+"///"+mMusicList.size());
                  LocalMusicFragment.sMusicList = arrayList;
                  MusicService.playingMusicIndex =index;
                  runOnUiThread(new Runnable() {
                      @Override
                      public void run() {
                          new MusicService().initMusic();
                      }
                  });
                  
                  Intent intent = new Intent(OnlineMusicActivity.this, MusicService.class);
                  intent.setAction(MusicService.TOGGLEPAUSE_ACTION);
                  startService(intent);
                  AudioPlayer.get().init(App.sContext);
                  AudioPlayer.get().addAndPlay(onlineMusicImusicMap.get((OnlineMusic) parent.getAdapter().getItem(position)));
                  this.cancel();
              }
          }
      },0,100);
      
     
    }

    @Override
    public void onMoreClick(int position) {
        final OnlineMusic onlineMusic = mMusicList.get(position);
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(mMusicList.get(position).getTitle());
        String path = FileUtils.getMusicDir() + FileUtils.getMp3FileName(onlineMusic.getArtist_name(), onlineMusic.getTitle());
        File file = new File(path);
        int itemsId = file.exists() ? R.array.online_music_dialog_without_download : R.array.online_music_dialog;
        dialog.setItems(itemsId, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:// 分享
                        share(onlineMusic);
                        break;
                    case 1:// 查看歌手信息
                        artistInfo(onlineMusic);
                        break;
                    case 2:// 下载
                        Log.i("Url", "onClick: "+onlineMusic.getSong_id());
                        download(onlineMusic);
                        break;
                }
            }
        });
        dialog.show();
    }

    private void initHeader() {
        final ImageView ivHeaderBg = vHeader.findViewById(R.id.iv_header_bg);
        final ImageView ivCover = vHeader.findViewById(R.id.iv_cover);
        TextView tvTitle = vHeader.findViewById(R.id.tv_title);
        TextView tvUpdateDate = vHeader.findViewById(R.id.tv_update_date);
        TextView tvComment = vHeader.findViewById(R.id.tv_comment);
        tvTitle.setText(mOnlineMusicList.getBillboard().getName());
        tvUpdateDate.setText(getString(R.string.recent_update, mOnlineMusicList.getBillboard().getUpdate_date()));
        tvComment.setText(mOnlineMusicList.getBillboard().getComment());
        Glide.with(this)
                .load(mOnlineMusicList.getBillboard().getPic_s640())
                .asBitmap()
                .placeholder(R.drawable.default_cover)
                .error(R.drawable.default_cover)
                .override(200, 200)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        ivCover.setImageBitmap(resource);
                        ivHeaderBg.setImageBitmap(ImageUtils.blur(resource));
                    }
                });
    }
    public ArrayList<Music> arrayList=new ArrayList<>();
    public  String url;
    public  String title;
    public  String coverpath;
    public  String artist;
    private Map<Integer,Music> getMusicList=new ArrayMap<>();
    private  boolean isExcuteComplete=false;
    private int getCount=0;
   private Map<OnlineMusic,Imusic> onlineMusicImusicMap=new HashMap<>();
    private synchronized void play(final OnlineMusic onlineMusic, final int index) {
        getCount++;
        new PlayOnlineMusic(this, onlineMusic) {
            @Override
            public void onPrepare() {
                showProgress();
            }
            @Override
            public void onExecuteSuccess(Imusic music) {
                cancelProgress();
                 onlineMusicImusicMap.put(onlineMusic,music);
                url=music.getPath();
                title=music.getTitle();
                coverpath=music.getCoverPath();
                artist=music.getArtist();
                Log.i("ooooooo",music.getPath());
                Log.i("ooooooo",title);
                Log.i("ooooooo",music.getCoverPath());
                Log.i("ooooooo",artist);
                Music music1=new Music(title,url,coverpath,artist,onlineMusic.getLrclink());
                Log.i("test11", "onExecuteSuccess: "+onlineMusic.getLrclink()+"/n:"
                +new File(onlineMusic.getLrclink()).exists());
                music1.setId(index);
                getMusicList.put(index,music1);
                getCount--;
//                ToastUtils.show("已添加到播放列表");
//                MediaPlayer mediaPlayer=new MediaPlayer();
//               try {
//                    mediaPlayer.reset();
//                    mediaPlayer.setDataSource(url);
//                    mediaPlayer.prepare();
//                    mediaPlayer.start();
//               } catch (IOException e) {
//                   e.printStackTrace();
//               }
            }

            @Override
            public void onExecuteFail(Exception e) {
                cancelProgress();
                getCount--;
                ToastUtils.show(R.string.unable_to_play);
            }
            
        }.execute();
    }

    private void share(final OnlineMusic onlineMusic) {
        new ShareOnlineMusic(this, onlineMusic.getTitle(), onlineMusic.getSong_id()) {
            @Override
            public void onPrepare() {
                showProgress();
            }

            @Override
            public void onExecuteSuccess(Void aVoid) {
                cancelProgress();
            }

            @Override
            public void onExecuteFail(Exception e) {
                cancelProgress();
            }
        }.execute();
    }

    private void artistInfo(OnlineMusic onlineMusic) {
       ArtistInfoActivity.start(this,onlineMusic.getTing_uid());
    }

    private void download(final OnlineMusic onlineMusic) {
        new DownloadOnlineMusic(this, onlineMusic) {
            @Override
            public void onPrepare() {
                showProgress();
            }

            @Override
            public void onExecuteSuccess(Void aVoid) {
                cancelProgress();
                ToastUtils.show(getString(R.string.now_download, onlineMusic.getTitle()));
            }

            @Override
            public void onExecuteFail(Exception e) {
                cancelProgress();
                ToastUtils.show(R.string.unable_to_download);
            }
        }.execute();
    }
}
