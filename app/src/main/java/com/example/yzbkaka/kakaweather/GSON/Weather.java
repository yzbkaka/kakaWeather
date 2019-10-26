package com.example.yzbkaka.kakaweather.GSON;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by yzbkaka on 19-3-14.
 */

public class Weather {
    public String status;

    public Basic basic;

    public AQI aqi;

    public Now now;

    public Suggestion suggestion;

    @SerializedName("daily_forecast")
    public List<Forecast> forecastList; //需要定义成一个数组
}
