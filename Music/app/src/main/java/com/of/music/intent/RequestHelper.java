package com.of.music.intent;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 请求工具
 * 拥有的方法：
 * 1、无参数的请求方法
 * 2、带分页信息的请求方法（不指定每页大小）
 * 3、指定每页大小的带分页请求方法
 * Created by lilei on 2017/8/31.
 */

public class RequestHelper {
    private static final String REQUEST_PARAMS = "method";

    /**
     * 生成请求参数的map容器
     */
    private Map<String, String> generateMusicsRequestMap(String... args) {
        Map<String, String> requestParams = new HashMap<String, String>();
        //实例化map
        requestParams.put(REQUEST_PARAMS, "baidu.ting.billboard.billList");
//        requestParams.put("s",args[3]);
        requestParams.put("type", args[0]);//添加音乐类型
//        requestParams.put("limit",args[1]);
        requestParams.put("size", args[1]);//音乐数量
        requestParams.put("offset", args[2]);//添加需要排序标志
        return requestParams;//返回参数容器
    }

    /**
     * 从百度或网易云获取音乐方法
     */
    public String getMusics(String url, String type, String size, String offset, String searchKey) {
        OkHttpClient okHttpClient = RequestClient.requestAction(generateMusicsRequestMap(type, size, offset, searchKey));
        Request request = new Request.Builder()//构建请求
                .url(url)
                .get()
                .build();
        try {//获取返回结果
            Response response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful())//请求成功（不是业务逻辑处理成功）
                return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Map<String, String> generateMusicRequestMap(String... args) {
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put(REQUEST_PARAMS, "baidu.ting.song.play");
        requestParams.put("songid", args[0]);
        return requestParams;
    }

    /**
     * 获取音乐详细信息
     */
    public String getMusicPath(String url, String songId) {
        OkHttpClient okHttpClient = RequestClient.requestAction(generateMusicRequestMap(songId));
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful())
                return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 从网易云音乐用户获取音乐信息
     */
    public String getNeteaseMusics(String url) {
        OkHttpClient okHttpClient = RequestClient.requestAction(null);
        JSONObject jsonObject = new JSONObject();//创建json对象
        try {
            jsonObject.put("TransCode", "020112");
            jsonObject.put("OpenId", "123456789");
            JSONObject json = new JSONObject();
            json.put("SongListId", "141998290");
            jsonObject.put("Body", json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());//创建请求体
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful())
                return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
