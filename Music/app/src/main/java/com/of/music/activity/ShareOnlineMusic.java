package com.of.music.activity;

import android.content.Context;
import android.content.Intent;

import com.of.music.R;
import com.of.music.http.HttpCallback;
import com.of.music.http.HttpClient;
import com.of.music.model.DownloadInfo;
import com.of.music.model.IExecutor;
import com.of.music.util.ToastUtils;


/**
 * 分享在线歌曲
 * Created by hzwangchenyan on 2016/1/13.
 */
public abstract class ShareOnlineMusic implements IExecutor<Void> {
    private Context mContext;
    private String mTitle;
    private String mSongId;

    public ShareOnlineMusic(Context context, String title, String songId) {
        mContext = context;
        mTitle = title;
        mSongId = songId;
    }

    @Override
    public void execute() {
        onPrepare();
        share();
    }

    private void share() {
        // 获取歌曲播放链接
        HttpClient.getMusicDownloadInfo(mSongId, new HttpCallback<DownloadInfo>() {
            @Override
            public void onSuccess(DownloadInfo response) {
                if (response == null) {
                    onFail(null);
                    return;
                }

                onExecuteSuccess(null);
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, mContext.getString(R.string.share_music, mContext.getString(R.string.app_name),
                        mTitle, response.getBitrate().getFile_link()));
                mContext.startActivity(Intent.createChooser(intent, mContext.getString(R.string.share)));
            }

            @Override
            public void onFail(Exception e) {
                onExecuteFail(e);
                ToastUtils.show(R.string.unable_to_share);
            }
        });
    }
}
