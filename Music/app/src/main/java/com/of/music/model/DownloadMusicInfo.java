package com.of.music.model;

/**
 * Created by hzwangchenyan on 2017/8/11.
 */
public class DownloadMusicInfo {
    private String title;
    private String musicPath;
    private String coverPath;
    private String lrcPath;
    public DownloadMusicInfo(String title, String musicPath, String coverPath,String lrcPath) {
        this.title = title;
        this.musicPath = musicPath;
        this.coverPath = coverPath;
        this.lrcPath=lrcPath;
    }
     public  String getLrcPath(){return lrcPath;}
    public String getTitle() {
        return title;
    }

    public String getMusicPath() {
        return musicPath;
    }

    public String getCoverPath() {
        return coverPath;
    }
}
