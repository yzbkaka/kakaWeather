package com.example.yzbkaka.kakaweather.GSON;

import com.google.gson.annotations.SerializedName;

/**
 * Created by yzbkaka on 19-3-14.
 */

public class Forecast {
    public String date;

    @SerializedName("tmp")
    public Temperature temperature;

    @SerializedName("cond")
    public More more;

    public class Temperature {
        public String max;
        public String min;
    }

    public class More {
        @SerializedName("txt_d")
        public String info;
    }
}
