package com.example.yzbkaka.kakaweather.db;

import org.litepal.crud.LitePalSupport;

/**
 * Created by yzbkaka on 19-3-6.
 */

public class County extends LitePalSupport {
    private int id;
    private String countyName;
    private String weatherId;  //记录所对应的天气Id
    private int cityId;  //知道了所属的城市号才能知道相应的县

    public int getId() {
        return id;
    }

    public String getCountyName() {
        return countyName;
    }

    public String getWeatherId() {
        return weatherId;
    }

    public int getCityId() {
        return cityId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public void setWeatherId(String weatherId) {
        this.weatherId = weatherId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }
}
