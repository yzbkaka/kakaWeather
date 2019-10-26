package com.example.yzbkaka.kakaweather.GSON;

import com.google.gson.annotations.SerializedName;

/**
 * Created by yzbkaka on 19-3-14.
 */

public class Now {
    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    public class More {
        @SerializedName("txt")
        public String info;
    }

}
