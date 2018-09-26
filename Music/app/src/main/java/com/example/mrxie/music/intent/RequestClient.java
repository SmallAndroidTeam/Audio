package com.example.mrxie.music.intent;

import android.util.Log;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * 网络请求客户端
 */

public class RequestClient {

    /**
     * 生成请求和响应对应的日志记录
     */
    private static HttpLoggingInterceptor createLoggingInterceptor() {
        //实例化日志记录拦截对象
        HttpLoggingInterceptor loggingInterceptor = new
                HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {


            @Override
            public void log(String message) {
//                if (BuildConfig.DEBUG && !TextUtils.isEmpty(message))
                Log.i("message--->", message);

            }
        });
        //设置等级为详细记录
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return loggingInterceptor;
    }

    /**
     * 添加请求参数
     */
    private static Interceptor addRequestParams(final Map<String, String>
                                                        requestParams) {
        Interceptor addRequestParamsInterceptor = new Interceptor() {//创建添加请求参数的拦截器
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();//获取请求对象
                HttpUrl.Builder builder = request.url().newBuilder();
                //获取请求的url的创建者
                for (String key : requestParams.keySet())//依次添加所有的请求参数
                    builder.addQueryParameter(key, requestParams.get(key));
                HttpUrl url = builder.build();
                request = request.newBuilder().url(url).build();
                return chain.proceed(request);
            }
        };
        return addRequestParamsInterceptor;//返回添加参数拦截器
    }

    /**
     * 创建ssl套接字
     */
    private static SSLSocketFactory createSSLSocket() {
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws
                        CertificateException {

                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws
                        CertificateException {

                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, new SecureRandom());
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null == sslContext ? null : sslContext.getSocketFactory();
    }

    /**
     * 请求方法
     *
     * @param requestParams 请求参数
     */
    public static OkHttpClient requestAction(Map<String, String> requestParams) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        //实例化http客户端请求的创建者
        if (null != requestParams && 0 != requestParams.size()) {//有请求参数
            clientBuilder.addInterceptor(addRequestParams(requestParams));
            //添加请求参数拦截器
        }
        clientBuilder
                .addInterceptor(createLoggingInterceptor())//添加请求日志记录拦截器
                .retryOnConnectionFailure(false)
                .sslSocketFactory(createSSLSocket())
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                })
                .connectTimeout(10, TimeUnit.SECONDS)//设置连接超时时间为10秒
                .readTimeout(10, TimeUnit.SECONDS)//设置读超时时间
                .writeTimeout(30, TimeUnit.SECONDS);//设置写超时时间
        OkHttpClient client = clientBuilder.build();//获取请求对象
        return client;
    }
}
