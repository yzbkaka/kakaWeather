package com.example.yzbkaka.kakaweather.GSON;

import com.google.gson.annotations.SerializedName;

/**
 * Created by yzbkaka on 19-3-13.
 */

public class Basic {
    @SerializedName("city")  //让JSON字段和Java字段建立映射关系
    public String cityName;

    @SerializedName("cid")
    public String weatherId;

    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }

    public Update update;
}
