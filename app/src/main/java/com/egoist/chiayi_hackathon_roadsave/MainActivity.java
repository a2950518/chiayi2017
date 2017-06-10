package com.egoist.chiayi_hackathon_roadsave;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.akiniyalocts.imgur_api.Constants;
import com.akiniyalocts.imgur_api.ImgurClient;
import com.akiniyalocts.imgur_api.model.Image;
import com.akiniyalocts.imgur_api.model.ImgurResponse;
import com.desmond.squarecamera.CameraActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.BitmapCallback;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;

import okhttp3.Call;
import okhttp3.Request;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

public class MainActivity extends Activity implements OnMapReadyCallback {

    private static final int REQUEST_CAMERA = 0;

    EditText mUser;
    EditText mContact;
    TextView mGPS;
    EditText mAddress;
    Spinner  mTitle;
    EditText mCaption;
    Button   mImage;
    ImageView mImageView;
    Button mSubmit;

    MapFragment mMapFrag;
    GoogleMap mMap;
    double nowLat = 0;
    double nowLon = 0;

    String PostImageUrl = "http://i.imgur.com/JvqFN04.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImgurClient.initialize(getString(R.string.imgur_clientid));

        mUser = (EditText) findViewById(R.id.main_user);
        mContact = (EditText) findViewById(R.id.main_contact);
        mGPS = (TextView) findViewById(R.id.main_gps);
        mAddress = (EditText) findViewById(R.id.main_address);
        mTitle = (Spinner) findViewById(R.id.main_title);
        mCaption = (EditText) findViewById(R.id.main_caption);
        mImage = (Button) findViewById(R.id.main_image);
        mImage.setOnClickListener(TakeImage);
        mImageView = (ImageView) findViewById(R.id.main_image_view);
        mSubmit = (Button) findViewById(R.id.main_submit);
        mSubmit.setOnClickListener(SubmitClick);
        mMapFrag = (MapFragment)getFragmentManager().findFragmentById(R.id.map_mapview);
        mMapFrag.getMapAsync(this);

        set_view();
    }

    void set_view(){
        ArrayAdapter<CharSequence> TitleList = ArrayAdapter.createFromResource(MainActivity.this,
                R.array.return_type,
                android.R.layout.simple_spinner_dropdown_item);
        mTitle.setAdapter(TitleList);
    }

    View.OnClickListener SubmitClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.e("Submit","SSS");
            String url = "http://hack.legbone.tw:8000/";
            OkHttpUtils
                    .get()
                    .url(url)
                    .addParams("user", mUser.getText().toString())
                    .addParams("contact", mContact.getText().toString())
                    .addParams("lat", String.valueOf(nowLat))
                    .addParams("lon", String.valueOf(nowLon))
                    .addParams("address", mAddress.getText().toString())
                    .addParams("title", mTitle.getSelectedItem().toString())
                    .addParams("caption", mCaption.getText().toString())
                    .addParams("status", "0")
                    .addParams("image", PostImageUrl)
                    .addParams("end", "0")
                    .build()
                    .execute(new StringCallback()
                    {
                        @Override
                        public void onError(Call call, Exception e) {
                            Log.e("Error", e.toString());
                            Toast.makeText(MainActivity.this,
                                    "通報失敗，請檢察網路狀態", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onResponse(String response)
                        {
                            Log.e("Response", response.toString());
                            Toast.makeText(MainActivity.this,
                                    "通報成功！", Toast.LENGTH_LONG).show();
                        }
                    });
        }
    };

    View.OnClickListener TakeImage = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //TODO Take a picture
            Intent startCustomCameraIntent = new Intent(MainActivity.this, CameraActivity.class);
            startActivityForResult(startCustomCameraIntent, REQUEST_CAMERA);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;

        if (requestCode == REQUEST_CAMERA) {
            Uri photoUri = data.getData();
            Log.e("Img Uri", photoUri.getPath());
            //TODO Update picture to imgur
            mImageView.setImageResource(R.drawable.imageupdateing);
            Update_Imgur(photoUri.getPath());

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    void Update_Imgur(String img_path){
        File imgFile = new File(img_path);
        if(imgFile.exists()){
            ImgurClient.getInstance()
                    .uploadImage(
                            new TypedFile("image/*", imgFile),
                            mTitle.getSelectedItem().toString(),
                            mCaption.getText().toString(),
                            new Callback<ImgurResponse<Image>>() {
                                @Override
                                public void success(ImgurResponse<Image> imageImgurResponse, Response response) {
                                    //Do something with your response.
                                    Log.e("TTT",imageImgurResponse.data.getLink());
                                    PostImageUrl = imageImgurResponse.data.getLink();

                                    OkHttpUtils
                                            .get()//
                                            .url(imageImgurResponse.data.getLink())//
                                            .build()//
                                            .execute(new BitmapCallback()
                                            {
                                                @Override
                                                public void onError(Call call, Exception e) {
                                                    Log.e("onError:" , e.getMessage());
                                                    Toast.makeText(MainActivity.this,
                                                            "上傳失敗，請檢察網路狀態", Toast.LENGTH_LONG).show();
                                                }
                                                @Override
                                                public void onResponse(Bitmap bitmap)
                                                {
                                                    mImageView.setImageBitmap(bitmap);
                                                }
                                            });




                                }

                                @Override
                                public void failure(RetrofitError error) {
                                    //Notify user of failure
                                    Log.e("EEE",error.toString());
                                }
                            }
                    );
        }

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        // 右上角的定位功能 檢查權限
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //User has previously accepted this permission
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }
        } else {
            //Not in api-23, no need to prompt
            mMap.setMyLocationEnabled(true);
        }
        mMap.getUiSettings().setZoomControlsEnabled(true); // 右下角的放大縮小功能
        mMap.getUiSettings().setCompassEnabled(true); // 左上角的指南針，要兩指旋轉才會出現
        mMap.getUiSettings().setMapToolbarEnabled(true); // 右下角的導覽及開啟 Google Map功能

        //mMap.addMarker(new MarkerOptions().position(new LatLng(23.5997904, 120.5120964)).title("互助自助餐").icon(BitmapDescriptorFactory.fromResource(R.drawable.star)));
        // 為了讓Google Map的Toolbar得以使用，所以要先建立一個Marker(地標物)
        //gps_core.whereAmI();
        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        Location location = locationManager.getLastKnownLocation(locationManager
                .getBestProvider(criteria, false));
        nowLat = location.getLatitude();
        nowLon = location.getLongitude();
        mGPS.setText("經度：" + nowLat + "\n緯度：" + nowLon);
    }
}
