package com.example.yzbkaka.kakaweather.db;

import org.litepal.crud.LitePalSupport;

/**
 * Created by yzbkaka on 19-3-6.
 */

public class City extends LitePalSupport {
    private int id;
    private String cityName;
    private int cityCode;
    private int provinceId;  //只有知道了省份号才能够找到相应的城市

    public int getId() {
        return id;
    }

    public String getCityName() {
        return cityName;
    }

    public int getProvinceId() {
        return provinceId;
    }

    public int getCityCode() {
        return cityCode;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }

    public void setCityCode(int cityCode) {
        this.cityCode = cityCode;
    }
}
