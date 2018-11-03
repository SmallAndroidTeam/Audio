package com.of.music.songListInformation;

/**
 * 2015年8月15日 16:34:37
 * 博文地址：http://blog.csdn.net/u010156024
 */
public class Music {
	// id title singer data time image
	private int id; // 音乐id
	private String title; // 音乐标题
	private String uri; // 音乐路径
	private int length; // 长度
	private String image; // icon
	private String artist; // 艺术家
     private String lrcpath;//歌词路径
	private long duration;
	private String album;
	public Music() {
	}

	public Music(String title, String uri, String image, String artist, String lrcpath) {//本地歌曲的构造函数
		this.title = title;
		this.uri = uri;
		this.image = image;
		this.artist = artist;
		this.lrcpath = lrcpath;
	}
	
	public Music(String title, String image, String artist, String lrcpath) {
		this.title = title;
		this.image = image;
		this.artist = artist;
		this.lrcpath = lrcpath;
	}
	
	public String getLrcpath() {
		return lrcpath;
	}

	public void setLrcpath(String lrcpath) {
		this.lrcpath = lrcpath;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}
	public String getAlbum() {
		return album;
	}
	
	public void setAlbum(String album) {
		this.album = album;
	}
	public long getDuration() {
		return duration;
	}
	
	public void setDuration(long duration) {
		this.duration = duration;
	}

}
