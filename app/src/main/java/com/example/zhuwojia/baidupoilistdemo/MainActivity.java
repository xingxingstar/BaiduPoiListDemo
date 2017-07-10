package com.example.zhuwojia.baidupoilistdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;

import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;

public class MainActivity extends AppCompatActivity implements BDLocationListener {

    private static final int BAIDU_READ_PHONE_STATE = 100;
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private LocationClient locationClient;
    private LocationClientOption option;
    private boolean isFirstLoc = true;
    private LatLng ll;
    private PoiSearch mPoiSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SDKInitializer.initialize(getApplicationContext());
        mMapView = (MapView) findViewById(R.id.bmapView);
        mMapView.removeViewAt(1);//移除百度图标
        mMapView.removeViewAt(2);//移除缩放控件
        mBaiduMap = mMapView.getMap();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 定位初始化
        locationClient = new LocationClient(this);
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(poiListener);
        showContacts();

    }

    public void showContacts() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "没有获取精准位置的权限,请手动开启定位权限", Toast.LENGTH_SHORT).show();

//            Toast.makeText(getApplicationContext(), "没有权限,请手动开启定位权限", Toast.LENGTH_SHORT).show();
            // 申请一个（或多个）权限，并提供用于回调返回的获取码（用户定义）
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE}, BAIDU_READ_PHONE_STATE);

        } else {
            locationMap();
        }

    }

    private void locationMap() {
        //设置定位条件
        option = new LocationClientOption();
        option.setOpenGps(true);        //是否打开GPS
        option.setCoorType("bd09ll");       //设置返回值的坐标类型。
        option.setScanSpan(1000);
        option.setIsNeedAddress(true);
        option.setIsNeedLocationDescribe(true);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//高精度
        option.setPriority(LocationClientOption.MIN_SCAN_SPAN);
        locationClient.setLocOption(option);
        locationClient.start();
        locationClient.requestLocation();
        locationClient.registerLocationListener(this);
    }

    @Override
    public void onReceiveLocation(BDLocation location) {
        if (null == location || mBaiduMap == null) {
            return;
        }
        if (location.getLocType() == BDLocation.TypeServerError) {
            Toast.makeText(MainActivity.this, "地图服务端网络定位失败", Toast.LENGTH_SHORT).show();
        } else if (location.getLocType() == BDLocation.TypeNetWorkException) {

            Toast.makeText(MainActivity.this, "网络不通导致定位失败，请检查网络是否通畅", Toast.LENGTH_SHORT).show();
        } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
            Toast.makeText(MainActivity.this, "无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机", Toast.LENGTH_SHORT).show();
        }

        //是否是第一次定位
        if (isFirstLoc) {
            isFirstLoc = false;
            //定位数据
            MyLocationData data = new MyLocationData.Builder()
                    //定位精度bdLocation.getRadius()
                    .accuracy(0)
                    //经度
                    .latitude(location.getLatitude())
                    //纬度
                    .longitude(location.getLongitude())
                    //构建
                    .build();


            //设置定位数据
            mBaiduMap.setMyLocationData(data);
            ll = new LatLng(location.getLatitude(), location.getLongitude());
            MapStatusUpdate msu = MapStatusUpdateFactory.newLatLngZoom(ll, mBaiduMap.getMaxZoomLevel());
            MapStatusUpdate u2 = MapStatusUpdateFactory.newLatLng(ll);
            mBaiduMap.setMapStatus(u2);
            mBaiduMap.animateMapStatus(msu);
            String describe = location.getLocationDescribe();
            Button button = new Button(getApplicationContext());
            button.setBackgroundResource(R.color.sx_white);
            button.setText("我"+describe+">");
            button.setPadding(30,0,30,0);
            button.setTextColor(getResources().getColor(R.color.sx_black));
//定义用于显示该InfoWindow的坐标点
            LatLng pt = new LatLng(location.getLatitude(),location.getLongitude());
//创建InfoWindow , 传入 view， 地理坐标， y 轴偏移量
            InfoWindow mInfoWindow = new InfoWindow(button, pt, -47);
//显示InfoWindow
            mBaiduMap.showInfoWindow(mInfoWindow);
            mPoiSearch.searchNearby(new PoiNearbySearchOption()
                    .location(pt)
                    .keyword("美食")
                    .radius(5000)
                   );
        }

    }
    OnGetPoiSearchResultListener poiListener = new OnGetPoiSearchResultListener(){
        public void onGetPoiResult(PoiResult result){
            //获取POI检索结果
            int totalPageNum = result.getTotalPageNum();
            List<PoiInfo> allPoi = result.getAllPoi();
          if(allPoi!=null){
              for (int i = 0; i < allPoi.size(); i++) {
                  LatLng location = allPoi.get(i).location;
                  BitmapDescriptor bitmap = BitmapDescriptorFactory
                          .fromResource(R.mipmap.fyxq_fyzb);
                  OverlayOptions option = new MarkerOptions()
                          .position(location)
                          .icon(bitmap);
                  mBaiduMap.addOverlay(option);
              }
          }
        }
        public void onGetPoiDetailResult(PoiDetailResult result){
            //获取Place详情页检索结果

        }

        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            // requestCode即所声明的权限获取码，在checkSelfPermission时传入
            case BAIDU_READ_PHONE_STATE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 获取到权限，作相应处理（调用定位SDK应当确保相关权限均被授权，否则可能引起定位失败）
                    locationMap();
                } else {
                    // 没有获取到权限，做特殊处理
                    new AppSettingsDialog.Builder(MainActivity.this).setTitle("定位权限申请").setRationale("请选择定位权限").setNegativeButton("取消").setPositiveButton("确定").build().show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }
}
