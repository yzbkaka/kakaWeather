package com.example.yzbkaka.kakaweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  //先加载碎片，选择列表
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        if(pref.getString("weather",null) != null){  //之前已经请求过数据，就不用再选择界面了
            Intent intent = new Intent(this,WeatherActivity.class);
            startActivity(intent);
        }

    }
}
