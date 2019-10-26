package com.example.yzbkaka.kakaweather;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ProgressDialog progressDialog;  //加载进度圈
    public LocationClient mLocationClient;  //定位服务器
    private String longitude;  //经度
    private String latitude;  //纬度

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  //先加载碎片，选择列表

        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());

        List<String> permissionsList = new ArrayList<>();  //权限列表
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            permissionsList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            permissionsList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!permissionsList.isEmpty()){
            String[] permissions = permissionsList.toArray(new String[permissionsList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }
        else{
            requestLocation();
        }
        closeProgress();  //关闭加载圈
    }


    private void requestLocation(){
        mLocationClient.start();
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
                    requestLocation();
                }
                else{
                    Toast.makeText(this, "发生位置错误", Toast.LENGTH_SHORT).show();
                    finish();
            }
            break;
        default:
        }
    }


    public class MyLocationListener implements BDLocationListener{

        @Override
        public void onReceiveLocation(final BDLocation bdLocation) {
            showProgress();  //展示加载圈
            longitude = String.valueOf(bdLocation.getLongitude());  //获得经度
            latitude = String.valueOf(bdLocation.getLatitude());  //获得纬度
            closeProgress();
        }
    }


    public void showProgress(){
        if(progressDialog == null){
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在定位中...");
            progressDialog.setCancelable(false);
        }
        progressDialog.show();
    }


    public void closeProgress(){
        if(progressDialog != null){
            progressDialog.dismiss();
            Intent intent = new Intent(MainActivity.this,WeatherActivity.class);
            intent.putExtra("longitude",longitude);
            intent.putExtra("latitude",latitude);
            startActivity(intent);
            finish();
        }
    }
}
