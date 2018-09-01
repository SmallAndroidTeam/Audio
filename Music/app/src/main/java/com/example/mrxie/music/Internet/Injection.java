package com.example.mrxie.music.Internet;


import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Howard on 2016/7/11.
 */
public class Injection {

    public static OkHttpClient provideOkHttpClient() {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();//HttpLoggingInterceptor()该拦截器用于记录应用中的网络请求的信息
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);//BODY 请求/响应行 + 头 + 体
                                                            //共包含四个级别：NONE、BASIC、HEADER、BODY
                                                            //NONE 不记录;BASIC 请求/响应行;HEADER 请求/响应行 + 头

        return new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
    }

    public static Retrofit provideRetrofit() {

        return new Retrofit.Builder()
                .baseUrl(SongAPI.BaseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(Injection.provideOkHttpClient())
                .build();
    }

    public static SongAPI provideSongAPI() {

        return Injection.provideRetrofit().create(SongAPI.class);
    }
}

