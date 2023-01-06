package com.nyt.taxi2.Activities;

import android.os.Bundle;
import android.view.View;

import com.nyt.taxi2.Model.GDriverStatus;
import com.nyt.taxi2.Model.GOneOrder;
import com.nyt.taxi2.R;
import com.nyt.taxi2.Utils.UPref;
import com.nyt.taxi2.Web.WQOrdersHistoryRoute;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nyt.taxi2.Web.WebResponse;
import com.nyt.taxi2.databinding.ActivityOrdersHistoryRouteBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OrdersHistoryRouteActivity extends BaseActivity {

    private int mId;
    private ActivityOrdersHistoryRouteBinding mBind;
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
        WQOrdersHistoryRoute  ordersHistoryRoute = new WQOrdersHistoryRoute(String.format("%d", mId), mWebResponse);
        //WebHistoryDetails ordersHistoryRoute = new WebHistoryDetails(mId, this);
        ordersHistoryRoute.request();
    }

    @Override
    protected void onPause() {
        super.onPause();
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
        List<GDriverStatus.Point> plPoints = new ArrayList<>();
        for (int i = 0; i < mOneOrder.pauses.size(); i++) {

        }
        for (int i = 0; i < mOneOrder.trajectory.size(); i++) {
            if (i == 0) {

            }
            if (i == mOneOrder.trajectory.size() - 1) {

            }
            plPoints.add(mOneOrder.trajectory.get(i).getPoint());
        }

    }

    private void drawRoute(String s) {

    }
}