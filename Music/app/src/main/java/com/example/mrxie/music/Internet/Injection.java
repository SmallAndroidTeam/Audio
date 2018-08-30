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

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

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

