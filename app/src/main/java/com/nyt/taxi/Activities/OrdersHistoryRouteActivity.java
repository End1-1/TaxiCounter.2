package com.nyt.taxi.Activities;

import android.os.Bundle;
import android.view.View;

import com.nyt.taxi.Model.GOneOrder;
import com.nyt.taxi.R;
import com.nyt.taxi.Utils.UPref;
import com.nyt.taxi.Web.WQOrdersHistoryRoute;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nyt.taxi.Web.WebResponse;
import com.nyt.taxi.databinding.ActivityOrdersHistoryRouteBinding;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.geometry.Polyline;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.IconStyle;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.map.PolylineMapObject;
import com.yandex.runtime.image.ImageProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OrdersHistoryRouteActivity extends BaseActivity {

    private int mId;
    private ActivityOrdersHistoryRouteBinding mBind;
    private MapObjectCollection mMapObjects;
    private GOneOrder mOneOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityOrdersHistoryRouteBinding.inflate(getLayoutInflater());
        mBind.back.setOnClickListener(this);
        setContentView(mBind.getRoot());
        mId = getIntent().getIntExtra("id", 0);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mBind.mapview.onStart();
        mMapObjects = mBind.mapview.getMap().getMapObjects();
        WQOrdersHistoryRoute  ordersHistoryRoute = new WQOrdersHistoryRoute(String.format("%d", mId), mWebResponse);
        //WebHistoryDetails ordersHistoryRoute = new WebHistoryDetails(mId, this);
        ordersHistoryRoute.request();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBind.mapview.onStop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                super.onBackPressed();
                break;
        }
    }

    WebResponse mWebResponse = new WebResponse() {
        @Override
        public void webResponse(int code, int webResponse, String s) {
            hideProgressDialog();
            if (webResponse > 299) {
                //finish();
                return;
            }
            switch (code) {
                case mResponseHistoryDetailes:
                    drawRoute(s);
                    break;
                case mResponseOrderHistoryRoute:
                    mOneOrder = GOneOrder.parse(s, GOneOrder.class);
                    parseOrder();
                    break;
            }
        }
    };


    void parseOrder() {
        if (mOneOrder == null) {
            return;
        }

        double distance = mOneOrder.distance;
        mBind.tvDistance.setText(String.format("%.1f", distance));

        long time = mOneOrder.duration;
        long hour = time / 3600;
        long min = (time - (hour * 3600)) / 60;
        long sec = time % 60;
        mBind.tvRideTime.setText(String.format("%02d:%02d։%02d", hour, min, sec));

        mBind.startTime.setText(String.format("%s", mOneOrder.datetime.start_date));
        mBind.time.setText(String.format("%s", mOneOrder.datetime.start_time));
        //mBind.endTime.setText(String.format("%s %s", mOneOrder.datetime.end_date, mOneOrder.datetime.end_time));
        mBind.price.setText(String.format("%.1f%s", mOneOrder.cost, getString(R.string.RubSymbol)));
        mBind.paymentMethod.setText(mOneOrder.cache_type);
        mBind.from.setText(mOneOrder.from);
        mBind.to.setText(mOneOrder.to);
        List<Point> plPoints = new ArrayList<>();
        for (int i = 0; i < mOneOrder.pauses.size(); i++) {
            PlacemarkMapObject pm = mBind.mapview.getMap().getMapObjects().addPlacemark(mOneOrder.pauses.get(i).getPoint());
            IconStyle ic = new IconStyle();
            ImageProvider ip = ImageProvider.fromResource(this, R.drawable.pmpause);
            pm.setIcon(ip, ic);
            pm.setGeometry(mOneOrder.pauses.get(i).getPoint());
        }
        for (int i = 0; i < mOneOrder.trajectory.size(); i++) {
            if (i == 0) {
                PlacemarkMapObject pm = mBind.mapview.getMap().getMapObjects().addPlacemark(mOneOrder.trajectory.get(i).getPoint());
                IconStyle ic = new IconStyle();
                ImageProvider ip = ImageProvider.fromResource(this, R.drawable.pmstart);
                pm.setIcon(ip, ic);
                pm.setGeometry(mOneOrder.trajectory.get(i).getPoint());
                continue;
            }
            if (i == mOneOrder.trajectory.size() - 1) {
                PlacemarkMapObject pm = mBind.mapview.getMap().getMapObjects().addPlacemark(mOneOrder.trajectory.get(mOneOrder.trajectory.size() - 1).getPoint());
                IconStyle ic = new IconStyle();
                ImageProvider ip = ImageProvider.fromResource(this, R.drawable.pmend);
                pm.setIcon(ip, ic);
                pm.setGeometry(mOneOrder.trajectory.get(mOneOrder.trajectory.size() - 1).getPoint());
                continue;
            }
            plPoints.add(mOneOrder.trajectory.get(i).getPoint());
        }
        Polyline pl = new Polyline(plPoints);
        PolylineMapObject pm = mMapObjects.addPolyline(pl);
        int color = 0xff0000ff;
        pm.setStrokeWidth(4);
        pm.setStrokeColor(color);
        pm.setOutlineWidth(1f);
        pm.setOutlineColor(0xffff0000);
        if (mOneOrder.trajectory.size() > 0) {
            CameraPosition cp = new CameraPosition(mOneOrder.trajectory.get(0).getPoint(), 14f, 0, 0);
            mBind.mapview.getMap().move(cp);
        }
    }

    private void drawRoute(String s) {
        List<Point> points = new ArrayList<>();
        JsonParser jp = new JsonParser();
        JsonObject jo = jp.parse(s).getAsJsonObject();
        JsonArray ja = jo.getAsJsonArray("trajectory");
        for (int  i = 0; i < ja.size(); i++) {
            JsonObject jc = ja.get(i).getAsJsonObject();
            Point p = new Point(jc.get("lat").getAsDouble(), jc.get("lut").getAsDouble());
            points.add(p);
        }
        for (int i = 0; i < points.size(); i++) {
            if (i == 0) {
                continue;
            }
            List<Point> plPoints = new ArrayList<>();
            plPoints.add(points.get(i - 1));
            plPoints.add(points.get(i));
            Polyline pl = new Polyline(plPoints);
            PolylineMapObject pm = mMapObjects.addPolyline(pl);
            int color = 0xff0000ff;
            pm.setStrokeWidth(7);
//            mRidePolylines.add(pm);
//            if (oneRoute.mJamSegments.size() > 0) {
//                JamSegment js = oneRoute.mJamSegments.get(i - 1);
//                switch (js.getJamType()) {
//                    case BLOCKED:
//                        color = 0xffffffff;
//                        break;
//                    case FREE:
//                        color = 0xffafffc0;
//                        break;
//                    case LIGHT:
//                        color = 0xfff7ff71;
//                        break;
//                    case HARD:
//                        color = 0xffff0000;
//                        break;
//                    case VERY_HARD:
//                        color = 0xffffff00;
//                        break;
//                    default:
//                        color = 0xff00ff00;
//                        break;
//                }
            pm.setStrokeColor(color);
            pm.setOutlineWidth(1f);
            pm.setOutlineColor(0xffff0000);
        }
        CameraPosition cp = new CameraPosition(points.get(0), 14f, 0, 0);
        mBind.mapview.getMap().move(cp);
    }
}