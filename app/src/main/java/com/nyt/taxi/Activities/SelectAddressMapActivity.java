package com.nyt.taxi.Activities;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

import com.nyt.taxi.Kalman.Services.ServicesHelper;
import com.nyt.taxi.R;
import com.nyt.taxi.Utils.UConfig;
import com.nyt.taxi.Utils.UGeocoderAnswer;
import com.nyt.taxi.Utils.UPref;
import com.nyt.taxi.Web.WebQuery;
import com.nyt.taxi.Web.WebResponse;
import com.nyt.taxi.databinding.ActivitySelectAddressMapBinding;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraListener;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.CameraUpdateReason;
import com.yandex.mapkit.map.IconStyle;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.runtime.image.ImageProvider;

public class SelectAddressMapActivity extends BaseActivity implements CameraListener {

    private ActivitySelectAddressMapBinding bind;
    private PlacemarkMapObject mMiddle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivitySelectAddressMapBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());
        bind.ready.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        bind.mapview.onStart();
        if (ServicesHelper.getLocationService() == null) {
            return;
        }
        if (ServicesHelper.getLocationService().getLastLocation() == null) {
            return;
        }
        Point p = new Point(getIntent().getDoubleExtra("lat", 0.0), getIntent().getDoubleExtra("lon", 0.0));
        if (p.getLatitude() < 0.001) {
            p = new Point((double) UPref.getFloat("last_lat"), (double) UPref.getFloat("last_lon"));
        }
        mMiddle = bind.mapview.getMap().getMapObjects().addPlacemark(p);
        IconStyle ic = new IconStyle();
        mMiddle.setIcon(ImageProvider.fromResource(this, UPref.getBoolean( "navigator_nightmode") ? R.drawable.navigator : R.drawable.navigator), ic);
        mMiddle.setGeometry(p);
        mMiddle.setDraggable(true);
        bind.mapview.getMap().move(new CameraPosition(p, 15, 0, 0));
        bind.mapview.getMap().addCameraListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        bind.mapview.getMap().getMapObjects().clear();
        bind.mapview.onStop();
    }

    WebResponse mWebResponse = new WebResponse() {
        @Override
        public void webResponse(int code, int webResponse, String s) {
            hideProgressDialog();
            switch (code) {
                case mResponseGeocodeLatLon:
                    UGeocoderAnswer gg = new UGeocoderAnswer(s);
                    if (gg == null || !gg.isValid) {
                        return;
                    }
                    bind.address.setText(gg.mFull);
                    break;
            }
        }
    };


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ready:
                Intent resultIntent = new Intent();
                resultIntent.putExtra("lat", mMiddle.getGeometry().getLatitude());
                resultIntent.putExtra("lon", mMiddle.getGeometry().getLongitude());
                resultIntent.putExtra("address", bind.address.getText().toString());
                setResult(RESULT_OK, resultIntent);
                finish();
                break;
        }
    }

    @Override
    public void onCameraPositionChanged(@NonNull Map map, @NonNull CameraPosition cameraPosition, @NonNull CameraUpdateReason cameraUpdateReason, boolean finished) {
        bind.address.setText(getString(R.string.AddressRecognize));
        mMiddle.setGeometry(cameraPosition.getTarget());
        if (finished) {
            String link = String.format("https://geocode-maps.yandex.ru/1.x/?apikey=%s&format=json&kind=house&geocode=%f,%f&results=1&sco=latlong",
                    UConfig.mGeocoderApiKey, cameraPosition.getTarget().getLatitude(), cameraPosition.getTarget().getLongitude());
            WebQuery webQuery = new WebQuery(link, WebQuery.HttpMethod.GET, WebResponse.mResponseGeocodeLatLon, mWebResponse);
            webQuery.request();
        }
    }
}