package com.example.yzbkaka.kakaweather;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.bumptech.glide.Glide;
import com.example.yzbkaka.kakaweather.GSON.Forecast;
import com.example.yzbkaka.kakaweather.GSON.Weather;
import com.example.yzbkaka.kakaweather.util.HttpUtil;
import com.example.yzbkaka.kakaweather.util.Utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class WeatherActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;

    /**
     * sdk定位服务器
     */
    public LocationClient mLocationClient;

    /**
     * 天气预报的总体界面
     */
    private ScrollView weatherLayout;

    /**
     * bing的图片
     */
    private ImageView bingPicImg;

    /**
     * 显示菜单的按钮
     */
    private Button navButton;

    /**
     * 上方显示的城市名
     */
    private TextView titleCity;

    /**
     * 右上方显示的更新时间
     */
    private TextView time;

    /**
     * 显示的温度
     */
    private TextView degreeText;

    /**
     * 显示的天气状况
     */
    private TextView weatherInfoText;

    /**
     * 显示之后几天预测的天气
     */
    private LinearLayout forecastLayout;

    /**
     * 显示aqi指数
     */
    private TextView aqiText;

    /**
     * 显示pm2.5指数
     */
    private TextView pm25Text;

    /**
     * 显示舒适建议
     */
    private TextView comfortText;

    /**
     * 显示洗车的建议
     */
    private TextView carWashText;

    /**
     * 显示运动的建议
     */
    private TextView sportText;

    private String mWeatherId;

    /**
     * 经度
     */
    private String longitude;

    /**
     * 纬度
     */
    private String latitude;

    SwipeRefreshLayout swipeRefreshLayout;

    DrawerLayout drawerLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weater);
        showProgress();

        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());  //注册定位器

        requestNeedPermissions();

        titleCity = (TextView)findViewById(R.id.title_city);
        time = (TextView)findViewById(R.id.update_time);
        navButton = (Button)findViewById(R.id.nav_button);

        degreeText = (TextView)findViewById(R.id.degree_text);
        weatherInfoText = (TextView)findViewById(R.id.weather_info_text);

        forecastLayout = (LinearLayout)findViewById(R.id.forecast_layout);
        aqiText = (TextView)findViewById(R.id.aqi_text);
        pm25Text = (TextView)findViewById(R.id.pm25_text);

        comfortText = (TextView)findViewById(R.id.comfort_text);
        carWashText = (TextView)findViewById(R.id.car_wash_text);
        sportText = (TextView)findViewById(R.id.sport_text);

        weatherLayout = (ScrollView)findViewById(R.id.weather_layout);  //Scroll
        bingPicImg = (ImageView)findViewById(R.id.bing_pic_img);
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);  //设置为主背景的颜色
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);

        //获得bing图片作为背景
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        weatherLayout.setVisibility(View.INVISIBLE);
        Glide.with(this).load("https://api.dujin.org/bing/1920.php").into(bingPicImg);  //使用Glide框架将图片加载到背景中

        String weatherString = prefs.getString("weather",null);
        if(weatherString != null){  //如果有weather缓存
            Weather weather = Utility.handleWeatherResponse(weatherString);  //对数据进行解析
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);  //将信息显示在界面上
        }
        else{  //没有weather缓存
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeatherId(longitude,latitude);  //向服务器请求weatherId
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {  //设置下拉刷新的方法
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });

        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    /**
     * 获得所需要的权限
     */
    public void requestNeedPermissions(){
        List<String> permissionsList = new ArrayList<>();
        if(ContextCompat.checkSelfPermission(WeatherActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            permissionsList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(WeatherActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            permissionsList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(WeatherActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!permissionsList.isEmpty()){
            String[] permissions = permissionsList.toArray(new String[permissionsList.size()]);
            ActivityCompat.requestPermissions(WeatherActivity.this,permissions,1);
        }
        else{
            mLocationClient.start();  //启动定位器
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
        switch (requestCode){
            case 1:
                if(grantResults.length > 0){
                    for(int result : grantResults){
                        if(result != PackageManager.PERMISSION_DENIED){
                            Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    mLocationClient.start();
                }
                else{
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(final BDLocation bdLocation) {
            longitude = String.valueOf(bdLocation.getLongitude());  //获得经度
            latitude = String.valueOf(bdLocation.getLatitude());  //获得纬度
            Toast.makeText(WeatherActivity.this, "定位成功", Toast.LENGTH_SHORT).show();
            closeProgress();
        }
    }

    /**
     * 根据经纬度获得weatherId
     * @param longitude
     * @param latitude
     */
    public void requestWeatherId (String longitude,String latitude){
        String url = "https://free-api.heweather.net/s6/weather/now?location=" + longitude + "," + latitude +"&key=57a1dfcb705644dd916fa7b71c3d5787";
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(WeatherActivity.this, "获取weatherId失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Weather weather = Utility.handleWeatherId(responseText);
                mWeatherId = weather.basic.weatherId;
                requestWeather(mWeatherId);
            }
        });
    }

    /**
     * 向服务器请求天气数据
     * @param weatherId
     */
    public void requestWeather(final String weatherId){
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=bc0418b57b2d4918819d3974ac1285d9";
        HttpUtil.sendOkHttpRequest(weatherUrl,new okhttp3.Callback(){
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气数据失败", Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);  //返回weather型的天气数据
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if("ok".equals(weather.status) && weather != null){
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            mWeatherId = weather.basic.weatherId;
                            showWeatherInfo(weather);
                        }
                        else{
                            Toast.makeText(WeatherActivity.this, "获取天气数据失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);  //刷新事件结束，隐藏刷新条
                    }
                });
            }
        });
    }

    /**
     * 将天气数据显示在界面上
     * @param weather
     */
    public void showWeatherInfo(Weather weather){
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityName);
        time.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);  //动态添加布局item
        }
        if (weather.aqi != null) {
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运行建议：" + weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
    }

    /**
     * 展示加载圈
     */
    public void showProgress(){
        if(progressDialog == null){
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在定位中...");
            progressDialog.setCancelable(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭加载圈
     */
    public void closeProgress(){
        if(progressDialog != null){
            requestWeatherId(longitude,latitude);
            progressDialog.dismiss();
        }
    }
}
