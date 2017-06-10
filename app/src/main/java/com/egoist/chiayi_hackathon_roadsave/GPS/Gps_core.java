package com.egoist.chiayi_hackathon_roadsave.GPS;

/**
 * Created by Egoist on 2017/6/10.
 */

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by Egoist on 2016/5/18.
 */
public class Gps_core {
    private static Context mContext;
    /** GPS */
    private final String TAG = "=== Map Demo ==>";
    private LocationManager locationMgr;
    private String provider;

    //!!注意 使用前需要實例化 Gps_core()
    public Gps_core (Context context){this.mContext = context;}

    /**
     * GPS初始化，取得可用的位置提供器
     * @return
     */
    public boolean initLocationProvider() {
        locationMgr = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        if (locationMgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
            return true;
        }

        return false;
    }

    public void removeGPS(){
        if(initLocationProvider()){
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkLocationPermission();
            }
            locationMgr.removeUpdates(locationListener);	//離開頁面時停止更新
        }
    }

    /**
     * 執行"我"在哪裡
     * 1.建立位置改變偵聽器
     * 2.預先顯示上次的已知位置
     */
    public void whereAmI(){
//		String provider = LocationManager.GPS_PROVIDER;

        //取得上次已知的位置 (檢查權限)
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //User has previously accepted this permission
            if (ActivityCompat.checkSelfPermission(mContext,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //一樣的事
                Location location = locationMgr.getLastKnownLocation(provider);
                updateWithNewLocation(location);
                //GPS Listener
                locationMgr.addGpsStatusListener(gpsListener);
                //Location Listener
                long minTime = 5000;//ms
                float minDist = 5.0f;//meter
                locationMgr.requestLocationUpdates(provider, minTime, minDist, locationListener);
            }
        } else {
            //一樣的事
            Location location = locationMgr.getLastKnownLocation(provider);
            updateWithNewLocation(location);
            //GPS Listener
            locationMgr.addGpsStatusListener(gpsListener);
            //Location Listener
            long minTime = 5000;//ms
            float minDist = 5.0f;//meter
            locationMgr.requestLocationUpdates(provider, minTime, minDist, locationListener);
        }

    }

    /**
     * 顯示"我"在哪裡
     * @param lat
     * @param lng
     * @param title
     * @param tag
     */
    public static void showMarkerMe(double lat, double lng,String title,String tag){
        /*if (MapActivity.mMarkerMe != null) {
            MapActivity.mMarkerMe.remove();
        }*/

        MarkerOptions markerOpt = new MarkerOptions();
        markerOpt.position(new LatLng(lat, lng));
        markerOpt.title(title);
        markerOpt.snippet(tag);
        //Toast.makeText(mContext, "lat:" + lat + ",lng:" + lng, Toast.LENGTH_SHORT).show();
    }

    //移動攝影機跟著"我"
    public void cameraFocusOnMe(double lat, double lng){
        CameraPosition camPosition = new CameraPosition.Builder()
                .target(new LatLng(lat, lng))
                .zoom(16)
                .build();
    }

    //畫路線的
    /*private void trackToMe(double lat, double lng){
        if (traceOfMe == null) {
            traceOfMe = new ArrayList<LatLng>();
        }
        traceOfMe.add(new LatLng(lat, lng));

        PolylineOptions polylineOpt = new PolylineOptions();
        for (LatLng latlng : traceOfMe) {
            polylineOpt.add(latlng);
        }

        polylineOpt.color(Color.RED);

        Polyline line = mMap.addPolyline(polylineOpt);
        line.setWidth(10);
    }*/

    /**
     * 更新並顯示新位置
     * @param location
     */
    private void updateWithNewLocation(Location location) {
        String where = "";
        if (location != null) {
            //經度
            double lng = location.getLongitude();
            //緯度
            double lat = location.getLatitude();
            //速度
            float speed = location.getSpeed();
            //時間
            long time = location.getTime();
            String timeString = getTimeString(time);

            where = "經度: " + lng +
                    "\n緯度: " + lat +
                    //"\n速度: " + speed +
                    //"\n時間: " + timeString +
                    "\nProvider: " + provider;

            //標記"我"
            //showMarkerMe(lat, lng);  //暫時不標記mark

            cameraFocusOnMe(lat, lng); //追蹤鏡頭就好


        }else{
            where = "無法定位\n請檢查GPS是否開啟，並給予權限";
        }

        //位置改變顯示
        Toast.makeText(mContext, where, Toast.LENGTH_SHORT).show();
    }


    GpsStatus.Listener gpsListener = new GpsStatus.Listener() {

        @Override
        public void onGpsStatusChanged(int event) {
            switch (event) {
                case GpsStatus.GPS_EVENT_STARTED:
                    //Log.d(TAG, "GPS_EVENT_STARTED");
                    //Toast.makeText(mContext, "GPS_EVENT_STARTED", Toast.LENGTH_SHORT).show();
                    break;

                case GpsStatus.GPS_EVENT_STOPPED:
                    //Log.d(TAG, "GPS_EVENT_STOPPED");
                    //Toast.makeText(mContext, "GPS_EVENT_STOPPED", Toast.LENGTH_SHORT).show();
                    break;

                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    //Log.d(TAG, "GPS_EVENT_FIRST_FIX");
                    //Toast.makeText(mContext, "GPS_EVENT_FIRST_FIX", Toast.LENGTH_SHORT).show();
                    break;

                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    //Log.d(TAG, "GPS_EVENT_SATELLITE_STATUS");
                    break;
            }
        }
    };


    LocationListener locationListener = new LocationListener(){

        @Override
        public void onLocationChanged(Location location) {
            updateWithNewLocation(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            updateWithNewLocation(null);
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.OUT_OF_SERVICE:
                    //Log.v(TAG, "Status Changed: Out of Service");
                    //Toast.makeText(mContext, "Status Changed: Out of Service",
                    //        Toast.LENGTH_SHORT).show();
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    //Log.v(TAG, "Status Changed: Temporarily Unavailable");
                    //Toast.makeText(mContext, "Status Changed: Temporarily Unavailable",
                    //        Toast.LENGTH_SHORT).show();
                    break;
                case LocationProvider.AVAILABLE:
                    //Log.v(TAG, "Status Changed: Available");
                    //Toast.makeText(mContext, "Status Changed: Available",
                    //        Toast.LENGTH_SHORT).show();
                    break;
            }
        }

    };

    private String getTimeString(long timeInMilliseconds){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(timeInMilliseconds);
    }

    /*
    * TODO Android 6.0以上要檢查定位權限
    * */
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                //  TODO: Prompt with explanation!

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions((Activity)mContext,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions((Activity)mContext,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

}
