package com.example.yzbkaka.kakaweather;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.yzbkaka.kakaweather.db.City;
import com.example.yzbkaka.kakaweather.db.County;
import com.example.yzbkaka.kakaweather.db.Province;
import com.example.yzbkaka.kakaweather.util.HttpUtil;
import com.example.yzbkaka.kakaweather.util.Utility;

import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Response;

/**
 * Created by yzbkaka on 19-3-7.
 */

public class ChooseAreaFragment extends Fragment {
    public static final int PROVINCE_LEVEL = 1;
    public static final int CITY_LEVEL = 2;
    public static final int COUNTY_LEVEL = 3;
    private int currentLevel;  //当前的位置（省？，市？，县？）

    private Button back;  //返回按钮
    private TextView textView;  //中间显示的地区名字
    private ListView listView;  //列表
    private List<String> dataList = new ArrayList<>();  //ListView中的数据
    private ArrayAdapter<String> myadapter;  //ListView的适配器
    private List<Province> provinceList = new ArrayList<>();  //省份列表
    private List<City> cityList = new ArrayList<>();  //城市列表
    private List<County> countyList = new ArrayList<>();  //县列表
    private Province selectProvince;  //选中的省份
    private City selectCity;  //选中的城市
    private County selectCounty;  //选中的县
    private ProgressDialog progressDialog;  //加载框


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState){
        View view = inflater.inflate(R.layout.title,container,false);  //把碎片的视图加载进去
        back = (Button)view.findViewById(R.id.back);
        textView = (TextView)view.findViewById(R.id.title_name);
        listView = (ListView)view.findViewById(R.id.list);

        myadapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(myadapter);
        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if(currentLevel == PROVINCE_LEVEL){
                    selectProvince = provinceList.get(position);
                    queryCity();
                }
                else if(currentLevel == CITY_LEVEL){
                    selectCity = cityList.get(position);
                    queryCounty();
                }
                else if(currentLevel == COUNTY_LEVEL){
                    String weatherId = countyList.get(position).getWeatherId();
                    if(getActivity() instanceof MainActivity){  //当碎片在MainActivity中时
                        Intent intent = new Intent(getActivity(),WeatherActivity.class);
                        intent.putExtra("weather_id",weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    }
                    else if(getActivity() instanceof WeatherActivity){  //当碎片在WeatherActivity中时
                        WeatherActivity activity = (WeatherActivity)getActivity();  //获得activity里面的实例，然后直接调用里面的方法
                        activity.drawerLayout.closeDrawers();
                        activity.swipeRefreshLayout.setRefreshing(true);
                        activity.requestWeather(weatherId);
                    }
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentLevel == CITY_LEVEL){
                    queryProvince();
                }
                if(currentLevel == COUNTY_LEVEL){
                    queryCity();
                }
            }
        });
        queryProvince();
    }


    public void queryProvince(){  //查询省份
        textView.setText("中国");
        back.setVisibility(View.GONE);
        provinceList = LitePal.findAll(Province.class);
        if(provinceList.size()>0){  //如果数据库里面不为空
            dataList.clear();
            for(Province province : provinceList){
                dataList.add(province.getProvinceName());
            }
            myadapter.notifyDataSetChanged();  //刷新ListView
            listView.setSelection(0);  //将第0个显示在最上面
            currentLevel = PROVINCE_LEVEL;  //当前标记为为省份
        }
        else{
            String adress = "http://guolin.tech/api/china";
            queryFromServer(adress,"province");
        }
    }


    public void queryCity(){  //查询城市
        textView.setText(selectProvince.getProvinceName());
        back.setVisibility(View.VISIBLE);
        cityList = LitePal.where("provinceid = ?", String.valueOf(selectProvince.getId())).find(City.class);
        if(cityList.size()!=0){
            dataList.clear();
            for(City city : cityList){
                dataList.add(city.getCityName());
            }
            myadapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = CITY_LEVEL;  //必须在加载完成之后才能设置标记位
        }
        else{
            String adress = "http://www.guolin.tech/api/china/"+selectProvince.getProvinceCode();
            queryFromServer(adress,"city");
        }
    }


    public void queryCounty(){  //查询县
        textView.setText(selectCity.getCityName());
        back.setVisibility(View.VISIBLE);
        countyList = LitePal.where("cityid = ?",String.valueOf(selectCity.getId())).find(County.class);
        if(countyList.size()>0){
            dataList.clear();
            for(County county : countyList){
                dataList.add(county.getCountyName());
            }
            myadapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = COUNTY_LEVEL;
        }
        else{
            String adress = "http://www.guolin.tech/api/china/"+ selectProvince.getProvinceCode() + "/"+selectCity.getCityCode();
            queryFromServer(adress,"county");
        }
    }


    public void queryFromServer(String adress,final String type){  //使用okhttp进行查询省市县列表
            showProgressDialog();  //显示加载框
            HttpUtil.sendOkHttpRequest(adress,new okhttp3.Callback(){
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(okhttp3.Call call, Response response) throws IOException {
                    boolean result = false;
                    String responseData = response.body().string();  //将数据转换为String类型
                    if("province".equals(type)){  //如果查询的是省份
                        result = Utility.handleProvinceResponse(responseData);
                    }
                    if("city".equals(type)){
                        result = Utility.handleCityResponse(responseData,selectProvince.getId());
                    }
                    if("county".equals(type)){
                        result = Utility.handleCountyResponse(responseData, selectCity.getId());
                    }
                    if(result){
                        getActivity().runOnUiThread(new Runnable() {  //在主线程进行UI操作
                            @Override
                            public void run() {
                                closeProgressDialog();  //关闭加载框
                                if("province".equals(type)){
                                    queryProvince();
                                }
                                if("city".equals(type)){
                                    queryCity();
                                }
                                if("county".equals(type)){
                                    queryCounty();
                                }
                            }
                        });
                    }
                }
            });
        }


    public void showProgressDialog(){  //显示加载圈
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }


    public void closeProgressDialog(){
        if(progressDialog != null){
            progressDialog.dismiss();  //关闭加载框
        }
    }


}
