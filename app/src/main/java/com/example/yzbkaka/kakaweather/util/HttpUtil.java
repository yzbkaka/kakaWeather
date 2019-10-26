package com.example.yzbkaka.kakaweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by yzbkaka on 19-3-6.
 */


//向服务器发送请求
public class HttpUtil {
    public static void sendOkHttpRequest(String adress,okhttp3.Callback callback){
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(adress).build();
        okHttpClient.newCall(request).enqueue(callback);  //enqueue()方法会调用回调机制
    }
}