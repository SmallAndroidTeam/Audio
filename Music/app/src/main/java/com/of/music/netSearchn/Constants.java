package com.of.music.netSearchn;

public class Constants {
//    public static final String SP_NAME = "DRMPlayer";//保存状态值的名字
//    public static final String DB_NAME = "DRMPlayerDB.db";//音乐收藏数据库的名字
//    public static final int MY_RECORD_NUM = 5;//最近播放记录查询的最大数量


    //网络音乐界面 默认搜索
    public static final String MIGU_CHINA = "http://music.taihe.com/top/new/?pst=shouyeTop";
    //搜索
    public static final String MIGU_SEARCH_HEAD = "http://music.taihe.com/search?key=";
  //  public static final String MIGU_SEARCH_FOOT = "";
//    //歌词  "http://music.baidu.com/search/lrc?key=" + 歌名 + " " + 歌手
//    public static final String BAIDU_LRC_SEARCH_HEAD = "http://music.baidu.com/search/lrc?key=";

    //userAgent 属性是一个只读的字符串，声明了浏览器用于 HTTP 请求的用户代理头的值。
    //关于userAgent更多资料请看这里  http://www.w3school.com.cn/jsref/prop_nav_useragent.asp
    //在任何一个可以在线运行html的网站
    //我使用 http://tool.chinadmoz.org/htmlrun.asp
    //运行以下html代码 获得用户代理
    /**
     <html>
     <body>
     <script type="text/javascript">
     document.write("<p>UserAgent: ")
     document.write(navigator.userAgent + "</p>")
     </script>
     </body>
     </html>
     */
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36";
    //public static final String USER_AGENT = "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Win64; x64; Trident/5.0; .NET CLR 2.0.50727; SLCC2; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; InfoPath.3; .NET4.0C; Tablet PC 2.0; .NET4.0E)";
    //成功标记
    public static final int SUCCESS = 1;
    //失败标记
    public static final int FAILED = 2;

//    public static final String DIR_MUSIC = "/drm_music";
//    public static final String DIR_LRC = "/drm_music/lrc/";
}


